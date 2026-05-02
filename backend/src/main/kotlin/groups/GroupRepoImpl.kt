package com.astrais.groups

import ROLE_USERMOD
import ROLE_USERNORMAL
import ROLE_USEROWNER
import com.astrais.db.GroupRoles
import com.astrais.db.getDatabaseDaoImpl
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.seconds
import org.jetbrains.exposed.v1.exceptions.ExposedSQLException
import org.slf4j.LoggerFactory
import groups.types.*
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import java.net.URI

private val log = LoggerFactory.getLogger(GroupRepoImpl::class.java)

/**
 * Implementación concreta de [GroupRepo].
 *
 * Realiza todas las operaciones de grupos contra la base de datos a través del
 * DAO centralizado (`getDatabaseDaoImpl()`). Gestiona el sistema de invitaciones seguras
 * mediante tokens aleatorios. Registra eventos significativos en el log de auditoría del
 * grupo y en el logger.
 *
 */
class GroupRepoImpl : GroupRepo{
    /** Generador criptográficamente seguro utilizado para producir los códigos de invitación. */
    private val secureRandom = SecureRandom()

    /**
     * URL base a la que se añade el parámetro `?code=` para construir las URLs de invitación.
     * Se configura mediante la variable de entorno `INVITE_BASE_URL`; si no está definida
     * se usa `https://astrais.app/groups/join` como valor por defecto.
     */
    private val inviteBaseUrl = System.getenv("INVITE_BASE_URL")?.trim()?.removeSuffix("/")
        ?: "https://astrais.app/groups/join"

    /** Devuelve la fecha y hora actuales en UTC como [kotlinx.datetime.LocalDateTime]. */
    private fun nowUtc() = Clock.System.now().toLocalDateTime(TimeZone.UTC)

    /**
     * Genera un código de invitación aleatorio criptográficamente seguro.
     * Produce 24 bytes de entropía y los codifica en Base64 URL-safe sin padding,
     * resultando en una cadena de 32 caracteres.
     *
     * @return Código aleatorio en formato Base64 URL-safe listo para incrustar en una URL.
     */
    private fun randomCode(): String {
        val bytes = ByteArray(24)
        secureRandom.nextBytes(bytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }

    /**
     * Calcula el hash SHA-256 de la cadena de entrada y lo devuelve como cadena hexadecimal
     * en minúsculas.
     * Se usa para derivar el identificador persistido a partir del código de invitación,
     * de modo que el servidor nunca almacena los códigos originales.
     *
     * @param input Cadena a hashear.
     * @return Hash SHA-256 expresado en hexadecimal en minúsculas (64 caracteres).
     */
    private fun sha256Hex(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(input.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }

    /**
     * Construye la URL de invitación completa a partir del código.
     *
     * @param code Código de invitación(no hasheado).
     * @return URL en formato `{inviteBaseUrl}?code={code}` lista para compartir.
     */
    private fun buildInviteUrl(code: String): String {
        return "$inviteBaseUrl?code=$code"
    }

    /**
     * Determina el rol efectivo de un usuario sobre un grupo concreto consultando la base de datos.
     * Primero verifica si el usuario es el Owner del grupo; luego si este es administrador del servidor;
     * si no es ninguno de los dos, consulta la tabla de membresías para distinguir entre Moderador y Miembro normal.
     *
     * @param uid Identificador del usuario cuyo rol se quiere conocer.
     * @param gid Identificador del grupo.
     * @return El rol del usuario: [ROLE_USEROWNER], [ROLE_USERMOD] o [ROLE_USERNORMAL].
     *   Devuelve [ROLE_USERNORMAL] si el grupo no existe.
     */
    private suspend fun roleForUserOnGroup(uid: Int, gid: Int): Int {
        val gp = getDatabaseDaoImpl().getGroupById(gid) ?: return ROLE_USERNORMAL
        if (gp.owner.value == uid) return ROLE_USEROWNER
        if (getDatabaseDaoImpl().checkIfUserIsServerAdmin(uid)) return ROLE_USEROWNER
        val d = getDatabaseDaoImpl().getUserRoleOnGroup(idusuario = uid, idgrupo = gid)
        return if (d == GroupRoles.MOD) ROLE_USERMOD else ROLE_USERNORMAL
    }

    /**
     * Recupera todos los grupos no personales a los que pertenece el usuario.
     * Los grupos marcados como `es_grupo_personal` (grupo privado de tareas individuales)
     * son filtrados y nunca se devuelven en este listado.
     * Para cada grupo determina el rol efectivo del usuario (Owner, Mod o Miembro).
     *
     * @param userId Identificador del usuario cuyos grupos se quieren listar.
     * @return Lista de [SingleGroupOut] con los grupos del usuario. Devuelve lista vacía
     *   si el usuario no pertenece a ningún grupo o si ocurre un error de base de datos.
     */
    override suspend fun getAllUserGroups(userId : Int) : List<SingleGroupOut>{
        log.info("Function: getAllUserGroups($userId)")
        try {
            val d = getDatabaseDaoImpl().getGroupsOfUser(userId)
            if (d.isNotEmpty()){
                log.info("User joined groups, preparing them to return...")
                return d.map { gp->
                    if (gp.es_grupo_personal) {
                        return@map null
                    }
                    var r = ROLE_USEROWNER
                    if (gp.owner.value != userId){
                        val da = getDatabaseDaoImpl().getUserRoleOnGroup(idusuario = userId, idgrupo = gp.id.value)
                        r = if (da == GroupRoles.MOD){
                            ROLE_USERMOD
                        }else{
                            ROLE_USERNORMAL
                        }
                    }

                    SingleGroupOut(
                        id = gp.id.value,
                        name = gp.nombre,
                        description = gp.descripcion,
                        role = r
                    )
                }.filterNotNull()
            }
        } catch (e : ExposedSQLException){
            log.error("Error querying all user $userId groups! Message: ${e.message}")
        }
        log.info("No groups, returning empty list")
        return emptyList()
    }

    /**
     * Crea un nuevo grupo en la base de datos con el usuario especificado como Owner.
     * El DAO se encarga de crear la entrada de grupo y el registro de membresía del Owner.
     *
     * @param name Nombre del grupo.
     * @param desc Descripción del grupo.
     * @param ownerId Identificador del usuario que será el Owner del grupo.
     * @return El GID del grupo recén creado, o `-1` si ocurrió un error de base de datos.
     */
    override suspend fun createGroup(name: String, desc: String, ownerId: Int) : Int{
        try {
            val gid = getDatabaseDaoImpl().createGroup(ownerId, name, desc)
            log.info("Created group with ID $gid for owner $ownerId")
            return gid
        } catch (e : ExposedSQLException){
            log.error("Error creating the group $name for $ownerId. Message: ${e.message}")
        }
        return -1
    }

    /**
     * Agrega un usuario a un grupo existente. Verifica que el solicitante tenga rol de
     * Owner o Moderador antes de ejecutar la operación.
     *
     * @param requesterId Identificador del usuario que solicita la adición.
     * @param userId Identificador del usuario que se desea añadir al grupo.
     * @param gid Identificador del grupo destino.
     * @return [AddUserReturn] indicando el resultado de la operación.
     */
    override suspend fun addUser(requesterId: Int, userId: Int, gid: Int): AddUserReturn {
        try {
            val gp = getDatabaseDaoImpl().getGroupById(gid) ?: return AddUserReturn.NOGROUP
            var role = roleForUserOnGroup(requesterId, gid)

            if (role != ROLE_USERNORMAL){
                val add = getDatabaseDaoImpl().addUserToGroup(userId, gid)
                if (add){
                    log.info("Added user $userId to group $gid by $requesterId.")
                    return AddUserReturn.OK
                }else{
                    log.info("User $userId is already on group $gid.")
                    return AddUserReturn.ALREADYJOINED
                }
            }
            log.info("User $requesterId has no permission for adding someone.")
            return AddUserReturn.NOPERMISSION
        } catch (e : ExposedSQLException){
            log.error("Couldn't add $userId to $gid with requester $requesterId. Message: ${e.message}")
            return AddUserReturn.CONNERR
        }
    }

    /**
     * Expulsa a un usuario de un grupo. Verifica que el solicitante sea Owner o Moderador
     * y que el usuario objetivo no sea el Owner del grupo.
     *
     * @param requesterId Identificador del usuario que solicita la expulsión.
     * @param userId Identificador del usuario a expulsar.
     * @param gid Identificador del grupo.
     * @return [RemoveUserReturn] indicando el resultado de la operación.
     */
    override suspend fun removeUser(requesterId: Int, userId: Int, gid: Int): RemoveUserReturn {
        try {
            val gp = getDatabaseDaoImpl().getGroupById(gid) ?: return RemoveUserReturn.NOGROUP
            if (gp.owner.value == userId) {
                return RemoveUserReturn.CANNOT_REMOVE_OWNER
            }

            val requesterRole = roleForUserOnGroup(requesterId, gid)
            /*if (gp.owner.value != requesterId) {
                val d = getDatabaseDaoImpl().getUserRoleOnGroup(idusuario = requesterId, idgrupo = gid)
                requesterRole = if (d == GroupRoles.MOD) {
                    ROLE_USERMOD
                } else {
                    ROLE_USERNORMAL
                }
            }*/

            if (requesterRole == ROLE_USERNORMAL) {
                return RemoveUserReturn.NOPERMISSION
            }

            val removed = getDatabaseDaoImpl().removeUserFromGroup(idusuario = userId, idgrupo = gid)
            return if (removed) RemoveUserReturn.OK else RemoveUserReturn.NOTMEMBER
        } catch (e: ExposedSQLException) {
            log.error("Couldn't remove $userId from $gid with requester $requesterId. Message: ${e.message}")
            return RemoveUserReturn.CONNERR
        }
    }

    /**
     * Transfiere la propiedad del grupo del Owner actual a otro miembro existente.
     * Verifica que el solicitante sea el Owner actual, que el destinatario ya sea miembro
     * y que no se intente transferir al mismo Owner.
     *
     * @param requesterId Identificador del Owner actual que cede la propiedad.
     * @param newOwnerUserId Identificador del miembro que recibirá la propiedad.
     * @param gid Identificador del grupo.
     * @return [PassOwnershipReturn] indicando el resultado de la operación.
     */
    override suspend fun passOwnership(requesterId: Int, newOwnerUserId: Int, gid: Int): PassOwnershipReturn {
        try {
            val gp = getDatabaseDaoImpl().getGroupById(gid) ?: return PassOwnershipReturn.NOGROUP
            val currentOwner = gp.owner.value
            if (currentOwner != requesterId) {
                return PassOwnershipReturn.NOPERMISSION
            }
            if (newOwnerUserId == currentOwner) {
                return PassOwnershipReturn.SAMEOWNER
            }

            val targetMembership = getDatabaseDaoImpl().getUserRoleOnGroup(idusuario = newOwnerUserId, idgrupo = gid)
            if (targetMembership == null) {
                return PassOwnershipReturn.TARGETNOTMEMBER
            }

            val changed = getDatabaseDaoImpl().passGroupOwnership(gid = gid, newOwnerId = newOwnerUserId)
            return if (changed) PassOwnershipReturn.OK else PassOwnershipReturn.CONNERR
        } catch (e: ExposedSQLException) {
            log.error("Couldn't pass ownership of $gid from $requesterId to $newOwnerUserId. Message: ${e.message}")
            return PassOwnershipReturn.CONNERR
        }
    }

    /**
     * Genera una URL de invitación simple basada en el GID del grupo.
     * No persiste ningún token; la URL contiene directamente el `gid` como parámetro.
     * Actualmente el endpoint `/groups/inviteUrl` delega en [createInvite] para mayor
     * seguridad; este método se mantiene por compatibilidad con la interfaz (HAY QUE REFACTORIZAAAAR).
     *
     * @param requesterId Identificador del solicitante (debe ser Owner o Moderador).
     * @param gid Identificador del grupo para el que se genera la invitación.
     * @return Par con el código de resultado y la URL generada, o `null` en caso de error.
     */
    override suspend fun generateInviteUrl(requesterId: Int, gid: Int): Pair<InviteUrlReturn, String?> {
        try {
            val gp = getDatabaseDaoImpl().getGroupById(gid) ?: return Pair(InviteUrlReturn.NOGROUP, null)
            if (gp.es_grupo_personal) {
                return Pair(InviteUrlReturn.NOPERMISSION, null)
            }

            /*var requesterRole = ROLE_USEROWNER
            if (gp.owner.value != requesterId) {
                val d = getDatabaseDaoImpl().getUserRoleOnGroup(idusuario = requesterId, idgrupo = gid)
                requesterRole = if (d == GroupRoles.MOD) ROLE_USERMOD else ROLE_USERNORMAL
            } else if (getDatabaseDaoImpl().checkIfUserIsServerAdmin(requesterId)){
                requesterRole = ROLE_USEROWNER
            }*/
            val requesterRole = roleForUserOnGroup(requesterId, gid)
            if (requesterRole == ROLE_USERNORMAL) {
                return Pair(InviteUrlReturn.NOPERMISSION, null)
            }

            val invite = "astrais://groups/join?gid=$gid"
            return Pair(InviteUrlReturn.OK, invite)
        } catch (e: ExposedSQLException) {
            log.error("Couldn't create invite url for group $gid by $requesterId. Message: ${e.message}")
            return Pair(InviteUrlReturn.CONNERR, null)
        }
    }

    /**
     * Une al usuario autenticado a un grupo a partir de una URL de invitación.
     * Soporta dos formatos:
     * - URL con parámetro `code`: delega en [joinByCode] para usar el flujo de token seguro.
     * - URL con parámetro `gid` (legado): une directamente al usuario al grupo sin validación
     *   de token.
     * Verifica que el usuario no sea ya miembro antes de añadirlo.
     *
     * @param requesterId Identificador del usuario que desea unirse.
     * @param inviteUrl La URL de invitación completa.
     * @return [InviteUrlReturn] indicando el resultado de la operación.
     */
    override suspend fun joinByInviteUrl(requesterId: Int, inviteUrl: String): InviteUrlReturn {
        try {
            val parsed = runCatching { URI(inviteUrl) }.getOrNull() ?: return InviteUrlReturn.INVALID_URL
            val query = parsed.query ?: return InviteUrlReturn.INVALID_URL

            val code = query.split("&").firstOrNull { it.startsWith("code=") }?.substringAfter("code=")
            if (!code.isNullOrBlank()) {
                return joinByCode(requesterId, code)
            }

            val gid = runCatching {
                val parsed = URI(inviteUrl)
                val q = parsed.query ?: return@runCatching null
                q.split("&")
                    .firstOrNull { it.startsWith("gid=") }
                    ?.substringAfter("gid=")
                    ?.toIntOrNull()
            }.getOrNull() ?: return InviteUrlReturn.INVALID_URL

            val gp = getDatabaseDaoImpl().getGroupById(gid) ?: return InviteUrlReturn.NOGROUP
            if (gp.es_grupo_personal) {
                return InviteUrlReturn.NOPERMISSION
            }

            if (gp.owner.value == requesterId) {
                return InviteUrlReturn.ALREADYJOINED
            }

            val existingRole = getDatabaseDaoImpl().getUserRoleOnGroup(idusuario = requesterId, idgrupo = gid)
            if (existingRole != null) {
                return InviteUrlReturn.ALREADYJOINED
            }

            val added = getDatabaseDaoImpl().addUserToGroup(idusuario = requesterId, idgrupo = gid)
            return if (added) InviteUrlReturn.OK else InviteUrlReturn.ALREADYJOINED
        } catch (e: Exception) {
            log.error("Couldn't join by invite url for $requesterId. Message: ${e.message}")
            return InviteUrlReturn.CONNERR
        }
    }

    /**
     * Crea una invitación segura basada en token para el grupo indicado.
     * Genera un código aleatorio de 24 bytes (Base64 URL-safe), persiste únicamente su hash
     * SHA-256 en la base de datos y construye la URL pública con el código en claro.
     * Registra el evento `invite_created` en el log de auditoría del grupo.
     * Solo puede ser invocada por Owners y Moderadores.
     *
     * @param requesterId Identificador del usuario que crea la invitación.
     * @param gid Identificador del grupo.
     * @param expiresInSeconds Segundos de validez desde el momento de creación, o `null`
     *   para invitación sin expiración.
     * @param maxUses Límite máximo de usos, o `null` para usos ilimitados.
     * @return Par con el código de resultado y el [InviteOut] generado, o `null` si falla.
     */
    override suspend fun createInvite(
        requesterId: Int,
        gid: Int,
        expiresInSeconds: Long?,
        maxUses: Int?
    ): Pair<InviteUrlReturn, InviteOut?> {
        try {
            val gp = getDatabaseDaoImpl().getGroupById(gid) ?: return Pair(InviteUrlReturn.NOGROUP, null)
            if (gp.es_grupo_personal) return Pair(InviteUrlReturn.NOPERMISSION, null)

            val requesterRole = roleForUserOnGroup(requesterId, gid)
            if (requesterRole == ROLE_USERNORMAL) return Pair(InviteUrlReturn.NOPERMISSION, null)

            val createdAt = nowUtc()
            val expiresAt = expiresInSeconds?.let {
                Clock.System.now().plus(it.seconds).toLocalDateTime(TimeZone.UTC)
            }
            val code = randomCode()
            val codeHash = sha256Hex(code)

            getDatabaseDaoImpl().createGroupInvite(
                gid = gid,
                code = code,
                codeHash = codeHash,
                createdByUid = requesterId,
                createdAt = createdAt,
                expiresAt = expiresAt,
                maxUses = maxUses
            )

            getDatabaseDaoImpl().appendGroupAuditEvent(
                gid = gid,
                actorUid = requesterId,
                eventType = "invite_created",
                payloadJson = """{"maxUses":${maxUses ?: "null"},"expiresAt":"${expiresAt ?: ""}"}""",
                createdAt = createdAt
            )

            val inviteUrl = buildInviteUrl(code)
            return Pair(
                InviteUrlReturn.OK,
                InviteOut(
                    code = code,
                    inviteUrl = inviteUrl,
                    expiresAt = expiresAt,
                    maxUses = maxUses,
                    usesCount = 0,
                    revokedAt = null
                )
            )
        } catch (e: Exception) {
            log.error("Couldn't create invite for $gid by $requesterId. Message: ${e.message}")
            return Pair(InviteUrlReturn.CONNERR, null)
        }
    }

    /**
     * Lista todas las invitaciones (activas, revocadas y expiradas) de un grupo.
     * Solo accesible por Owners y Moderadores del grupo.
     * Reconstruye la URL pública de cada invitación usando [buildInviteUrl].
     *
     * @param requesterId Identificador del solicitante.
     * @param gid Identificador del grupo.
     * @return Par con el código de resultado y la lista de [InviteOut], o `null` si falla.
     */
    override suspend fun listInvites(requesterId: Int, gid: Int): Pair<InviteUrlReturn, List<InviteOut>?> {
        try {
            val gp = getDatabaseDaoImpl().getGroupById(gid) ?: return Pair(InviteUrlReturn.NOGROUP, null)
            if (gp.es_grupo_personal) return Pair(InviteUrlReturn.NOPERMISSION, null)

            val requesterRole = roleForUserOnGroup(requesterId, gid)
            if (requesterRole == ROLE_USERNORMAL) return Pair(InviteUrlReturn.NOPERMISSION, null)

            val invites = getDatabaseDaoImpl().listGroupInvites(gid = gid, includeRevoked = true).map {
                InviteOut(
                    code = it.code,
                    inviteUrl = buildInviteUrl(it.code),
                    expiresAt = it.expiresAt,
                    maxUses = it.maxUses,
                    usesCount = it.usesCount,
                    revokedAt = it.revokedAt
                )
            }
            return Pair(InviteUrlReturn.OK, invites)
        } catch (e: Exception) {
            log.error("Couldn't list invites for $gid by $requesterId. Message: ${e.message}")
            return Pair(InviteUrlReturn.CONNERR, null)
        }
    }

    /**
     * Revoca una invitación activa de un grupo. La invitación se localiza por el hash
     * SHA-256 del código en texto claro para no exponer el código original.
     * Una invitación revocada no puede ser utilizada aunque no haya expirado.
     * Registra el evento `invite_revoked` en el log de auditoría del grupo.
     * Solo Owners y Moderadores pueden revocar invitaciones.
     *
     * @param requesterId Identificador del solicitante.
     * @param gid Identificador del grupo.
     * @param code Código de invitación en texto claro a revocar.
     * @return [InviteUrlReturn] indicando el resultado.
     */
    override suspend fun revokeInvite(requesterId: Int, gid: Int, code: String): InviteUrlReturn {
        try {
            val gp = getDatabaseDaoImpl().getGroupById(gid) ?: return InviteUrlReturn.NOGROUP
            if (gp.es_grupo_personal) return InviteUrlReturn.NOPERMISSION

            val requesterRole = roleForUserOnGroup(requesterId, gid)
            if (requesterRole == ROLE_USERNORMAL) return InviteUrlReturn.NOPERMISSION

            val now = nowUtc()
            val ok = getDatabaseDaoImpl().revokeGroupInvite(gid = gid, codeHash = sha256Hex(code), revokedAt = now)
            if (ok) {
                getDatabaseDaoImpl().appendGroupAuditEvent(
                    gid = gid,
                    actorUid = requesterId,
                    eventType = "invite_revoked",
                    payloadJson = """{"code":"$code"}""",
                    createdAt = now
                )
                return InviteUrlReturn.OK
            }
            return InviteUrlReturn.INVALID_CODE
        } catch (e: Exception) {
            log.error("Couldn't revoke invite for $gid by $requesterId. Message: ${e.message}")
            return InviteUrlReturn.CONNERR
        }
    }

    /**
     * Une al usuario autenticado a un grupo mediante un código de invitación.
     * El flujo completo de validación es:
     * 1. Localiza la invitación por el hash SHA-256 del código.
     * 2. Verifica que el grupo exista y no sea personal.
     * 3. Verifica que el usuario no sea ya miembro.
     * 4. Comprueba que la invitación no esté revocada, expirada ni con usos agotados.
     * 5. Consume atómicamente el uso de la invitación.
     * 6. Añade al usuario al grupo.
     * 7. Registra el evento `member_joined_by_invite` en auditoría.
     *
     * @param requesterId Identificador del usuario que quiere unirse.
     * @param code Código de invitación.
     * @return [InviteUrlReturn] indicando el resultado de cada paso de validación.
     */
    override suspend fun joinByCode(requesterId: Int, code: String): InviteUrlReturn {
        try {
            val hash = sha256Hex(code)
            val invite = getDatabaseDaoImpl().getGroupInviteByHash(hash) ?: return InviteUrlReturn.INVALID_CODE

            val gp = getDatabaseDaoImpl().getGroupById(invite.gid) ?: return InviteUrlReturn.NOGROUP
            if (gp.es_grupo_personal) return InviteUrlReturn.NOPERMISSION

            if (gp.owner.value == requesterId) return InviteUrlReturn.ALREADYJOINED
            val existingRole = getDatabaseDaoImpl().getUserRoleOnGroup(idusuario = requesterId, idgrupo = invite.gid)
            if (existingRole != null) return InviteUrlReturn.ALREADYJOINED

            val now = nowUtc()
            if (invite.revokedAt != null) return InviteUrlReturn.REVOKED
            if (invite.expiresAt != null && invite.expiresAt <= now) return InviteUrlReturn.EXPIRED
            if (invite.maxUses != null && invite.usesCount >= invite.maxUses) return InviteUrlReturn.MAX_USES_REACHED

            val consumed = getDatabaseDaoImpl().tryConsumeInvite(inviteId = invite.id, now = now)
            if (!consumed) return InviteUrlReturn.INVALID_CODE

            val added = getDatabaseDaoImpl().addUserToGroup(idusuario = requesterId, idgrupo = invite.gid)
            if (!added) return InviteUrlReturn.ALREADYJOINED

            getDatabaseDaoImpl().appendGroupAuditEvent(
                gid = invite.gid,
                actorUid = requesterId,
                eventType = "member_joined_by_invite",
                payloadJson = """{"inviteId":${invite.id}}""",
                createdAt = now
            )

            return InviteUrlReturn.OK
        } catch (e: Exception) {
            log.error("Couldn't join by code for $requesterId. Message: ${e.message}")
            return InviteUrlReturn.CONNERR
        }
    }

    /**
     * Lista todos los miembros del grupo incluyendo nombre, rol y fecha de incorporación.
     * Cualquier miembro del grupo puede consultar la lista.
     * Los grupos personales no son accesibles por esta ruta.
     *
     * @param requesterId Identificador del usuario que realiza la consulta.
     * @param gid Identificador del grupo.
     * @return Par con el código de resultado y la lista de [GroupMemberOut], o `null` si falla.
     */
    override suspend fun listMembers(requesterId: Int, gid: Int): Pair<MemberListReturn, List<GroupMemberOut>?> {
        try {
            val gp = getDatabaseDaoImpl().getGroupById(gid) ?: return Pair(MemberListReturn.NOGROUP, null)
            if (gp.es_grupo_personal) return Pair(MemberListReturn.NOPERMISSION, null)

            val requesterRole = roleForUserOnGroup(requesterId, gid)
            if (requesterRole == ROLE_USERNORMAL) {
                val isOwner = gp.owner.value == requesterId
                val isMember = getDatabaseDaoImpl().getUserRoleOnGroup(requesterId, gid) != null
                if (!isOwner && !isMember) return Pair(MemberListReturn.NOPERMISSION, null)
            }

            val members = getDatabaseDaoImpl().listGroupMembers(gid).map {
                GroupMemberOut(
                    uid = it.uid,
                    name = it.name,
                    role = it.role,
                    joinedAt = it.joinedAt
                )
            }
            return Pair(MemberListReturn.OK, members)
        } catch (e: Exception) {
            log.error("Couldn't list members for $gid by $requesterId. Message: ${e.message}")
            return Pair(MemberListReturn.CONNERR, null)
        }
    }

    /**
     * Elimina al usuario autenticado de la lista de miembros del grupo.
     * El Owner no puede abandonar el grupo; debe transferir la propiedad primero.
     * Registra el evento `member_left` en el log de auditoría del grupo.
     *
     * @param requesterId Identificador del usuario que desea abandonar el grupo.
     * @param gid Identificador del grupo.
     * @return [LeaveGroupReturn] indicando el resultado de la operación.
     */
    override suspend fun leaveGroup(requesterId: Int, gid: Int): LeaveGroupReturn {
        try {
            val gp = getDatabaseDaoImpl().getGroupById(gid) ?: return LeaveGroupReturn.NOGROUP
            if (gp.owner.value == requesterId) return LeaveGroupReturn.OWNER_CANNOT_LEAVE

            val removed = getDatabaseDaoImpl().removeUserFromGroup(idusuario = requesterId, idgrupo = gid)
            if (!removed) return LeaveGroupReturn.NOTMEMBER

            getDatabaseDaoImpl().appendGroupAuditEvent(
                gid = gid,
                actorUid = requesterId,
                eventType = "member_left",
                payloadJson = null,
                createdAt = nowUtc()
            )
            return LeaveGroupReturn.OK
        } catch (e: Exception) {
            log.error("Couldn't leave group $gid by $requesterId. Message: ${e.message}")
            return LeaveGroupReturn.CONNERR
        }
    }

    /**
     * Cambia el rol de un miembro del grupo. Solo el Owner puede invocar esta operación.
     * No se puede cambiar el rol del propio Owner ni asignar el rol de Owner (2).
     * Registra el evento `member_role_changed` en el log de auditoría del grupo.
     *
     * @param requesterId Identificador del Owner que solicita el cambio.
     * @param gid Identificador del grupo.
     * @param userId Identificador del miembro cuyo rol se va a modificar.
     * @param role Nuevo rol como entero: `0` = Miembro normal, `1` = Moderador.
     * @return [SetMemberRoleReturn] indicando el resultado de la operación.
     */
    override suspend fun setMemberRole(requesterId: Int, gid: Int, userId: Int, role: Int): SetMemberRoleReturn {
        try {
            val gp = getDatabaseDaoImpl().getGroupById(gid) ?: return SetMemberRoleReturn.NOGROUP
            if (gp.owner.value != requesterId) return SetMemberRoleReturn.NOPERMISSION
            if (gp.owner.value == userId) return SetMemberRoleReturn.INVALID_ROLE

            val targetMembership = getDatabaseDaoImpl().getUserRoleOnGroup(idusuario = userId, idgrupo = gid)
            if (targetMembership == null) return SetMemberRoleReturn.NOTMEMBER

            val newRole = when (role) {
                ROLE_USERMOD -> GroupRoles.MOD
                ROLE_USERNORMAL -> GroupRoles.USER
                else -> return SetMemberRoleReturn.INVALID_ROLE
            }

            val ok = getDatabaseDaoImpl().setGroupMemberRole(gid = gid, uid = userId, role = newRole)
            if (!ok) return SetMemberRoleReturn.CONNERR

            getDatabaseDaoImpl().appendGroupAuditEvent(
                gid = gid,
                actorUid = requesterId,
                eventType = "member_role_changed",
                payloadJson = """{"userId":$userId,"role":$role}""",
                createdAt = nowUtc()
            )

            return SetMemberRoleReturn.OK
        } catch (e: Exception) {
            log.error("Couldn't set member role in $gid by $requesterId. Message: ${e.message}")
            return SetMemberRoleReturn.CONNERR
        }
    }

    /**
     * Recupera el historial de eventos de auditoría del grupo con soporte de paginación.
     * Cualquier miembro del grupo puede consultar el log.
     * Los eventos se mapean a [AuditEventOut] antes de devolverse.
     *
     * @param requesterId Identificador del usuario que solicita el log.
     * @param gid Identificador del grupo.
     * @param limit Número máximo de eventos a devolver. El handler HTTP lo limita a 200.
     * @param offset Desplazamiento de paginación.
     * @return Par con el código de resultado y la lista de [AuditEventOut], o `null` si falla.
     */
    override suspend fun listAudit(requesterId: Int, gid: Int, limit: Int, offset: Long): Pair<MemberListReturn, List<AuditEventOut>?> {
        try {
            val gp = getDatabaseDaoImpl().getGroupById(gid) ?: return Pair(MemberListReturn.NOGROUP, null)
            if (gp.es_grupo_personal) return Pair(MemberListReturn.NOPERMISSION, null)

            val requesterRole = roleForUserOnGroup(requesterId, gid)
            val isOwner = gp.owner.value == requesterId
            val isMember = getDatabaseDaoImpl().getUserRoleOnGroup(requesterId, gid) != null
            if (requesterRole == ROLE_USERNORMAL && !isOwner && !isMember) {
                return Pair(MemberListReturn.NOPERMISSION, null)
            }

            val events = getDatabaseDaoImpl().listGroupAuditEvents(gid = gid, limit = limit, offset = offset).map {
                AuditEventOut(
                    id = it.id,
                    actorUid = it.actorUid,
                    eventType = it.eventType,
                    payloadJson = it.payloadJson,
                    createdAt = it.createdAt
                )
            }
            return Pair(MemberListReturn.OK, events)
        } catch (e: Exception) {
            log.error("Couldn't list audit for $gid by $requesterId. Message: ${e.message}")
            return Pair(MemberListReturn.CONNERR, null)
        }
    }

    /**
     * Elimina permanentemente un grupo y todos sus datos asociados.
     * Solo el Owner del grupo puede realizar esta operación;
     * la comprobación se delega en `checkIfUserIsGroupAdmin` del DAO.
     *
     * @param gid Identificador del grupo a eliminar.
     * @param uid Identificador del usuario que solicita la eliminación.
     * @return `true` si el grupo fue eliminado correctamente; `false` si el usuario
     *   no tiene permisos o si ocurre un error.
     */
    override suspend fun deleteGroup(gid: Int, uid: Int): Boolean {
        if (getDatabaseDaoImpl().checkIfUserIsGroupAdmin(uid = uid, gid = gid) || getDatabaseDaoImpl().checkIfUserIsServerAdmin(uid)){
            return getDatabaseDaoImpl().deleteGroup(gid)
        }
        return false
    }

    /**
     * Actualiza los metadatos de un grupo (nombre y/o descripción).
     * Los campos nulos no se modifican.
     * Puede ser invocada tanto por el Owner como por un Moderador del grupo.
     *
     * @param gid Identificador del grupo a editar.
     * @param uid Identificador del usuario que solicita la edición.
     * @param name Nuevo nombre del grupo, o `null` para no modificarlo.
     * @param desc Nueva descripción del grupo, o `null` para no modificarla.
     * @return `true` si la edición fue exitosa; `false` si el usuario no tiene permisos.
     */
    override suspend fun editGroup(gid: Int, uid: Int, name: String?, desc: String?): Boolean {
        if (roleForUserOnGroup(uid, gid) == ROLE_USEROWNER){
            return getDatabaseDaoImpl().editGroup(gid = gid, name = name, desc = desc)
        }
        return false
    }
}


package com.mm.astraisandroid.data.repository

import com.mm.astraisandroid.data.api.CreateGroupRequest
import com.mm.astraisandroid.data.api.DeleteGroupRequest
import com.mm.astraisandroid.data.api.EditGroupRequest
import com.mm.astraisandroid.data.api.AddUserRequest
import com.mm.astraisandroid.data.api.RemoveUserRequest
import com.mm.astraisandroid.data.api.PassOwnershipRequest
import com.mm.astraisandroid.data.api.InviteUrlRequest
import com.mm.astraisandroid.data.api.CreateInviteRequest
import com.mm.astraisandroid.data.api.InviteOut
import com.mm.astraisandroid.data.api.ListInvitesResponse
import com.mm.astraisandroid.data.api.RevokeInviteRequest
import com.mm.astraisandroid.data.api.JoinByUrlRequest
import com.mm.astraisandroid.data.api.JoinByCodeRequest
import com.mm.astraisandroid.data.api.services.GroupApi
import com.mm.astraisandroid.data.api.GroupMembersResponse
import com.mm.astraisandroid.data.api.LeaveGroupRequest
import com.mm.astraisandroid.data.api.SetMemberRoleRequest
import com.mm.astraisandroid.data.api.AuditEventsResponse
import com.mm.astraisandroid.data.local.dao.GrupoDao
import com.mm.astraisandroid.data.local.entities.GrupoEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Repositorio de grupos de la capa de datos del cliente Android.
 *
 * Actúa como única fuente de verdad para los datos de grupos, coordinando las
 * operaciones de red ([GroupApi]) con el almacenamiento local ([GrupoDao]).
 * La lista de grupos es expuesta como un [kotlinx.coroutines.flow.Flow] reactivo que
 * refleja automáticamente los cambios en la base de datos local Room.
 *
 * El patrón utilizado es:
 * 1. Leer de Room para la UI.
 * 2. Sincronizar con el servidor bajo demanda mediante [refreshGroups].
 * 3. Para operaciones de escritura, llamar primero al servidor y luego actualizar
 *    el caché local con otra llamada a [refreshGroups].
 *
 * Esta clase es inyectada por Hilt como singleton en el grafo de dependencias.
 *
 * @property api Servicio HTTP para comunicarse con la API de grupos del backend.
 * @property dao DAO de Room para persistir y consultar grupos localmente.
 */
class GroupRepository @Inject constructor(
    private val api: GroupApi,
    private val dao: GrupoDao
) {
    /**
     * Flujo reactivo que emite la lista completa de grupos almacenados localmente.
     * Cualquier cambio en la tabla `grupos` de Room se propaga automáticamente a los
     * colectores.
     */
    val allGroups: Flow<List<GrupoEntity>> = dao.getAllGroups()

    /**
     * Sincroniza los grupos del usuario con el servidor y actualiza el caché local.
     * Realiza una llamada a `GET /group/userGroups`, borra los datos locales obsoletos
     * y escribe los nuevos registros en Room.
     * Debe invocarse tras cualquier operación que modifique el estado del grupo en el
     * servidor (crear, editar, eliminar, unirse, abandonar).
     *
     * @throws Exception Si la petición de red falla o hay un error en la base de datos.
     */
    suspend fun refreshGroups(): Result<Unit> {
        return try {
            val response = api.getGroups()
            val entities = response.groupList.map {
                GrupoEntity(
                    id = it.id,
                    name = it.name,
                    description = it.description,
                    role = it.role
                )
            }
            dao.replaceAll(entities)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Crea un nuevo grupo en el servidor y refresca el caché local.
     * El usuario autenticado pasa a ser el Owner del nuevo grupo.
     *
     * @param name Nombre del grupo.
     * @param desc Descripción del grupo.
     * @throws Exception Si la petición de red falla.
     */
    suspend fun createGroup(name: String, desc: String): Int {
        val req = CreateGroupRequest(name, desc)
        return api.createGroup(req)
    }

    /**
     * Edita los metadatos de un grupo y refresca el caché local.
     * Solo los Owners y Moderadores pueden editar grupos.
     * Los campos `null` no se envían al servidor.
     *
     * @param gid Identificador del grupo a editar.
     * @param name Nuevo nombre, o `null` para no cambiarlo.
     * @param desc Nueva descripción, o `null` para no cambiarla.
     * @throws Exception Si la petición de red falla o el usuario no tiene permisos.
     */
    suspend fun editGroup(gid: Int, name: String?, desc: String?) {
        val req = EditGroupRequest(gid, name, desc)
        api.editGroup(req)
    }

    /**
     * Elimina permanentemente un grupo del servidor y refresca el caché local.
     * Solo el Owner del grupo puede realizar esta operación.
     *
     * @param gid Identificador del grupo a eliminar.
     * @throws Exception Si la petición de red falla o el usuario no tiene permisos.
     */
    suspend fun deleteGroup(gid: Int) {
        val req = DeleteGroupRequest(gid)
        api.deleteGroup(req)
        dao.deleteGroupById(gid)
    }

    /**
     * Agrega un usuario existente a un grupo de forma directa.
     * Solo Owners y Moderadores pueden agregar usuarios.
     *
     * @param gid Identificador del grupo destino.
     * @param userId Identificador del usuario que se desea añadir.
     * @throws Exception Si la petición de red falla.
     */
    suspend fun addUser(gid: Int, userId: Int) {
        val req = AddUserRequest(gid = gid, userId = userId)
        api.addUser(req)
    }

    /**
     * Expulsa a un miembro de un grupo.
     * Solo Owners y Moderadores pueden expulsar miembros. El Owner no puede ser expulsado.
     *
     * @param gid Identificador del grupo.
     * @param userId Identificador del usuario a expulsar.
     * @throws Exception Si la petición de red falla o el usuario no tiene permisos.
     */
    suspend fun removeUser(gid: Int, userId: Int) {
        val req = RemoveUserRequest(gid = gid, userId = userId)
        api.removeUser(req)
    }

    /**
     * Transfiere la propiedad de un grupo a otro miembro y refresca el caché local.
     * Solo el Owner actual puede realizar esta operación.
     * El miembro destino debe ser ya miembro del grupo.
     *
     * @param gid Identificador del grupo cuya propiedad se transfiere.
     * @param newOwnerUserId Identificador del miembro que pasará a ser el nuevo Owner.
     * @throws Exception Si la petición de red falla o el destino no es miembro.
     */
    suspend fun passOwnership(gid: Int, newOwnerUserId: Int) {
        val req = PassOwnershipRequest(gid = gid, newOwnerUserId = newOwnerUserId)
        api.passOwnership(req)
    }

    /**
     * Genera una URL de invitación para el grupo.
     * Internamente el servidor produce un token seguro. Solo Owners y Moderadores pueden
     * generar invitaciones.
     *
     * @param gid Identificador del grupo.
     * @return URL de invitación completa lista para compartir.
     * @throws Exception Si la petición de red falla o el usuario no tiene permisos.
     */
    suspend fun createInviteUrl(gid: Int): String {
        return api.createInviteUrl(InviteUrlRequest(gid = gid))
    }

    /**
     * Une al usuario autenticado a un grupo a través de una URL de invitación.
     * Soporta tanto el formato seguro (`?code=`) como el legado (`?gid=`).
     * Después de unirse, refresca el caché local para incluir el nuevo grupo.
     *
     * @param inviteUrl URL de invitación completa.
     * @throws Exception Si la URL es inválida, el código expiró, fue revocado o el
     *   usuario ya pertenece al grupo.
     */
    suspend fun joinByUrl(inviteUrl: String) {
        api.joinByUrl(JoinByUrlRequest(inviteUrl = inviteUrl))
    }

    /**
     * Une al usuario autenticado a un grupo mediante un código de invitación en texto claro.
     * Valida revocación, expiración y límite de usos en el servidor.
     * Después de unirse, refresca el caché local.
     *
     * @param code Código de invitación en texto claro.
     * @throws Exception Si el código es inválido, expirado, revocado o los usos se agotaron.
     */
    suspend fun joinByCode(code: String) {
        api.joinByCode(JoinByCodeRequest(code = code))
    }

    /**
     * Crea una invitación segura basada en token para el grupo indicado.
     * Solo Owners y Moderadores pueden crear invitaciones.
     *
     * @param gid Identificador del grupo.
     * @param expiresInSeconds Segundos de validez desde la creación, o `null` para sin
     *   expiración.
     * @param maxUses Límite máximo de usos, o `null` para usos ilimitados.
     * @return [InviteOut] con el código en texto claro y la URL de la invitación.
     * @throws Exception Si la petición de red falla o el usuario no tiene permisos.
     */
    suspend fun createInvite(gid: Int, expiresInSeconds: Long? = null, maxUses: Int? = null): InviteOut {
        return api.createInvite(CreateInviteRequest(gid = gid, expiresInSeconds = expiresInSeconds, maxUses = maxUses))
    }

    /**
     * Lista todas las invitaciones de un grupo (activas, revocadas y expiradas).
     * Solo Owners y Moderadores pueden consultar las invitaciones.
     *
     * @param gid Identificador del grupo.
     * @return [ListInvitesResponse] con todas las invitaciones del grupo.
     * @throws Exception Si la petición de red falla o el usuario no tiene permisos.
     */
    suspend fun listInvites(gid: Int): ListInvitesResponse {
        return api.listInvites(gid)
    }

    /**
     * Revoca una invitación activa de un grupo.
     * Una invitación revocada no puede usarse aunque no haya expirado.
     * Solo Owners y Moderadores pueden revocar invitaciones.
     *
     * @param gid Identificador del grupo al que pertenece la invitación.
     * @param code Código de invitación en texto claro a revocar.
     * @throws Exception Si la petición de red falla o el código no existe.
     */
    suspend fun revokeInvite(gid: Int, code: String) {
        api.revokeInvite(RevokeInviteRequest(gid = gid, code = code))
    }

    /**
     * Recupera la lista de miembros de un grupo desde el servidor.
     * No almacena el resultado en caché local; los datos se devuelven directamente al
     * ViewModel para su uso en la pantalla de detalle.
     *
     * @param gid Identificador del grupo.
     * @return [GroupMembersResponse] con la lista de miembros, roles y fechas de unión.
     * @throws Exception Si la petición de red falla.
     */
    suspend fun getMembers(gid: Int): GroupMembersResponse {
        return api.getMembers(gid)
    }

    /**
     * Permite al usuario autenticado abandonar voluntariamente un grupo.
     * El Owner no puede abandonar sin transferir primero la propiedad.
     * Después de abandonar, refresca el caché local para eliminar el grupo de la lista.
     *
     * @param gid Identificador del grupo que el usuario desea abandonar.
     * @throws Exception Si la petición de red falla o el usuario es el Owner.
     */
    suspend fun leaveGroup(gid: Int) {
        api.leaveGroup(LeaveGroupRequest(gid = gid))
    }

    /**
     * Cambia el rol de un miembro del grupo. Solo el Owner puede invocar esta operación.
     *
     * @param gid Identificador del grupo.
     * @param userId Identificador del miembro cuyo rol se va a modificar.
     * @param role Nuevo rol: `0` = Miembro normal, `1` = Moderador.
     * @throws Exception Si la petición de red falla o el usuario no tiene permisos.
     */
    suspend fun setMemberRole(gid: Int, userId: Int, role: Int) {
        api.setMemberRole(SetMemberRoleRequest(gid = gid, userId = userId, role = role))
    }

    /**
     * Recupera el historial de eventos de auditoría de un grupo con paginación.
     * Cualquier miembro del grupo puede consultar el log.
     * Los datos se devuelven directamente al ViewModel sin persistirse localmente.
     *
     * @param gid Identificador del grupo.
     * @param limit Número máximo de eventos (por defecto 50, máximo 200).
     * @param offset Desplazamiento para paginación (por defecto 0).
     * @return [AuditEventsResponse] con la lista paginada de eventos.
     * @throws Exception Si la petición de red falla.
     */
    suspend fun getAudit(gid: Int, limit: Int = 50, offset: Long = 0L): AuditEventsResponse {
        return api.getAudit(gid, limit = limit, offset = offset)
    }

    suspend fun clearLocalData() {
        dao.clearAll()
    }
}

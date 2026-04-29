package com.astrais.groups

import groups.types.*

/**
 * Resultado posible de la operación [GroupRepo.addUser].
 *
 * @property OK El usuario fue agregado correctamente al grupo.
 * @property NOGROUP El grupo con el GID proporcionado no existe.
 * @property ALREADYJOINED El usuario ya es miembro del grupo.
 * @property NOPERMISSION El solicitante no tiene rol suficiente (debe ser Owner o Mod).
 * @property CONNERR Error de base de datos o de conectividad interna.
 */
enum class AddUserReturn {
    OK,
    NOGROUP,
    ALREADYJOINED,
    NOPERMISSION,
    CONNERR
}

/**
 * Resultado posible de la operación [GroupRepo.removeUser].
 *
 * @property OK El usuario fue expulsado correctamente.
 * @property NOGROUP El grupo con el GID proporcionado no existe.
 * @property NOTMEMBER El usuario objetivo no pertenece al grupo.
 * @property NOPERMISSION El solicitante no tiene rol suficiente para expulsar miembros.
 * @property CANNOT_REMOVE_OWNER El Owner del grupo no puede ser expulsado; debe transferir
 *   la propiedad primero.
 * @property CONNERR Error de base de datos o de conectividad interna.
 */
enum class RemoveUserReturn {
    OK,
    NOGROUP,
    NOTMEMBER,
    NOPERMISSION,
    CANNOT_REMOVE_OWNER,
    CONNERR
}

/**
 * Resultado posible de la operación [GroupRepo.passOwnership].
 *
 * @property OK La propiedad fue transferida correctamente al nuevo Owner.
 * @property NOGROUP El grupo con el GID proporcionado no existe.
 * @property NOPERMISSION Solo el Owner actual puede transferir la propiedad.
 * @property TARGETNOTMEMBER El usuario destino no es miembro del grupo.
 * @property SAMEOWNER El usuario destino ya es el Owner actual.
 * @property CONNERR Error de base de datos o de conectividad interna.
 */
enum class PassOwnershipReturn {
    OK,
    NOGROUP,
    NOPERMISSION,
    TARGETNOTMEMBER,
    SAMEOWNER,
    CONNERR
}

/**
 * Resultado posible de las operaciones relacionadas con invitaciones de grupo
 * ([GroupRepo.generateInviteUrl], [GroupRepo.joinByInviteUrl], [GroupRepo.createInvite],
 * [GroupRepo.revokeInvite] y [GroupRepo.joinByCode]).
 *
 * @property OK La operación de invitación fue exitosa.
 * @property NOGROUP El grupo con el GID proporcionado no existe.
 * @property NOPERMISSION El solicitante carece de permisos (grupos personales o rol insuficiente).
 * @property INVALID_URL La URL de invitación proporcionada no tiene formato válido.
 * @property INVALID_CODE El código de invitación no existe o no pudo localizarse.
 * @property EXPIRED El código de invitación ha superado su fecha de expiración.
 * @property REVOKED El código de invitación fue revocado manualmente.
 * @property MAX_USES_REACHED El código ha alcanzado el límite máximo de usos permitidos.
 * @property ALREADYJOINED El usuario ya pertenece al grupo.
 * @property CONNERR Error de base de datos o de conectividad interna.
 */
enum class InviteUrlReturn {
    OK,
    NOGROUP,
    NOPERMISSION,
    INVALID_URL,
    INVALID_CODE,
    EXPIRED,
    REVOKED,
    MAX_USES_REACHED,
    ALREADYJOINED,
    CONNERR
}

/**
 * Resultado posible de las operaciones de listado de miembros y auditoría
 * ([GroupRepo.listMembers] y [GroupRepo.listAudit]).
 *
 * @property OK Los datos fueron recuperados correctamente.
 * @property NOGROUP El grupo con el GID proporcionado no existe.
 * @property NOPERMISSION El solicitante no pertenece al grupo o es un grupo personal.
 * @property CONNERR Error de base de datos o de conectividad interna.
 */
enum class MemberListReturn {
    OK,
    NOGROUP,
    NOPERMISSION,
    CONNERR
}

/**
 * Resultado posible de la operación [GroupRepo.leaveGroup].
 *
 * @property OK El usuario abandonó el grupo correctamente.
 * @property NOGROUP El grupo con el GID proporcionado no existe.
 * @property NOTMEMBER El usuario no es miembro del grupo.
 * @property OWNER_CANNOT_LEAVE El Owner no puede abandonar el grupo; debe transferir la
 *   propiedad a otro miembro antes de salir.
 * @property CONNERR Error de base de datos o de conectividad interna.
 */
enum class LeaveGroupReturn {
    OK,
    NOGROUP,
    NOTMEMBER,
    OWNER_CANNOT_LEAVE,
    CONNERR
}

/**
 * Resultado posible de la operación [GroupRepo.setMemberRole].
 *
 * @property OK El rol del miembro fue actualizado correctamente.
 * @property NOGROUP El grupo con el GID proporcionado no existe.
 * @property NOTMEMBER El usuario objetivo no pertenece al grupo.
 * @property NOPERMISSION Solo el Owner puede cambiar roles de otros miembros.
 * @property INVALID_ROLE El entero de rol proporcionado no corresponde a ningún rol válido,
 *   o se intentó cambiar el rol del propio Owner.
 * @property CONNERR Error de base de datos o de conectividad interna.
 */
enum class SetMemberRoleReturn {
    OK,
    NOGROUP,
    NOTMEMBER,
    NOPERMISSION,
    INVALID_ROLE,
    CONNERR
}

/**
 * Interfaz del repositorio de grupos de Astrais.
 *
 * Define todas las operaciones disponibles sobre grupos: creación, gestión de miembros,
 * sistema de invitaciones con tokens seguros, transferencia de propiedad y auditoría.
 * Las implementaciones concretas (p. ej. [GroupRepoImpl]) realizan las consultas
 * contra la base de datos a través del DAO centralizado.
 *
 * Todas las funciones son suspendidas y deben invocarse desde una corrutina (Elías por favor hazlo).
 */
interface GroupRepo {
    /**
     * Devuelve una lista de grupos del usuario con el ID dado.
     * @param userId El ID del usuario
     */
    suspend fun getAllUserGroups(userId : Int) : List<SingleGroupOut>

    /**
     * Crea un grupo cerrado nuevo.
     * @param name Nombre del grupo
     * @param desc Descripcion del grupo
     * @return El ID del grupo
     */
    suspend fun createGroup(name : String, desc : String, ownerId : Int) : Int

    /**
     * Añade un usuario al grupo
     * @param requesterId El ID del usuario que pide que se agregue
     * @param userId El ID del usuario a intentar agregar
     * @param gid El ID del grupo a agregar
     */
    suspend fun addUser(requesterId : Int, userId: Int, gid : Int) : AddUserReturn

    /**
     * Elimina un usuario de un grupo.
     * @param requesterId El ID del usuario que pide la eliminacion
     * @param userId El ID del usuario a eliminar
     * @param gid El ID del grupo
     */
    suspend fun removeUser(requesterId: Int, userId: Int, gid: Int): RemoveUserReturn

    /**
     * Transfiere la propiedad del grupo a otro usuario.
     * @param requesterId El ID del usuario que intenta transferir
     * @param newOwnerUserId El ID del nuevo owner
     * @param gid El ID del grupo
     */
    suspend fun passOwnership(requesterId: Int, newOwnerUserId: Int, gid: Int): PassOwnershipReturn

    /**
     * Genera una URL de invitacion para un grupo.
     */
    suspend fun generateInviteUrl(requesterId: Int, gid: Int): Pair<InviteUrlReturn, String?>

    /**
     * Une al usuario autenticado a un grupo a traves de una URL.
     */
    suspend fun joinByInviteUrl(requesterId: Int, inviteUrl: String): InviteUrlReturn

    /**
     * Genera una invitacion para un grupo.
     */
    suspend fun createInvite(requesterId: Int, gid: Int, expiresInSeconds: Long?, maxUses: Int?): Pair<InviteUrlReturn, InviteOut?>

    /**
     * Lista todas las invitaciones activas, revocadas o expiradas de un grupo.
     * Solo accesible por Owner o Moderador.
     *
     * @param requesterId El ID del usuario que solicita el listado.
     * @param gid El ID del grupo cuyas invitaciones se quieren listar.
     * @return Par con el código de resultado y la lista de [InviteOut], o `null` si falla.
     */
    suspend fun listInvites(requesterId: Int, gid: Int): Pair<InviteUrlReturn, List<InviteOut>?>

    /**
     * Revoca una invitación activa identificada por su código.
     * Una invitación revocada no puede ser utilizada aunque no haya expirado.
     * Solo accesible por Owner o Moderador.
     *
     * @param requesterId El ID del usuario que solicita la revocación.
     * @param gid El ID del grupo al que pertenece la invitación.
     * @param code El código de invitación (se hashea internamente con SHA-256).
     * @return El resultado de la operación ([InviteUrlReturn]).
     */
    suspend fun revokeInvite(requesterId: Int, gid: Int, code: String): InviteUrlReturn

    /**
     * Une al usuario autenticado a un grupo mediante un código de invitación .
     * Valida la revocación, expiración y límite de usos antes de consumir el código.
     *
     * @param requesterId El ID del usuario que quiere unirse.
     * @param code El código de invitación.
     * @return El resultado de la operación ([InviteUrlReturn]).
     */
    suspend fun joinByCode(requesterId: Int, code: String): InviteUrlReturn

    /**
     * Lista todos los miembros de un grupo, incluyendo su nombre, rol y fecha de incorporación.
     * Cualquier miembro del grupo puede consultar la lista.
     *
     * @param requesterId El ID del usuario que realiza la consulta.
     * @param gid El ID del grupo.
     * @return Par con el código de resultado y la lista de [GroupMemberOut], o `null` si falla.
     */
    suspend fun listMembers(requesterId: Int, gid: Int): Pair<MemberListReturn, List<GroupMemberOut>?>

    /**
     * Permite a un miembro abandonar voluntariamente un grupo.
     * El Owner no puede usar esta operación; debe transferir la propiedad primero.
     * Registra el evento en el log de auditoría del grupo.
     *
     * @param requesterId El ID del usuario que quiere abandonar el grupo.
     * @param gid El ID del grupo.
     * @return El resultado de la operación ([LeaveGroupReturn]).
     */
    suspend fun leaveGroup(requesterId: Int, gid: Int): LeaveGroupReturn

    /**
     * Cambia el rol de un miembro dentro de un grupo. Solo el Owner puede realizar esta acción.
     * Los roles válidos son [ROLE_USERNORMAL] (0) y [ROLE_USERMOD] (1).
     * Registra el cambio en el log de auditoría del grupo.
     *
     * @param requesterId El ID del Owner que solicita el cambio.
     * @param gid El ID del grupo.
     * @param userId El ID del miembro cuyo rol se va a modificar.
     * @param role El nuevo rol expresado como entero.
     * @return El resultado de la operación ([SetMemberRoleReturn]).
     */
    suspend fun setMemberRole(requesterId: Int, gid: Int, userId: Int, role: Int): SetMemberRoleReturn

    /**
     * Recupera el historial de eventos de auditoría de un grupo con paginación.
     * Cualquier miembro del grupo puede consultar el log.
     *
     * @param requesterId El ID del usuario que solicita el log.
     * @param gid El ID del grupo.
     * @param limit Número máximo de eventos a devolver (máximo 200).
     * @param offset Desplazamiento para paginación.
     * @return Par con el código de resultado y la lista de [AuditEventOut], o `null` si falla.
     */
    suspend fun listAudit(requesterId: Int, gid: Int, limit: Int, offset: Long): Pair<MemberListReturn, List<AuditEventOut>?>

    /**
     * Se borra el grupo entero
     * @param gid El ID del grupo
     * @param uid El ID del usuario que quiere borrarlo
     * @return False si no tiene permisos
     */
    suspend fun deleteGroup(gid : Int, uid : Int) : Boolean

    /**
     * Se edita un grupo
     * @param gid El ID del grupo
     * @param uid El ID del usuario que quiere editarlo
     * @param name El nuevo nombre, NULL si no se quiere cambiar
     * @param desc La nueva descripcion, NULL si no se quiere cambiar
     */
    suspend fun editGroup(gid : Int, uid : Int, name: String? = null, desc: String? = null) : Boolean
}

/**
 * Devuelve la implementación activa de [GroupRepo].
 * En producción retorna una instancia de [GroupRepoImpl].
 *
 * @return La implementación concreta de [GroupRepo].
 */
fun getGroupRepoImpl() : GroupRepo{
    return groupRepoProvider()
}

/**
 * Test.
 */
var groupRepoProvider: () -> GroupRepo = { GroupRepoImpl() }
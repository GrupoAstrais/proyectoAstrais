package com.astrais.groups

import groups.types.*
import com.astrais.ErrorCodes
import com.astrais.Errors
import OK_MESSAGE_RESPONSE
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * Registra todas las rutas HTTP relacionadas con la gestión de grupos de Astrais.
 *
 * Las rutas públicas (sin autenticación) se limitan al puente de redirección de invitaciones
 * legadas. El resto de rutas requiere un JWT de acceso válido (`access-jwt`) y extrae el
 * UID del usuario del subject del token.
 *
 * Rutas registradas:
 * - `GET  /groups/redirectInvite`      — Redirección de invitación legada (público).
 * - `GET  /group/userGroups`           — Lista los grupos del usuario autenticado.
 * - `POST /groups/createGroup`         — Crea un nuevo grupo.
 * - `POST /groups/addUser`             — Agrega un usuario a un grupo.
 * - `POST /groups/removeUser`          — Expulsa a un usuario de un grupo.
 * - `POST /groups/inviteUrl`           — Genera una URL de invitación.
 * - `POST /groups/joinByUrl`           — Une al usuario a un grupo por URL de invitación.
 * - `POST /groups/joinByCode`          — Une al usuario a un grupo por código de invitación.
 * - `POST /groups/invites`             — Crea una invitación segura con token.
 * - `GET  /groups/{gid}/invites`       — Lista las invitaciones de un grupo.
 * - `POST /groups/invites/revoke`      — Revoca una invitación activa.
 * - `GET  /groups/{gid}/members`       — Lista los miembros de un grupo.
 * - `POST /groups/leave`              — El usuario abandona un grupo.
 * - `PATCH /groups/setMemberRole`      — Cambia el rol de un miembro.
 * - `GET  /groups/{gid}/audit`         — Recupera el log de auditoría de un grupo.
 * - `PATCH /groups/editGroup`          — Edita los metadatos de un grupo.
 * - `PATCH /groups/passOwnership`      — Transfiere la propiedad de un grupo.
 * - `DELETE /groups/deleteGroup`       — Elimina permanentemente un grupo.
 */
fun Route.groupRoutes(){
    /**
     * Puente de compatibilidad para URLs de invitación legadas (HAY QUE BORRAR ESTO MANUEEEEL).
     * Acepta los parámetros de consulta `code` o `gid` y emite una redirección HTTP 302
     * hacia la URL configurada en `INVITE_BASE_URL`.
     * Este endpoint es público y no requiere autenticación.
     *
     * Parámetros de consulta:
     * - `code` — Código de invitación seguro (tiene prioridad sobre `gid`).
     * - `gid`  — Identificador de grupo del flujo legado.
     *
     * Respuestas:
     * - `302 Found`       — Redirección a la URL.
     * - `400 Bad Request` — Ninguno de los parámetros esperados está presente.
     */
    get("/groups/redirectInvite") {
        val code = call.request.queryParameters["code"]?.trim()
        val gid = call.request.queryParameters["gid"]?.trim()
        val base = (System.getenv("INVITE_BASE_URL")?.trim()?.removeSuffix("/"))
            ?: "https://astrais.app/groups/join"

        val target = when {
            !code.isNullOrBlank() -> "$base?code=$code"
            !gid.isNullOrBlank() -> "$base?gid=$gid"
            else -> null
        }

        if (target == null) {
            call.respond(
                HttpStatusCode.BadRequest,
                Errors(ErrorCodes.ERR_BADVALUE.ordinal, "Missing invite code/gid")
            )
            return@get
        }

        call.respondRedirect(target, permanent = false)
    }

    authenticate("access-jwt") {
        /**
         * Devuelve la lista de grupos (no personales) a los que pertenece el usuario autenticado.
         * Para cada grupo indica el rol del usuario (Owner, Moderador o Miembro).
         *
         * Respuestas:
         * - `200 OK`          — [AllGroupsResponse] con la lista de grupos.
         * - `400 Bad Request`  — El UID extraído del token no es válido.
         */
        get("/group/userGroups") {
            val token = call.principal<JWTPrincipal>()
            val uid = token?.subject?.toInt() ?: -1

            if (uid != -1){
                val out = getGroupRepoImpl().getAllUserGroups(uid)
                call.respond(HttpStatusCode.OK, AllGroupsResponse(out))
            }else {
                call.respond(HttpStatusCode.BadRequest, Errors(ErrorCodes.ERR_BADVALUE.ordinal, "Invalid UID"))
            }
        }

        /**
         * Crea un nuevo grupo con el usuario autenticado como Owner.
         * El cuerpo de la petición debe ser un [CreateGroupRequest] con `name` y `desc`.
         *
         * Respuestas:
         * - `200 OK`      — JSON `{"groupId": <Int>}` con el GID del grupo creado.
         * - `400 Bad Request` — UID inválido en el token.
         * - `409 Conflict`    — El grupo no pudo ser creado (error interno).
         */
        post("/groups/createGroup") {
            val token = call.principal<JWTPrincipal>()
            val uid = token?.subject?.toInt() ?: -1
            if (uid == -1){
                call.respond(HttpStatusCode.BadRequest, Errors(ErrorCodes.ERR_BADVALUE.ordinal, "Invalid UID"))
                return@post
            }

            val req = call.receive<CreateGroupRequest>()
            val gid = getGroupRepoImpl().createGroup(req.name, req.desc, uid)
            if (gid != -1){
                call.respond(HttpStatusCode.OK, mapOf("groupId" to gid))
            }else{
                call.respond(HttpStatusCode.Conflict, Errors(ErrorCodes.ERR_RESOURCENOTCREATED.ordinal, "Can't create the group"))
            }
        }

        /**
         * Agrega un usuario existente a un grupo de forma directa.
         * Solo Owners y Moderadores pueden invocar esta operación.
         * El cuerpo de la petición debe ser un [AddUserRequest] con `gid` y `userId`.
         *
         * Respuestas:
         * - `200 OK`        — Operación exitosa.
         * - `401 Unauthorized` — UID inválido en el token.
         * - `403 Forbidden`    — El solicitante no tiene permisos suficientes.
         * - `404 Not Found`    — El grupo no existe.
         * - `400 Bad Request`  — El usuario ya es miembro del grupo.
         * - `500 Internal Server Error` — Error de base de datos.
         */
        post("/groups/addUser") {
            val token = call.principal<JWTPrincipal>()
            val uid = token?.subject?.toInt() ?: -1
            if (uid == -1){
                call.respond(HttpStatusCode.Unauthorized, Errors(ErrorCodes.ERR_BADVALUE.ordinal, "Invalid UID"))
                return@post
            }

            val data = call.receive<AddUserRequest>()
            val ret = getGroupRepoImpl().addUser(uid, data.userId, data.gid)

            when (ret) {
                AddUserReturn.OK -> call.respond(HttpStatusCode.OK, OK_MESSAGE_RESPONSE)
                AddUserReturn.NOGROUP -> call.respond(HttpStatusCode.NotFound, Errors(ErrorCodes.ERR_RESOURCEMISSING.ordinal, "GID invalid"))
                AddUserReturn.ALREADYJOINED -> call.respond(HttpStatusCode.BadRequest, Errors(ErrorCodes.ERR_RESOURCEALREADYEXISTS.ordinal, "User already joined the group"))
                AddUserReturn.NOPERMISSION -> call.respond(HttpStatusCode.Forbidden, Errors(ErrorCodes.ERR_FORBIDDEN.ordinal, "User doesn't have the permission to add people"))
                AddUserReturn.CONNERR -> call.respond(HttpStatusCode.InternalServerError, Errors(ErrorCodes.ERR_INTERNALERROR.ordinal, "Error with the database"))
            }
        }

        /**
         * Expulsa a un miembro de un grupo.
         * El Owner no puede ser expulsado; solo Owners y Moderadores pueden expulsar a otros.
         * El cuerpo de la petición debe ser un [RemoveUserRequest] con `gid` y `userId`.
         *
         * Respuestas:
         * - `200 OK`        — Miembro expulsado correctamente.
         * - `401 Unauthorized` — UID inválido en el token.
         * - `403 Forbidden`    — Sin permisos o intento de expulsar al Owner.
         * - `404 Not Found`    — Grupo inexistente o usuario no es miembro.
         * - `500 Internal Server Error` — Error de base de datos.
         */
        post("/groups/removeUser") {
            val token = call.principal<JWTPrincipal>()
            val uid = token?.subject?.toInt() ?: -1
            if (uid == -1) {
                call.respond(HttpStatusCode.Unauthorized, Errors(ErrorCodes.ERR_BADVALUE.ordinal, "Invalid UID"))
                return@post
            }

            val data = call.receive<RemoveUserRequest>()
            val ret = getGroupRepoImpl().removeUser(uid, data.userId, data.gid)

            when (ret) {
                RemoveUserReturn.OK -> call.respond(HttpStatusCode.OK, OK_MESSAGE_RESPONSE)
                RemoveUserReturn.NOGROUP -> call.respond(HttpStatusCode.NotFound, Errors(ErrorCodes.ERR_RESOURCEMISSING.ordinal, "GID invalid"))
                RemoveUserReturn.NOTMEMBER -> call.respond(HttpStatusCode.NotFound, Errors(ErrorCodes.ERR_RESOURCEMISSING.ordinal, "User is not member of that group"))
                RemoveUserReturn.NOPERMISSION -> call.respond(HttpStatusCode.Forbidden, Errors(ErrorCodes.ERR_FORBIDDEN.ordinal, "User doesn't have the permission to remove people"))
                RemoveUserReturn.CANNOT_REMOVE_OWNER -> call.respond(HttpStatusCode.BadRequest, Errors(ErrorCodes.ERR_BADVALUE.ordinal, "Owner cannot be removed from group"))
                RemoveUserReturn.CONNERR -> call.respond(HttpStatusCode.InternalServerError, Errors(ErrorCodes.ERR_INTERNALERROR.ordinal, "Error with the database"))
            }
        }

        /**
         * Genera una URL de invitación para un grupo.
         * Internamente delega en [GroupRepo.createInvite] para producir un token seguro.
         * El cuerpo de la petición debe ser un [InviteUrlRequest] con `gid`.
         *
         * Respuestas:
         * - `200 OK`        — [InviteUrlResponse] con la URL lista para compartir.
         * - `401 Unauthorized` — UID inválido en el token.
         * - `403 Forbidden`    — Sin permisos o grupo personal.
         * - `404 Not Found`    — Grupo inexistente.
         * - `400 Bad Request`  — Código inválido, expirado, revocado o usos agotados.
         * - `500 Internal Server Error` — Error de base de datos.
         */
        post("/groups/inviteUrl") {
            val token = call.principal<JWTPrincipal>()
            val uid = token?.subject?.toInt() ?: -1
            if (uid == -1) {
                call.respond(HttpStatusCode.Unauthorized, Errors(ErrorCodes.ERR_BADVALUE.ordinal, "Invalid UID"))
                return@post
            }

            val data = call.receive<InviteUrlRequest>()
            // Backward compatible: now returns a secure token-based invite URL
            val (ret, invite) = getGroupRepoImpl().createInvite(uid, data.gid, expiresInSeconds = null, maxUses = null)
            when (ret) {
                InviteUrlReturn.OK -> call.respond(HttpStatusCode.OK, InviteUrlResponse(inviteUrl = invite!!.inviteUrl))
                InviteUrlReturn.NOGROUP -> call.respond(HttpStatusCode.NotFound, Errors(ErrorCodes.ERR_RESOURCEMISSING.ordinal, "GID invalid"))
                InviteUrlReturn.NOPERMISSION -> call.respond(HttpStatusCode.Forbidden, Errors(ErrorCodes.ERR_FORBIDDEN.ordinal, "No permission to generate invite URL"))
                InviteUrlReturn.INVALID_URL -> call.respond(HttpStatusCode.BadRequest, Errors(ErrorCodes.ERR_BADVALUE.ordinal, "Invalid URL"))
                InviteUrlReturn.INVALID_CODE -> call.respond(HttpStatusCode.BadRequest, Errors(ErrorCodes.ERR_BADVALUE.ordinal, "Invalid invite code"))
                InviteUrlReturn.EXPIRED -> call.respond(HttpStatusCode.BadRequest, Errors(ErrorCodes.ERR_BADVALUE.ordinal, "Invite expired"))
                InviteUrlReturn.REVOKED -> call.respond(HttpStatusCode.BadRequest, Errors(ErrorCodes.ERR_BADVALUE.ordinal, "Invite revoked"))
                InviteUrlReturn.MAX_USES_REACHED -> call.respond(HttpStatusCode.BadRequest, Errors(ErrorCodes.ERR_BADVALUE.ordinal, "Invite max uses reached"))
                InviteUrlReturn.ALREADYJOINED -> call.respond(HttpStatusCode.BadRequest, Errors(ErrorCodes.ERR_RESOURCEALREADYEXISTS.ordinal, "User already joined the group"))
                InviteUrlReturn.CONNERR -> call.respond(HttpStatusCode.InternalServerError, Errors(ErrorCodes.ERR_INTERNALERROR.ordinal, "Error with the database"))
            }
        }

        /**
         * Une al usuario autenticado a un grupo a través de una URL de invitación.
         * Soporta tanto el formato legado (`?gid=`) como el formato seguro (`?code=`).
         * El cuerpo de la petición debe ser un [JoinByUrlRequest] con `inviteUrl`.
         *
         * Respuestas:
         * - `200 OK`           — El usuario se unió correctamente al grupo.
         * - `401 Unauthorized` — UID inválido en el token.
         * - `403 Forbidden`    — Sin permisos o grupo personal.
         * - `404 Not Found`    — Grupo inexistente.
         * - `400 Bad Request`  — URL inválida, código inválido, expirado, revocado, usos
         *   agotados, o usuario ya miembro.
         * - `500 Internal Server Error` — Error de base de datos.
         */
        post("/groups/joinByUrl") {
            val token = call.principal<JWTPrincipal>()
            val uid = token?.subject?.toInt() ?: -1
            if (uid == -1) {
                call.respond(HttpStatusCode.Unauthorized, Errors(ErrorCodes.ERR_BADVALUE.ordinal, "Invalid UID"))
                return@post
            }

            val data = call.receive<JoinByUrlRequest>()
            val ret = getGroupRepoImpl().joinByInviteUrl(uid, data.inviteUrl)
            when (ret) {
                InviteUrlReturn.OK -> call.respond(HttpStatusCode.OK, OK_MESSAGE_RESPONSE)
                InviteUrlReturn.NOGROUP -> call.respond(HttpStatusCode.NotFound, Errors(ErrorCodes.ERR_RESOURCEMISSING.ordinal, "GID invalid"))
                InviteUrlReturn.NOPERMISSION -> call.respond(HttpStatusCode.Forbidden, Errors(ErrorCodes.ERR_FORBIDDEN.ordinal, "Cannot join this group with URL"))
                InviteUrlReturn.INVALID_URL -> call.respond(HttpStatusCode.BadRequest, Errors(ErrorCodes.ERR_BADVALUE.ordinal, "Invalid invite URL"))
                InviteUrlReturn.INVALID_CODE -> call.respond(HttpStatusCode.BadRequest, Errors(ErrorCodes.ERR_BADVALUE.ordinal, "Invalid invite code"))
                InviteUrlReturn.EXPIRED -> call.respond(HttpStatusCode.BadRequest, Errors(ErrorCodes.ERR_BADVALUE.ordinal, "Invite expired"))
                InviteUrlReturn.REVOKED -> call.respond(HttpStatusCode.BadRequest, Errors(ErrorCodes.ERR_BADVALUE.ordinal, "Invite revoked"))
                InviteUrlReturn.MAX_USES_REACHED -> call.respond(HttpStatusCode.BadRequest, Errors(ErrorCodes.ERR_BADVALUE.ordinal, "Invite max uses reached"))
                InviteUrlReturn.ALREADYJOINED -> call.respond(HttpStatusCode.BadRequest, Errors(ErrorCodes.ERR_RESOURCEALREADYEXISTS.ordinal, "User already joined the group"))
                InviteUrlReturn.CONNERR -> call.respond(HttpStatusCode.InternalServerError, Errors(ErrorCodes.ERR_INTERNALERROR.ordinal, "Error with the database"))
            }
        }

        /**
         * Une al usuario autenticado a un grupo mediante un código de invitación.
         * Valida revocación, expiración y límite de usos antes de consumir el código.
         * El cuerpo de la petición debe ser un [JoinByCodeRequest] con `code`.
         *
         * Respuestas:
         * - `200 OK`           — El usuario se unió correctamente al grupo.
         * - `401 Unauthorized` — UID inválido en el token.
         * - `403 Forbidden`    — Intento de unirse a un grupo personal.
         * - `404 Not Found`    — Grupo inexistente.
         * - `400 Bad Request`  — Código inválido, expirado, revocado, usos agotados, o usuario
         *   ya miembro.
         * - `500 Internal Server Error` — Error de base de datos.
         */
        post("/groups/joinByCode") {
            val token = call.principal<JWTPrincipal>()
            val uid = token?.subject?.toInt() ?: -1
            if (uid == -1) {
                call.respond(HttpStatusCode.Unauthorized, Errors(ErrorCodes.ERR_BADVALUE.ordinal, "Invalid UID"))
                return@post
            }

            val data = call.receive<JoinByCodeRequest>()
            val ret = getGroupRepoImpl().joinByCode(uid, data.code)
            when (ret) {
                InviteUrlReturn.OK -> call.respond(HttpStatusCode.OK, OK_MESSAGE_RESPONSE)
                InviteUrlReturn.NOGROUP -> call.respond(HttpStatusCode.NotFound, Errors(ErrorCodes.ERR_RESOURCEMISSING.ordinal, "GID invalid"))
                InviteUrlReturn.NOPERMISSION -> call.respond(HttpStatusCode.Forbidden, Errors(ErrorCodes.ERR_FORBIDDEN.ordinal, "Cannot join this group"))
                InviteUrlReturn.INVALID_CODE -> call.respond(HttpStatusCode.BadRequest, Errors(ErrorCodes.ERR_BADVALUE.ordinal, "Invalid invite code"))
                InviteUrlReturn.EXPIRED -> call.respond(HttpStatusCode.BadRequest, Errors(ErrorCodes.ERR_BADVALUE.ordinal, "Invite expired"))
                InviteUrlReturn.REVOKED -> call.respond(HttpStatusCode.BadRequest, Errors(ErrorCodes.ERR_BADVALUE.ordinal, "Invite revoked"))
                InviteUrlReturn.MAX_USES_REACHED -> call.respond(HttpStatusCode.BadRequest, Errors(ErrorCodes.ERR_BADVALUE.ordinal, "Invite max uses reached"))
                InviteUrlReturn.ALREADYJOINED -> call.respond(HttpStatusCode.BadRequest, Errors(ErrorCodes.ERR_RESOURCEALREADYEXISTS.ordinal, "User already joined the group"))
                else -> call.respond(HttpStatusCode.InternalServerError, Errors(ErrorCodes.ERR_INTERNALERROR.ordinal, "Error with the database"))
            }
        }

        /**
         * Crea una invitación segura basada en token para el grupo indicado.
         * Permite especificar expiración y límite de usos.
         * Solo Owners y Moderadores pueden crear invitaciones.
         * El cuerpo de la petición debe ser un [CreateInviteRequest].
         *
         * Respuestas:
         * - `200 OK`        — [InviteOut] con el código y la URL de la invitación creada.
         * - `401 Unauthorized` — UID inválido en el token.
         * - `403 Forbidden`    — Sin permisos o grupo personal.
         * - `404 Not Found`    — Grupo inexistente.
         * - `500 Internal Server Error` — Error de base de datos.
         */
        post("/groups/invites") {
            val token = call.principal<JWTPrincipal>()
            val uid = token?.subject?.toInt() ?: -1
            if (uid == -1) {
                call.respond(HttpStatusCode.Unauthorized, Errors(ErrorCodes.ERR_BADVALUE.ordinal, "Invalid UID"))
                return@post
            }

            val data = call.receive<CreateInviteRequest>()
            val (ret, invite) = getGroupRepoImpl().createInvite(uid, data.gid, data.expiresInSeconds, data.maxUses)
            when (ret) {
                InviteUrlReturn.OK -> call.respond(HttpStatusCode.OK, invite!!)
                InviteUrlReturn.NOGROUP -> call.respond(HttpStatusCode.NotFound, Errors(ErrorCodes.ERR_RESOURCEMISSING.ordinal, "GID invalid"))
                InviteUrlReturn.NOPERMISSION -> call.respond(HttpStatusCode.Forbidden, Errors(ErrorCodes.ERR_FORBIDDEN.ordinal, "No permission to create invite"))
                else -> call.respond(HttpStatusCode.InternalServerError, Errors(ErrorCodes.ERR_INTERNALERROR.ordinal, "Error with the database"))
            }
        }

        /**
         * Lista todas las invitaciones de un grupo (activas, revocadas y expiradas).
         * Solo Owners y Moderadores pueden consultar las invitaciones.
         *
         * Parámetros de ruta:
         * - `gid` — Identificador del grupo.
         *
         * Respuestas:
         * - `200 OK`        — [ListInvitesResponse] con la lista de invitaciones.
         * - `401 Unauthorized` — UID inválido en el token.
         * - `400 Bad Request`  — GID no es un entero válido.
         * - `403 Forbidden`    — Sin permisos.
         * - `404 Not Found`    — Grupo inexistente.
         * - `500 Internal Server Error` — Error de base de datos.
         */
        get("/groups/{gid}/invites") {
            val token = call.principal<JWTPrincipal>()
            val uid = token?.subject?.toInt() ?: -1
            if (uid == -1) {
                call.respond(HttpStatusCode.Unauthorized, Errors(ErrorCodes.ERR_BADVALUE.ordinal, "Invalid UID"))
                return@get
            }

            val gid = call.parameters["gid"]?.toIntOrNull() ?: run {
                call.respond(HttpStatusCode.BadRequest, Errors(ErrorCodes.ERR_BADVALUE.ordinal, "Invalid GID"))
                return@get
            }

            val (ret, invites) = getGroupRepoImpl().listInvites(uid, gid)
            when (ret) {
                InviteUrlReturn.OK -> call.respond(HttpStatusCode.OK, ListInvitesResponse(invites = invites!!))
                InviteUrlReturn.NOGROUP -> call.respond(HttpStatusCode.NotFound, Errors(ErrorCodes.ERR_RESOURCEMISSING.ordinal, "GID invalid"))
                InviteUrlReturn.NOPERMISSION -> call.respond(HttpStatusCode.Forbidden, Errors(ErrorCodes.ERR_FORBIDDEN.ordinal, "No permission to list invites"))
                else -> call.respond(HttpStatusCode.InternalServerError, Errors(ErrorCodes.ERR_INTERNALERROR.ordinal, "Error with the database"))
            }
        }

        /**
         * Revoca una invitación activa de un grupo.
         * Una invitación revocada no podrá ser usada aunque no haya expirado.
         * Solo Owners y Moderadores pueden revocar invitaciones.
         * El cuerpo de la petición debe ser un [RevokeInviteRequest] con `gid` y `code`.
         *
         * Respuestas:
         * - `200 OK`        — Invitación revocada correctamente.
         * - `401 Unauthorized` — UID inválido en el token.
         * - `403 Forbidden`    — Sin permisos.
         * - `404 Not Found`    — Grupo inexistente.
         * - `400 Bad Request`  — Código de invitación no encontrado.
         * - `500 Internal Server Error` — Error de base de datos.
         */
        post("/groups/invites/revoke") {
            val token = call.principal<JWTPrincipal>()
            val uid = token?.subject?.toInt() ?: -1
            if (uid == -1) {
                call.respond(HttpStatusCode.Unauthorized, Errors(ErrorCodes.ERR_BADVALUE.ordinal, "Invalid UID"))
                return@post
            }

            val data = call.receive<RevokeInviteRequest>()
            val ret = getGroupRepoImpl().revokeInvite(uid, data.gid, data.code)
            when (ret) {
                InviteUrlReturn.OK -> call.respond(HttpStatusCode.OK, OK_MESSAGE_RESPONSE)
                InviteUrlReturn.NOGROUP -> call.respond(HttpStatusCode.NotFound, Errors(ErrorCodes.ERR_RESOURCEMISSING.ordinal, "GID invalid"))
                InviteUrlReturn.NOPERMISSION -> call.respond(HttpStatusCode.Forbidden, Errors(ErrorCodes.ERR_FORBIDDEN.ordinal, "No permission to revoke invite"))
                InviteUrlReturn.INVALID_CODE -> call.respond(HttpStatusCode.BadRequest, Errors(ErrorCodes.ERR_BADVALUE.ordinal, "Invalid invite code"))
                else -> call.respond(HttpStatusCode.InternalServerError, Errors(ErrorCodes.ERR_INTERNALERROR.ordinal, "Error with the database"))
            }
        }

        /**
         * Lista todos los miembros de un grupo con su nombre, rol y fecha de incorporación.
         * Cualquier miembro del grupo puede consultar la lista.
         *
         * Parámetros de ruta:
         * - `gid` — Identificador del grupo.
         *
         * Respuestas:
         * - `200 OK`        — [GroupMembersResponse] con la lista de miembros.
         * - `401 Unauthorized` — UID inválido en el token.
         * - `400 Bad Request`  — GID no es un entero válido.
         * - `403 Forbidden`    — El solicitante no es miembro del grupo.
         * - `404 Not Found`    — Grupo inexistente.
         * - `500 Internal Server Error` — Error de base de datos.
         */
        get("/groups/{gid}/members") {
            val token = call.principal<JWTPrincipal>()
            val uid = token?.subject?.toInt() ?: -1
            if (uid == -1) {
                call.respond(HttpStatusCode.Unauthorized, Errors(ErrorCodes.ERR_BADVALUE.ordinal, "Invalid UID"))
                return@get
            }

            val gid = call.parameters["gid"]?.toIntOrNull() ?: run {
                call.respond(HttpStatusCode.BadRequest, Errors(ErrorCodes.ERR_BADVALUE.ordinal, "Invalid GID"))
                return@get
            }

            val (ret, members) = getGroupRepoImpl().listMembers(uid, gid)
            when (ret) {
                MemberListReturn.OK -> call.respond(HttpStatusCode.OK, GroupMembersResponse(members = members!!))
                MemberListReturn.NOGROUP -> call.respond(HttpStatusCode.NotFound, Errors(ErrorCodes.ERR_RESOURCEMISSING.ordinal, "GID invalid"))
                MemberListReturn.NOPERMISSION -> call.respond(HttpStatusCode.Forbidden, Errors(ErrorCodes.ERR_FORBIDDEN.ordinal, "No permission to list members"))
                else -> call.respond(HttpStatusCode.InternalServerError, Errors(ErrorCodes.ERR_INTERNALERROR.ordinal, "Error with the database"))
            }
        }

        /**
         * Permite al usuario autenticado abandonar voluntariamente un grupo.
         * El Owner no puede usar este endpoint; debe transferir la propiedad primero.
         * El cuerpo de la petición debe ser un [LeaveGroupRequest] con `gid`.
         *
         * Respuestas:
         * - `200 OK`        — El usuario abandonó el grupo correctamente.
         * - `401 Unauthorized` — UID inválido en el token.
         * - `400 Bad Request`  — El solicitante es el Owner del grupo.
         * - `404 Not Found`    — Grupo inexistente o usuario no es miembro.
         * - `500 Internal Server Error` — Error de base de datos.
         */
        post("/groups/leave") {
            val token = call.principal<JWTPrincipal>()
            val uid = token?.subject?.toInt() ?: -1
            if (uid == -1) {
                call.respond(HttpStatusCode.Unauthorized, Errors(ErrorCodes.ERR_BADVALUE.ordinal, "Invalid UID"))
                return@post
            }

            val data = call.receive<LeaveGroupRequest>()
            val ret = getGroupRepoImpl().leaveGroup(uid, data.gid)
            when (ret) {
                LeaveGroupReturn.OK -> call.respond(HttpStatusCode.OK, OK_MESSAGE_RESPONSE)
                LeaveGroupReturn.NOGROUP -> call.respond(HttpStatusCode.NotFound, Errors(ErrorCodes.ERR_RESOURCEMISSING.ordinal, "GID invalid"))
                LeaveGroupReturn.NOTMEMBER -> call.respond(HttpStatusCode.NotFound, Errors(ErrorCodes.ERR_RESOURCEMISSING.ordinal, "Not a member"))
                LeaveGroupReturn.OWNER_CANNOT_LEAVE -> call.respond(HttpStatusCode.BadRequest, Errors(ErrorCodes.ERR_BADVALUE.ordinal, "Owner cannot leave, transfer ownership first"))
                LeaveGroupReturn.CONNERR -> call.respond(HttpStatusCode.InternalServerError, Errors(ErrorCodes.ERR_INTERNALERROR.ordinal, "Error with the database"))
            }
        }

        /**
         * Cambia el rol de un miembro del grupo. Solo el Owner puede invocar esta operación.
         * No es posible cambiar el rol del propio Owner ni asignar el rol de Owner.
         * El cuerpo de la petición debe ser un [SetMemberRoleRequest] con `gid`, `userId` y `role`.
         *
         * Respuestas:
         * - `200 OK`        — Rol actualizado correctamente.
         * - `401 Unauthorized` — UID inválido en el token.
         * - `403 Forbidden`    — El solicitante no es el Owner.
         * - `404 Not Found`    — Grupo inexistente o usuario no es miembro.
         * - `400 Bad Request`  — Rol inválido o intento de cambiar el rol del Owner.
         * - `500 Internal Server Error` — Error de base de datos.
         */
        patch("/groups/setMemberRole") {
            val token = call.principal<JWTPrincipal>()
            val uid = token?.subject?.toInt() ?: -1
            if (uid == -1) {
                call.respond(HttpStatusCode.Unauthorized, Errors(ErrorCodes.ERR_BADVALUE.ordinal, "Invalid UID"))
                return@patch
            }

            val data = call.receive<SetMemberRoleRequest>()
            val ret = getGroupRepoImpl().setMemberRole(uid, data.gid, data.userId, data.role)
            when (ret) {
                SetMemberRoleReturn.OK -> call.respond(HttpStatusCode.OK, OK_MESSAGE_RESPONSE)
                SetMemberRoleReturn.NOGROUP -> call.respond(HttpStatusCode.NotFound, Errors(ErrorCodes.ERR_RESOURCEMISSING.ordinal, "GID invalid"))
                SetMemberRoleReturn.NOTMEMBER -> call.respond(HttpStatusCode.NotFound, Errors(ErrorCodes.ERR_RESOURCEMISSING.ordinal, "Target not member"))
                SetMemberRoleReturn.NOPERMISSION -> call.respond(HttpStatusCode.Forbidden, Errors(ErrorCodes.ERR_FORBIDDEN.ordinal, "Only owner can change roles"))
                SetMemberRoleReturn.INVALID_ROLE -> call.respond(HttpStatusCode.BadRequest, Errors(ErrorCodes.ERR_BADVALUE.ordinal, "Invalid role"))
                SetMemberRoleReturn.CONNERR -> call.respond(HttpStatusCode.InternalServerError, Errors(ErrorCodes.ERR_INTERNALERROR.ordinal, "Error with the database"))
            }
        }

        /**
         * Recupera el historial de eventos de auditoría de un grupo con paginación.
         * Cualquier miembro del grupo puede consultar el log.
         *
         * Parámetros de ruta:
         * - `gid` — Identificador del grupo.
         *
         * Parámetros de consulta:
         * - `limit`  — Número máximo de eventos (por defecto 50, máximo 200).
         * - `offset` — Desplazamiento para paginación (por defecto 0).
         *
         * Respuestas:
         * - `200 OK`        — [AuditEventsResponse] con la lista de eventos.
         * - `401 Unauthorized` — UID inválido en el token.
         * - `400 Bad Request`  — GID no es un entero válido.
         * - `403 Forbidden`    — El solicitante no es miembro del grupo.
         * - `404 Not Found`    — Grupo inexistente.
         * - `500 Internal Server Error` — Error de base de datos.
         */
        get("/groups/{gid}/audit") {
            val token = call.principal<JWTPrincipal>()
            val uid = token?.subject?.toInt() ?: -1
            if (uid == -1) {
                call.respond(HttpStatusCode.Unauthorized, Errors(ErrorCodes.ERR_BADVALUE.ordinal, "Invalid UID"))
                return@get
            }

            val gid = call.parameters["gid"]?.toIntOrNull() ?: run {
                call.respond(HttpStatusCode.BadRequest, Errors(ErrorCodes.ERR_BADVALUE.ordinal, "Invalid GID"))
                return@get
            }

            val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 50
            val offset = call.request.queryParameters["offset"]?.toLongOrNull() ?: 0L

            val (ret, events) = getGroupRepoImpl().listAudit(uid, gid, limit = limit.coerceIn(1, 200), offset = offset.coerceAtLeast(0))
            when (ret) {
                MemberListReturn.OK -> call.respond(HttpStatusCode.OK, AuditEventsResponse(events = events!!))
                MemberListReturn.NOGROUP -> call.respond(HttpStatusCode.NotFound, Errors(ErrorCodes.ERR_RESOURCEMISSING.ordinal, "GID invalid"))
                MemberListReturn.NOPERMISSION -> call.respond(HttpStatusCode.Forbidden, Errors(ErrorCodes.ERR_FORBIDDEN.ordinal, "No permission to view audit"))
                else -> call.respond(HttpStatusCode.InternalServerError, Errors(ErrorCodes.ERR_INTERNALERROR.ordinal, "Error with the database"))
            }
        }

        /**
         * Edita los metadatos (nombre y/o descripción) de un grupo.
         * Los campos nulos en el cuerpo no se modifican.
         * Solo Owners y Moderadores pueden editar un grupo.
         * El cuerpo de la petición debe ser un [EditGroupRequest] con `gid` y los campos opcionales.
         *
         * Respuestas:
         * - `200 OK`        — Grupo editado correctamente.
         * - `401 Unauthorized` — UID inválido en el token.
         * - `403 Forbidden`    — El solicitante no tiene permisos suficientes.
         */
        patch("/groups/editGroup") {
            val token = call.principal<JWTPrincipal>()
            val uid = token?.subject?.toInt() ?: -1
            if (uid == -1){
                call.respond(HttpStatusCode.Unauthorized, Errors(ErrorCodes.ERR_BADVALUE.ordinal, "Invalid UID"))
                return@patch
            }

            val data = call.receive<EditGroupRequest>()

            if (getGroupRepoImpl().editGroup(gid = data.gid, uid = uid, name = data.name, desc = data.desc)){
                call.respond(HttpStatusCode.OK, OK_MESSAGE_RESPONSE)
            }else{
                call.respond(HttpStatusCode.Forbidden, Errors(ErrorCodes.ERR_FORBIDDEN.ordinal, "Doesn't have enough privileges to edit the group"))
            }

        }

        /**
         * Transfiere la propiedad de un grupo del Owner actual a otro miembro existente.
         * Solo el Owner actual puede realizar esta operación.
         * El cuerpo de la petición debe ser un [PassOwnershipRequest] con `gid` y `newOwnerUserId`.
         *
         * Respuestas:
         * - `200 OK`        — Propiedad transferida correctamente.
         * - `401 Unauthorized` — UID inválido en el token.
         * - `403 Forbidden`    — El solicitante no es el Owner actual.
         * - `404 Not Found`    — Grupo inexistente.
         * - `400 Bad Request`  — El destino no es miembro del grupo o ya es el Owner.
         * - `500 Internal Server Error` — Error de base de datos.
         */
        patch("/groups/passOwnership"){
            val token = call.principal<JWTPrincipal>()
            val uid = token?.subject?.toInt() ?: -1
            if (uid == -1) {
                call.respond(HttpStatusCode.Unauthorized, Errors(ErrorCodes.ERR_BADVALUE.ordinal, "Invalid UID"))
                return@patch
            }

            val data = call.receive<PassOwnershipRequest>()
            val ret = getGroupRepoImpl().passOwnership(uid, data.newOwnerUserId, data.gid)

            when (ret) {
                PassOwnershipReturn.OK -> call.respond(HttpStatusCode.OK, OK_MESSAGE_RESPONSE)
                PassOwnershipReturn.NOGROUP -> call.respond(HttpStatusCode.NotFound, Errors(ErrorCodes.ERR_RESOURCEMISSING.ordinal, "GID invalid"))
                PassOwnershipReturn.NOPERMISSION -> call.respond(HttpStatusCode.Forbidden, Errors(ErrorCodes.ERR_FORBIDDEN.ordinal, "Only owner can transfer ownership"))
                PassOwnershipReturn.TARGETNOTMEMBER -> call.respond(HttpStatusCode.BadRequest, Errors(ErrorCodes.ERR_BADVALUE.ordinal, "New owner must be a group member"))
                PassOwnershipReturn.SAMEOWNER -> call.respond(HttpStatusCode.BadRequest, Errors(ErrorCodes.ERR_BADVALUE.ordinal, "Selected user is already owner"))
                PassOwnershipReturn.CONNERR -> call.respond(HttpStatusCode.InternalServerError, Errors(ErrorCodes.ERR_INTERNALERROR.ordinal, "Error with the database"))
            }
        }

        /**
         * Elimina permanentemente un grupo y todos sus datos asociados.
         * Solo el Owner del grupo puede realizar esta operación.
         * El cuerpo de la petición debe ser un [DeleteGroupRequest] con `gid`.
         *
         * Respuestas:
         * - `200 OK`        — Grupo eliminado correctamente.
         * - `401 Unauthorized` — UID inválido en el token.
         * - `403 Forbidden`    — El solicitante no es el Owner del grupo.
         */
        delete("/groups/deleteGroup") {
            val token = call.principal<JWTPrincipal>()
            val uid = token?.subject?.toInt() ?: -1
            if (uid == -1){
                call.respond(HttpStatusCode.Unauthorized, Errors(ErrorCodes.ERR_BADVALUE.ordinal, "Invalid UID"))
                return@delete
            }

            val data = call.receive<DeleteGroupRequest>()

            if (getGroupRepoImpl().deleteGroup(uid = uid, gid = data.gid)){
                call.respond(HttpStatusCode.OK, OK_MESSAGE_RESPONSE)
            }else{
                call.respond(HttpStatusCode.Forbidden, Errors(ErrorCodes.ERR_FORBIDDEN.ordinal, "Doesn't have enough privileges to delete the group"))
            }
        }

    }
}
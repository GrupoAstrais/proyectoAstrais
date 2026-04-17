package com.astrais.groups

import com.astrais.ErrorCodes
import com.astrais.Errors
import OK_MESSAGE_RESPONSE
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable


/**
 * Salida de un grupo en /group/userGroups
 */
@Serializable
data class SingleGroupOut(
    /** ID del grupo */
    val id : Int,
    /** Nombre del grupo */
    val name : String,
    /** La descripcion del grupo*/
    val description : String,
    /** El rol expresado de forma entera
     * @see ROLE_USERNORMAL
     * @see ROLE_USERMOD
     * @see ROLE_USEROWNER */
    val role: Int
)

/**
 * Salida de la ruta /group/userGroups
 */
@Serializable
data class AllGroupsResponse(
    val groupList : List<SingleGroupOut>
)
@Serializable
data class CreateGroupRequest(
    val name : String,
    val desc : String
)

@Serializable
data class AddUserRequest(
    val gid : Int,
    val userId : Int
)

@Serializable
data class DeleteGroupRequest (
    val gid : Int
)

@Serializable
data class EditGroupRequest (
    val gid : Int,
    val name: String? = null,
    val desc: String? = null
)

fun Route.groupRoutes(){
    authenticate("access-jwt") {
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
                AddUserReturn.NOPERMISSION -> call.respond(HttpStatusCode.Forbidden, Errors(ErrorCodes.EER_FORBIDDEN.ordinal, "User doesn't have the permission to add people"))
                AddUserReturn.CONNERR -> call.respond(HttpStatusCode.InternalServerError, Errors(ErrorCodes.ERR_INTERNALERROR.ordinal, "Error with the database"))
            }
        }

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
                call.respond(HttpStatusCode.Unauthorized, Errors(ErrorCodes.EER_FORBIDDEN.ordinal, "Doesn't have enough privileges to edit the group"))
            }

        }

        patch("/groups/passOwnership"){
            call.respond(HttpStatusCode.InternalServerError, Errors(ErrorCodes.ERR_UNIMPLEMENTED.ordinal, "Not implemented yet"))
        }

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
                call.respond(HttpStatusCode.Unauthorized, Errors(ErrorCodes.EER_FORBIDDEN.ordinal, "Doesn't have enough privileges to delete the group"))
            }
        }

    }
}
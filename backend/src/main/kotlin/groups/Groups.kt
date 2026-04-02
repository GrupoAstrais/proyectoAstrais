package com.astrais.groups

import com.astrais.ErrorCodes
import com.astrais.Errors
import com.astrais.OK_MESSAGE_RESPONSE
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable


@Serializable
data class SingleGroupOut(
    val id : Int,
    val name : String,
    val description : String,
    val role: Int
)
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
                call.respond(HttpStatusCode.OK, OK_MESSAGE_RESPONSE)
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

    }
}
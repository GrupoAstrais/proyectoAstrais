package com.astrais.groups

import com.astrais.ErrorCodes
import com.astrais.Errors
import com.astrais.auth.RegisterRequest
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable


const val ROLE_USERNORMAL   = 0
const val ROLE_USERMOD      = 1
const val ROLE_USEROWNER    = 2

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

data class CreateGroupRequest(
    val name : String,
    val desc : String
)

fun Route.groupRoutes(){
    authenticate("access-jwt") {
        post("/group/userGroups") {
            try {
                val token = call.principal<JWTPrincipal>()
                val uid = token?.subject?.toInt() ?: -1

                if (uid != -1){
                    val out = getGroupRepoImpl().getAllUserGroups(uid)
                    call.respond(HttpStatusCode.OK, AllGroupsResponse(out))
                }else {
                    call.respond(HttpStatusCode.BadRequest, Errors(ErrorCodes.ERR_BADVALUE.ordinal, "Invalid UID"))
                }
            } catch (e : BadRequestException){
                call.respond(HttpStatusCode.BadRequest, Errors(ErrorCodes.ERR_MALFORMEDMESSAGE.ordinal, "The data sent by the client was not in the accepted format"))
            } catch (e : NumberFormatException) {
                call.respond(HttpStatusCode.BadRequest, Errors(ErrorCodes.ERR_MALFORMEDMESSAGE.ordinal, "The subject of the token is invalid"))
            }
        }

        post("/groups/createGroup") {
            try {
                val token = call.principal<JWTPrincipal>()
                val uid = token?.subject?.toInt() ?: -1

                if (uid != -1){
                    val req = call.receive<CreateGroupRequest>()
                    val gid = getGroupRepoImpl().createGroup(req.name, req.desc, uid)
                    if (gid != -1){
                        call.respond(HttpStatusCode.OK, arrayOf("aknowledged" to true))
                    }else{
                        call.respond(HttpStatusCode.Conflict, Errors(ErrorCodes.ERR_RESOURCENOTCREATED.ordinal, "Can't create the group"))
                    }

                }else {
                    call.respond(HttpStatusCode.BadRequest, Errors(ErrorCodes.ERR_BADVALUE.ordinal, "Invalid UID"))
                }
            } catch (e : BadRequestException){
                call.respond(HttpStatusCode.BadRequest, Errors(ErrorCodes.ERR_MALFORMEDMESSAGE.ordinal, "The data sent by the client was not in the accepted format"))
            } catch (e : NumberFormatException) {
                call.respond(HttpStatusCode.BadRequest, Errors(ErrorCodes.ERR_MALFORMEDMESSAGE.ordinal, "The subject of the token is invalid"))
            }
        }

    }
}
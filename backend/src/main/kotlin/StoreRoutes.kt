import com.astrais.ErrorCodes
import com.astrais.Errors
import com.astrais.db.BuyCosmeticResponse
import com.astrais.db.getDatabaseDaoImpl
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

@Serializable
data class CosmeticResponseDTO(
        val id: Int,
        val name: String,
        val desc: String,
        val type: String,
        val price: Int,
        val assetRef: String,
        val theme: String,
        val coleccion: String,
        val owned: Boolean,
        val equipped: Boolean
)

fun Route.storeRoutes() {
    authenticate("access-jwt") {
        route("/store") {
            get("/items") {
                val uid = call.principal<JWTPrincipal>()!!.subject?.toInt() ?: return@get call.respond(HttpStatusCode.Unauthorized, Errors(ErrorCodes.ERR_FORBIDDEN.ordinal, "No UID available"))
                call.respond(HttpStatusCode.OK, getDatabaseDaoImpl().getStoreItems(uid, true))
            }
            get("/items/admin") {
                val uid = call.principal<JWTPrincipal>()!!.subject?.toInt() ?: return@get call.respond(HttpStatusCode.Unauthorized, Errors(ErrorCodes.ERR_FORBIDDEN.ordinal, "No UID available"))
                if (getDatabaseDaoImpl().checkIfUserIsServerAdmin(uid)){
                    call.respond(HttpStatusCode.OK, getDatabaseDaoImpl().getStoreItems(uid, false))
                }else {
                    call.respond(HttpStatusCode.Forbidden, Errors(ErrorCodes.ERR_FORBIDDEN.ordinal, "You are not administrator"))
                }
            }
            post("/buy/{id}") {
                val uid = call.principal<JWTPrincipal>()!!.subject?.toInt() ?: return@post call.respond(HttpStatusCode.Unauthorized, Errors(ErrorCodes.ERR_FORBIDDEN.ordinal, "No UID available"))
                val cid = call.parameters["id"]?.toInt() ?: return@post call.respond(HttpStatusCode.BadRequest, Errors(ErrorCodes.ERR_FORBIDDEN.ordinal, "Didn't catch the Cosmetic ID"))

                when (getDatabaseDaoImpl().buyCosmetic(uid, cid)){
                    BuyCosmeticResponse.OKAY -> call.respond(HttpStatusCode.OK, OK_MESSAGE_RESPONSE)
                    BuyCosmeticResponse.USER_NOT_FOUND -> call.respond(HttpStatusCode.BadRequest, Errors(ErrorCodes.ERR_RESOURCEMISSING.ordinal, "User was not found"))
                    BuyCosmeticResponse.COSMETIC_NOT_FOUND -> call.respond(HttpStatusCode.BadRequest, Errors(ErrorCodes.ERR_RESOURCEMISSING.ordinal, "The cosmetic was not found"))
                    BuyCosmeticResponse.INSUFICIENT_CURRENCY -> call.respond(HttpStatusCode.BadRequest, Errors(ErrorCodes.ERR_INTERNALERROR.ordinal, "Insuficient funds"))
                    BuyCosmeticResponse.ALREADY_HAS_OBJECT -> call.respond(HttpStatusCode.BadRequest, Errors(ErrorCodes.ERR_RESOURCEALREADYEXISTS.ordinal, "Cosmetic was already bought"))
                    else -> call.respond("what?")
                }
            }
            post("/equip/{id}") {
                val uid = call.principal<JWTPrincipal>()!!.subject?.toInt() ?: return@post call.respond(HttpStatusCode.Unauthorized, Errors(ErrorCodes.ERR_FORBIDDEN.ordinal, "No UID available"))
                val cid = call.parameters["id"]?.toInt() ?: return@post call.respond(HttpStatusCode.BadRequest, Errors(ErrorCodes.ERR_FORBIDDEN.ordinal, "Didn't catch the Cosmetic ID"))

                when (getDatabaseDaoImpl().equipCosmetic(uid, cid)){
                    BuyCosmeticResponse.OKAY -> call.respond(HttpStatusCode.OK, OK_MESSAGE_RESPONSE)
                    BuyCosmeticResponse.USER_NOT_FOUND -> call.respond(HttpStatusCode.BadRequest, Errors(ErrorCodes.ERR_RESOURCEMISSING.ordinal, "User was not found"))
                    BuyCosmeticResponse.COSMETIC_NOT_FOUND -> call.respond(HttpStatusCode.BadRequest, Errors(ErrorCodes.ERR_RESOURCEMISSING.ordinal, "The cosmetic was not found"))
                    BuyCosmeticResponse.INSUFICIENT_CURRENCY -> call.respond(HttpStatusCode.BadRequest, Errors(ErrorCodes.ERR_INTERNALERROR.ordinal, "Insuficient funds"))
                    BuyCosmeticResponse.ALREADY_HAS_OBJECT -> call.respond(HttpStatusCode.BadRequest, Errors(ErrorCodes.ERR_RESOURCEMISSING.ordinal, "Not owned"))
                    else -> call.respond("what?")
                }
            }
        }
    }
}

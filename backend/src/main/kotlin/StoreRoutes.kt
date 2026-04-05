import com.astrais.ErrorCodes
import com.astrais.Errors
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
                val uid =
                        call.principal<JWTPrincipal>()!!.subject?.toInt()
                                ?: return@get call.respond(HttpStatusCode.Unauthorized)
                call.respond(HttpStatusCode.OK, getDatabaseDaoImpl().getStoreItems(uid))
            }
            post("/buy/{id}") {
                val uid =
                        call.principal<JWTPrincipal>()!!.subject?.toInt()
                                ?: return@post call.respond(HttpStatusCode.Unauthorized)
                val cid =
                        call.parameters["id"]?.toInt()
                                ?: return@post call.respond(HttpStatusCode.BadRequest)
                if (getDatabaseDaoImpl().buyCosmetic(uid, cid))
                        call.respond(HttpStatusCode.OK, mapOf("success" to true))
                else
                        call.respond(
                                HttpStatusCode.BadRequest,
                                mapOf("error" to "No funds or already owned")
                        )
            }
            post("/equip/{id}") {
                val uid =
                        call.principal<JWTPrincipal>()!!.subject?.toInt()
                                ?: return@post call.respond(HttpStatusCode.Unauthorized)
                val cid =
                        call.parameters["id"]?.toInt()
                                ?: return@post call.respond(HttpStatusCode.BadRequest)
                if (getDatabaseDaoImpl().equipCosmetic(uid, cid))
                        call.respond(HttpStatusCode.OK, mapOf("success" to true))
                else call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Not owned"))
            }
        }
    }
}

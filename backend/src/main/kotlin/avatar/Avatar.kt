package avatar

import AvatarLayer
import com.astrais.ErrorCodes
import com.astrais.Errors
import com.astrais.db.getDatabaseDaoImpl
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

@Serializable
data class AvatarLayerDTO(
    val slot: String,
    val layer: AvatarLayer,
    val assetRef: String,
    val cosmeticId: Int
)

fun Route.avatarRoute(){
    authenticate("access-jwt") {
        post("/avatar/") {
            val uid = call.principal<JWTPrincipal>()!!.subject?.toInt() ?: return@post call.respond(
                HttpStatusCode.Unauthorized, Errors(
                    ErrorCodes.ERR_FORBIDDEN.ordinal, "No UID available")
            )

            val layer = getDatabaseDaoImpl().retrieveAvatar(uid)
            call.respond(HttpStatusCode.OK, mapOf("avatarLayers" to layer))
        }
    }
}
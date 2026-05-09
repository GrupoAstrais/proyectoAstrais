package avatar

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
    val cosmeticId: Int,
    val name: String,
    val imageRef: String,
    val rareza : String
)

fun Route.avatarRoute(){
    authenticate("access-jwt") {
        post("/avatar/") {
            val uid = call.principal<JWTPrincipal>()!!.subject?.toInt() ?: return@post call.respond(
                HttpStatusCode.Unauthorized, Errors(
                    ErrorCodes.ERR_FORBIDDEN.ordinal, "No UID available")
            )

            val layer = getDatabaseDaoImpl().getUserEquippedAvatar(uid)
            if (layer != null){
                call.respond(HttpStatusCode.OK, mapOf("avatar" to AvatarLayerDTO(
                    cosmeticId = layer.id.value,
                    name = layer.nombre,
                    imageRef = layer.assetRef,
                    rareza = layer.rareza.name
                )))
            } else {
                call.respond(HttpStatusCode.OK, mapOf("avatar" to null))
            }
        }
    }
}
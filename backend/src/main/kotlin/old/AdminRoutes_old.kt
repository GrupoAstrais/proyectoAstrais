package old
/*

import com.astrais.db.CosmeticType
import com.astrais.db.getDatabaseDaoImpl
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.http.content.streamProvider
import io.ktor.server.request.receiveMultipart
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import java.io.File
import kotlinx.serialization.Serializable

@Serializable
data class CreateCosmeticRequest(
    val name: String,
    val desc: String,
    val type: String,
    val price: Int,
    val assetRef: String,
    val theme: String,
    val adminPassword: String
)

enum class RarityType(val multiplier: Double) {
    COMUN(1.0),
    RARO(2.5),
    EPICO(5.0),
    LEGENDARIO(10.0)
}

fun calculateCosmeticPrice(type: CosmeticType, rarity: RarityType): Int {
    val baseCost = when (type) {
        CosmeticType.AVATAR_PART -> 100
        CosmeticType.PET_SKIN -> 300
        CosmeticType.APP_THEME -> 500
        CosmeticType.PET -> 1000
    }
    return (baseCost * rarity.multiplier).toInt()
}

fun Route.adminRoutesOld() {
    route("/admin") {
        post("/cosmetic/upload") {
            val multipart = call.receiveMultipart()
            var rarityStr = "COMUN"
            var name = ""
            var desc = ""
            var type = ""
            var price = 0
            var theme = ""
            var adminPassword = ""
            var fileName = ""
            var fileBytes: ByteArray? = null
            var collection = "DEFAULT"
            multipart.forEachPart { part ->
                when (part) {
                    is PartData.FormItem -> {
                        when (part.name) {
                            "name" -> name = part.value
                            "desc" -> desc = part.value
                            "type" -> type = part.value
                            "price" ->
                                price =
                                    part.value.toIntOrNull()
                                        ?: 0
                            "theme" -> theme = part.value
                            "adminPassword" ->
                                adminPassword = part.value
                            "collection" -> collection = part.value
                            "rarity" -> rarityStr = part.value
                        }
                    }
                    is PartData.FileItem -> {
                        fileName =
                            part.originalFileName
                                ?.substringAfterLast("/")
                                ?.substringAfterLast("\\")
                                ?: "unknown.json"
                        fileBytes = part.streamProvider().readBytes()
                    }
                    else -> {}
                }
                part.dispose()
            }

            // Esto hay que cambiarlo jejejeje
            if (adminPassword != "AstraisAdmin2026!") {
                call.respond(
                    HttpStatusCode.Unauthorized,
                    mapOf("error" to "Contraseña incorrecta")
                )
                return@post
            }

            if (fileBytes == null || fileName.isBlank() || !fileName.endsWith(".json")
            ) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf(
                        "error" to
                                "Falta el archivo Lottie o no es formato .json"
                    )
                )
                return@post
            }

            try {
                val baseDir = System.getenv("UPLOAD_DIR") ?: "uploads"
                val uploadDir = File(baseDir, "pets")
                if (!uploadDir.exists()) uploadDir.mkdirs()

                val file = File(uploadDir, fileName)
                file.writeBytes(fileBytes!!)


                val typeEnum = runCatching { CosmeticType.valueOf(type) }.getOrElse { CosmeticType.PET }
                val rarityEnum = runCatching { RarityType.valueOf(rarityStr) }.getOrElse { RarityType.COMUN }
                val finalPrice = calculateCosmeticPrice(typeEnum, rarityEnum)
                val success = getDatabaseDaoImpl().createCosmetic(
                    name = name,
                    desc = desc,
                    type = typeEnum,
                    price = finalPrice,
                    assetRef = fileName,
                    theme = theme,
                    coleccion = collection,
                    layer = null
                )

                if (success) {
                    call.respond(
                        HttpStatusCode.Created,
                        mapOf("success" to true)
                    )
                } else {
                    if (file.exists()) file.delete()
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to "Error al guardar en BD")
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to "Error al escribir el archivo")
                )
            }
        }
    }
}
*/
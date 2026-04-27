package admin

import AvatarLayer
import OK_MESSAGE_RESPONSE
import com.astrais.ErrorCodes
import com.astrais.Errors
import com.astrais.db.CosmeticType
import com.astrais.db.getDatabaseDaoImpl
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File

@Serializable
data class CreateCosmeticRequest(
    val engName: String,
    val espName: String,
    val rusName: String,
    val desc: String,
    val type: String,
    val price: Int,
    val assetRef: String,
    val theme: String,
)

@Serializable
data class NamesCosmetic(
    val engName: String,
    val espName: String,
    val rusName: String,
)

@Serializable
data class CreateGroupAdmin(
    val name : String,
    val uid : Int,
    val desc : String
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

fun Route.adminRoutes() {
    authenticate("access-jwt") {
        route("/admin") {
            post("/cosmetic/upload") {
                val token = call.principal<JWTPrincipal>()?.subject?.toInt()
                if (token == null) {
                    // No deberia ser null, pero se hace la comprobacion por si acaso
                    call.respond(
                        HttpStatusCode.Unauthorized,
                        Errors(
                            ErrorCodes.ERR_INVALIDTOKEN.ordinal,
                            "Invalid/Missing refresh token"
                        )
                    )
                    return@post
                }

                if (!getDatabaseDaoImpl().checkIfUserIsServerAdmin(token)){
                    call.respond(
                        HttpStatusCode.Forbidden,
                        Errors(
                            ErrorCodes.ERR_FORBIDDEN.ordinal,
                            "Forbidden, you are not admin"
                        )
                    )
                    return@post
                }


                val multipart = call.receiveMultipart()
                val data = receiveFormDataFromClient(multipart)

                if (data.fileBytes == null || data.fileName.isBlank() || !data.fileName.endsWith(".json")) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        Errors(
                            ErrorCodes.ERR_MALFORMEDMESSAGE.ordinal,
                            "Falta el archivo Lottie o no es formato .json"
                        )
                    )
                    return@post
                }

                try {
                    val uploadsubdir = "pets"

                    saveFileIntoCosmetics(
                        uploadDir = uploadsubdir,
                        filename = data.fileName,
                        data = data.fileBytes!!
                    )


                    val finalPrice = if (data.price == 0) {
                        calculateCosmeticPrice(data.type, data.rarity)
                    } else{
                        data.price
                    }
                    val success = getDatabaseDaoImpl().createCosmetic(
                        name = Json.encodeToString(NamesCosmetic(
                            data.engName, data.espName.ifEmpty { data.engName }, data.rusName.ifEmpty { data.engName }
                        )),
                        desc = data.desc,
                        type = data.type,
                        price = finalPrice,
                        assetRef = data.fileName,
                        theme = data.theme,
                        coleccion = data.collection,
                        layer = data.layer,
                        rarity = data.rarity
                    )

                    if (success) {
                        call.respond(
                            HttpStatusCode.Created,
                            OK_MESSAGE_RESPONSE
                        )
                    } else {
                        val file = getCosmeticPath(uploadDir = uploadsubdir, filename =  data.fileName)
                        if (file.exists()) file.delete()

                        call.respond(
                            HttpStatusCode.InternalServerError,
                            Errors(
                                    ErrorCodes.ERR_INTERNALERROR.ordinal,
                            "Error al guardar en BD"
                            )
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

            post("/cosmetic/upload/{cid}") {
                val token = call.principal<JWTPrincipal>()?.subject?.toInt()
                if (token == null) {
                    // No deberia ser null, pero se hace la comprobacion por si acaso
                    call.respond(
                        HttpStatusCode.Unauthorized,
                        Errors(
                            ErrorCodes.ERR_INVALIDTOKEN.ordinal,
                            "Invalid/Missing refresh token"
                        )
                    )
                    return@post
                }
                val cid = call.parameters["cid"]?.toInt() ?: return@post call.respond(HttpStatusCode.BadRequest, Errors(ErrorCodes.ERR_FORBIDDEN.ordinal, "CID invalid"))

                if (!getDatabaseDaoImpl().checkIfUserIsServerAdmin(token)){
                    call.respond(
                        HttpStatusCode.Forbidden,
                        Errors(
                            ErrorCodes.ERR_FORBIDDEN.ordinal,
                            "Forbidden, you are not admin"
                        )
                    )
                    return@post
                }


                val multipart = call.receiveMultipart()
                val data = receiveFormDataFromClient(multipart)

                if (data.fileBytes == null || data.fileName.isBlank() || !data.fileName.endsWith(".json")) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        Errors(
                            ErrorCodes.ERR_MALFORMEDMESSAGE.ordinal,
                            "Falta el archivo Lottie o no es formato .json"
                        )
                    )
                    return@post
                }

                try {
                    val uploadsubdir = "pets"

                    saveFileIntoCosmetics(
                        uploadDir = uploadsubdir,
                        filename = data.fileName,
                        data = data.fileBytes!!
                    )


                    val finalPrice = if (data.price == 0) {
                        calculateCosmeticPrice(data.type, data.rarity)
                    } else{
                        data.price
                    }
                    val success = getDatabaseDaoImpl().adminUpdateCosmetic(
                        cid = cid,
                        name = Json.encodeToString(
                            NamesCosmetic(
                                data.engName,
                            data.espName.ifEmpty { data.engName },
                            data.rusName.ifEmpty { data.engName }
                        )),
                        desc = data.desc,
                        type = data.type,
                        price = finalPrice,
                        assetRef = data.fileName,
                        theme = data.theme,
                        coleccion = data.collection,
                        layer = data.layer,
                        rarity = data.rarity
                    )

                    if (success) {
                        call.respond(
                            HttpStatusCode.Created,
                            OK_MESSAGE_RESPONSE
                        )
                    } else {
                        val file = getCosmeticPath(uploadDir = uploadsubdir, filename =  data.fileName)
                        if (file.exists()) file.delete()

                        call.respond(
                            HttpStatusCode.InternalServerError,
                            Errors(
                                ErrorCodes.ERR_INTERNALERROR.ordinal,
                                "Error al guardar en BD"
                            )
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

            delete("/cosmetic/delete/{cid}") {
                val token = call.principal<JWTPrincipal>()?.subject?.toInt()
                if (token == null) {
                    // No deberia ser null, pero se hace la comprobacion por si acaso
                    call.respond(
                        HttpStatusCode.Unauthorized,
                        Errors(
                            ErrorCodes.ERR_INVALIDTOKEN.ordinal,
                            "Invalid/Missing refresh token"
                        )
                    )
                    return@delete
                }
                val cid = call.parameters["cid"]?.toInt() ?: return@delete call.respond(HttpStatusCode.BadRequest, Errors(ErrorCodes.ERR_FORBIDDEN.ordinal, "CID invalid"))

                if (!getDatabaseDaoImpl().checkIfUserIsServerAdmin(token)){
                    call.respond(
                        HttpStatusCode.Forbidden,
                        Errors(
                            ErrorCodes.ERR_FORBIDDEN.ordinal,
                            "Forbidden, you are not admin"
                        )
                    )
                    return@delete
                }


                if (getDatabaseDaoImpl().admindeleteCosmetic(cid)){
                    call.respond(HttpStatusCode.OK, OK_MESSAGE_RESPONSE)
                }else {
                    call.respond(HttpStatusCode.BadRequest, Errors(ErrorCodes.ERR_RESOURCENOTMODIFIED.ordinal, "Couldn't delete the cosmetic"))
                }
            }

            // Gets some data from all users
            post("/users") {
                val token = call.principal<JWTPrincipal>()?.subject?.toInt()
                if (token == null) {
                    // No deberia ser null, pero se hace la comprobacion por si acaso
                    call.respond(
                        HttpStatusCode.Unauthorized,
                        Errors(
                            ErrorCodes.ERR_INVALIDTOKEN.ordinal,
                            "Invalid/Missing refresh token"
                        )
                    )
                    return@post
                }

                if (!getDatabaseDaoImpl().checkIfUserIsServerAdmin(token)){
                    call.respond(
                        HttpStatusCode.Forbidden,
                        Errors(
                            ErrorCodes.ERR_FORBIDDEN.ordinal,
                            "Forbidden, you are not admin"
                        )
                    )
                    return@post
                }

                val users = getDatabaseDaoImpl().adminGetAllUsers()
                call.respond(HttpStatusCode.OK, users)

            }

            post ("/groups") {
                val token = call.principal<JWTPrincipal>()?.subject?.toInt()
                if (token == null) {
                    // No deberia ser null, pero se hace la comprobacion por si acaso
                    call.respond(
                        HttpStatusCode.Unauthorized,
                        Errors(
                            ErrorCodes.ERR_INVALIDTOKEN.ordinal,
                            "Invalid/Missing refresh token"
                        )
                    )
                    return@post
                }

                if (!getDatabaseDaoImpl().checkIfUserIsServerAdmin(token)){
                    call.respond(
                        HttpStatusCode.Forbidden,
                        Errors(
                            ErrorCodes.ERR_FORBIDDEN.ordinal,
                            "Forbidden, you are not admin"
                        )
                    )
                    return@post
                }

                val groups = getDatabaseDaoImpl().adminGetAllGroups()
                call.respond(HttpStatusCode.OK, groups)
            }
            post("/groups/add") {
                val token = call.principal<JWTPrincipal>()?.subject?.toInt()
                if (token == null) {
                    // No deberia ser null, pero se hace la comprobacion por si acaso
                    call.respond(
                        HttpStatusCode.Unauthorized,
                        Errors(
                            ErrorCodes.ERR_INVALIDTOKEN.ordinal,
                            "Invalid/Missing refresh token"
                        )
                    )
                    return@post
                }

                if (!getDatabaseDaoImpl().checkIfUserIsServerAdmin(token)){
                    call.respond(
                        HttpStatusCode.Forbidden,
                        Errors(
                            ErrorCodes.ERR_FORBIDDEN.ordinal,
                            "Forbidden, you are not admin"
                        )
                    )
                    return@post
                }

                val data = call.receive<CreateGroupAdmin>()

                val gid = getDatabaseDaoImpl().createGroup(
                    grpownerId = data.uid,
                    grpname = data.name,
                    grpdescription = data.desc,
                    personal = false
                )
                if (gid == -1){
                    call.respond(HttpStatusCode.BadRequest, Errors(ErrorCodes.ERR_RESOURCENOTCREATED.ordinal, "Couldn't create group"))
                }else{
                    call.respond(HttpStatusCode.OK, OK_MESSAGE_RESPONSE)
                }
            }
        }
    }
}

data class FormClientData(
    var rarity : RarityType,
    var engName : String,
    var espName : String,
    var rusName : String,
    var desc : String,
    var type : CosmeticType,
    var price : Int,
    var theme : String,
    var fileName : String,
    var fileBytes: ByteArray?,
    var collection : String,
    var layer : AvatarLayer?
)

suspend fun receiveFormDataFromClient(multipart : MultiPartData) : FormClientData {
    var rarityStr : String = "COMUN"
    var engName : String = ""
    var espName : String = ""
    var rusName : String = ""
    var desc : String = ""
    var type : String = ""
    var price : Int = 0
    var theme : String = ""
    var fileName : String = ""
    var fileBytes: ByteArray? = null
    var collection : String = "DEFAULT"
    var layer : AvatarLayer? = null

    multipart.forEachPart { part ->
        when (part) {
            is PartData.FormItem -> {
                when (part.name) {
                    "engName" -> engName = part.value
                    "espName" -> espName = part.value
                    "rusName" -> rusName = part.value
                    "desc" -> desc = part.value
                    "type" -> type = part.value
                    "price" -> price = part.value.toIntOrNull() ?: 0
                    "theme" -> theme = part.value
                    "collection" -> collection = part.value
                    "rarity" -> rarityStr = part.value
                    "layer" -> layer = AvatarLayer.valueOf(part.value)
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

    return FormClientData(
        rarity = runCatching { RarityType.valueOf(rarityStr) }.getOrElse { RarityType.COMUN },
        engName = engName,
        espName = espName,
        rusName = rusName,
        desc = desc,
        type = runCatching { CosmeticType.valueOf(type) }.getOrElse { CosmeticType.PET },
        price = price,
        theme = theme,
        fileName = fileName,
        fileBytes = fileBytes,
        collection = collection,
        layer = layer
    )
}

fun getCosmeticPath(uploadDir: String = "", filename: String) : File {
    val baseDir = System.getenv("UPLOAD_DIR") ?: "uploads"

    val finalFolderDir = if (uploadDir.isEmpty()){
        File(baseDir)
    } else {
        File(baseDir, uploadDir)
    }
    return finalFolderDir
}

fun saveFileIntoCosmetics(uploadDir : String = "", filename : String, data : ByteArray) {
    val finalFolderDir = getCosmeticPath(uploadDir,filename)

    if (!finalFolderDir.exists()){
        finalFolderDir.mkdirs()
    }

    val finalFile = File(finalFolderDir, filename)
    finalFile.writeBytes(data)
}
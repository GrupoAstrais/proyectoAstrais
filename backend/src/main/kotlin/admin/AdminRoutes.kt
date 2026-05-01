package admin

import OK_MESSAGE_RESPONSE
import com.astrais.ErrorCodes
import com.astrais.Errors
import com.astrais.db.CosmeticType
import com.astrais.db.DatosSimpleUsuarios
import com.astrais.db.UserRoles
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
import kotlin.reflect.jvm.internal.impl.types.error.ErrorEntity

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

@Serializable
data class EditGroupAdmin(
    val name : String?,
    val desc : String?
)

@Serializable
data class CreateUserAdmin(
    val name : String,
    val email : String,
    val password : String,
    val lang : String,
    val utcOffset : Float,
    val role : String
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
                val res = getAdminDao().uploadCosmetic(data)

                when (res.first) {
                    UploadCosmeticResponse.OK -> call.respond(HttpStatusCode.OK, OK_MESSAGE_RESPONSE)
                    UploadCosmeticResponse.MISSING_FILE -> call.respond(HttpStatusCode.BadRequest, Errors(ErrorCodes.ERR_MALFORMEDMESSAGE.ordinal, res.second))
                    UploadCosmeticResponse.DB_ERROR -> call.respond(HttpStatusCode.InternalServerError, Errors(ErrorCodes.ERR_INTERNALERROR.ordinal, res.second))
                    UploadCosmeticResponse.EXCEPTION -> call.respond(HttpStatusCode.InternalServerError, Errors(ErrorCodes.ERR_INTERNALERROR.ordinal, res.second))
                    UploadCosmeticResponse.NO_COSMETIC -> call.respond(HttpStatusCode.NotModified, Errors(ErrorCodes.ERR_RESOURCEMISSING.ordinal, res.second))
                    UploadCosmeticResponse.NOT_ADMIN -> call.respond(HttpStatusCode.Forbidden, Errors(ErrorCodes.ERR_FORBIDDEN.ordinal, "Forbidden, you are not admin"))
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
                val res = getAdminDao().updateCosmetic(cid, data)

                when (res.first) {
                    UploadCosmeticResponse.OK -> call.respond(HttpStatusCode.OK, OK_MESSAGE_RESPONSE)
                    UploadCosmeticResponse.MISSING_FILE -> call.respond(HttpStatusCode.BadRequest, Errors(ErrorCodes.ERR_MALFORMEDMESSAGE.ordinal, res.second))
                    UploadCosmeticResponse.DB_ERROR -> call.respond(HttpStatusCode.InternalServerError, Errors(ErrorCodes.ERR_INTERNALERROR.ordinal, res.second))
                    UploadCosmeticResponse.EXCEPTION -> call.respond(HttpStatusCode.InternalServerError, Errors(ErrorCodes.ERR_INTERNALERROR.ordinal, res.second))
                    UploadCosmeticResponse.NO_COSMETIC -> call.respond(HttpStatusCode.NotModified, Errors(ErrorCodes.ERR_RESOURCEMISSING.ordinal, res.second))
                    UploadCosmeticResponse.NOT_ADMIN -> call.respond(HttpStatusCode.Forbidden, Errors(ErrorCodes.ERR_FORBIDDEN.ordinal, "Forbidden, you are not admin"))
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


                if (getAdminDao().deleteCosmetic(cid)){
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

                val data = getAdminDao().listUsers()
                if (data.first){
                    call.respond(HttpStatusCode.OK, data.second)
                } else {
                    call.respond(HttpStatusCode.InternalServerError, Errors(HttpStatusCode.InternalServerError.value, "Unknown error!"))
                }
            }

            get("/users/{uid}") {
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
                    return@get
                }

                if (!getDatabaseDaoImpl().checkIfUserIsServerAdmin(token)){
                    call.respond(
                        HttpStatusCode.Forbidden,
                        Errors(
                            ErrorCodes.ERR_FORBIDDEN.ordinal,
                            "Forbidden, you are not admin"
                        )
                    )
                    return@get
                }

                val uid = call.parameters["uid"]?.toInt() ?: return@get call.respond(HttpStatusCode.BadRequest, Errors(ErrorCodes.ERR_MALFORMEDMESSAGE.ordinal, "No GID"))

                val data = getAdminDao().getUserData(uid)
                if (data != null){
                    call.respond(HttpStatusCode.OK, data)
                }else{
                    call.respond(HttpStatusCode.NotFound, Errors(ErrorCodes.ERR_RESOURCEMISSING.ordinal, "User doesn't exists"))
                }
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

            post("/groups/edit/{gid}") {
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

                val gid = call.parameters["gid"]?.toInt() ?: return@post call.respond(HttpStatusCode.BadRequest, Errors(ErrorCodes.ERR_MALFORMEDMESSAGE.ordinal, "No GID"))
                val data = call.receive<EditGroupAdmin>()

                if (getDatabaseDaoImpl().editGroup(
                        gid = gid,
                        name = data.name,
                        desc = data.desc
                )){
                    call.respond(HttpStatusCode.OK, OK_MESSAGE_RESPONSE)
                }else{
                    call.respond(HttpStatusCode.BadRequest, Errors(ErrorCodes.ERR_RESOURCENOTCREATED.ordinal, "Couldn't edit group"))
                }
            }

            delete("/groups/delete/{gid}"){
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
                val gid = call.parameters["gid"]?.toInt() ?: return@delete call.respond(HttpStatusCode.BadRequest, Errors(ErrorCodes.ERR_MALFORMEDMESSAGE.ordinal, "No GID"))

                if (getDatabaseDaoImpl().deleteGroup(gid)){
                    call.respond(HttpStatusCode.OK, OK_MESSAGE_RESPONSE)
                }else {
                    call.respond(HttpStatusCode.NotModified, Errors(ErrorCodes.ERR_RESOURCENOTMODIFIED.ordinal, "Couldn't delete!"))
                }
            }

            post("/user/create"){
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

                val data = call.receive<CreateUserAdmin>()

                val d = getDatabaseDaoImpl().createUser(
                    nombreusu = data.name,
                    emailusu = data.email,
                    passwordusu = data.password,
                    lang = data.lang,
                    utcOffset = data.utcOffset,
                    role = if (data.role == "ADMIN") {UserRoles.ADMIN_USER} else {UserRoles.NORMAL_USER}
                )
                if (d != -1){
                    call.respond(HttpStatusCode.OK, OK_MESSAGE_RESPONSE)
                }else {
                    call.respond(HttpStatusCode.BadRequest, Errors(ErrorCodes.ERR_RESOURCENOTCREATED.ordinal, "Couldn't create user"))
                }
            }
            post("/user/delete/{cid}"){
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

                val data = call.parameters["cid"]?.toInt() ?: return@post call.respond(HttpStatusCode.BadRequest, Errors(ErrorCodes.ERR_MALFORMEDMESSAGE.ordinal, "No CID"))

                val d = getDatabaseDaoImpl().deleteUsuario(data)
                if (d){
                    call.respond(HttpStatusCode.OK, OK_MESSAGE_RESPONSE)
                }else {
                    call.respond(HttpStatusCode.BadRequest, Errors(ErrorCodes.ERR_RESOURCENOTCREATED.ordinal, "Couldn't create user"))
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
    var theme : String?,
    var fileName : String,
    var fileBytes: ByteArray?,
    var collection : String,
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
        collection = collection
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
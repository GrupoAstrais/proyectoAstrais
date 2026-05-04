package admin

import com.astrais.db.CosmeticType
import com.astrais.db.DatosSimpleUsuarios
import com.astrais.db.UserRoles
import com.astrais.db.getDatabaseDaoImpl
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.v1.exceptions.ExposedSQLException

class AdminDaoImpl : AdminDao {
    override suspend fun uploadCosmetic(formData: FormClientData): Pair<UploadCosmeticResponse, String> {

        if (formData.type == CosmeticType.PET && (formData.fileBytes == null || formData.fileName.isBlank() || !formData.fileName.endsWith(".json"))) {
            return Pair(UploadCosmeticResponse.MISSING_FILE, "Falta el archivo Lottie o no es formato .json")
        } else if (formData.type == CosmeticType.APP_THEME && (formData.theme == null)){
            return Pair(UploadCosmeticResponse.MISSING_FILE, "No se envio el JSON de tema")
        } else if (formData.type == CosmeticType.AVATAR_PART && (formData.fileBytes == null || formData.fileName.isBlank()) &&
                (!formData.fileName.endsWith(".png") || !formData.fileName.endsWith(".jpg"))){
            return Pair(UploadCosmeticResponse.MISSING_FILE, "No se adjunto imagen")
        }

        try {
            val uploadsubdir = if (formData.type == CosmeticType.PET) { "pets" } else { "avatar" }

            if (formData.type == CosmeticType.PET || formData.type == CosmeticType.AVATAR_PART) {
                saveFileIntoCosmetics(
                    uploadDir = uploadsubdir,
                    filename = formData.fileName,
                    data = formData.fileBytes!!
                )
            }

            val finalPrice = if (formData.price <= 0) {
                calculateCosmeticPrice(formData.type, formData.rarity)
            } else{
                formData.price
            }
            val success = getDatabaseDaoImpl().createCosmetic(
                name = Json.encodeToString(NamesCosmetic(
                    formData.engName, formData.espName.ifEmpty { formData.engName }, formData.rusName.ifEmpty { formData.engName }
                )),
                desc = formData.desc,
                type = formData.type,
                price = finalPrice,
                assetRef = formData.fileName,
                theme = formData.theme ?: "",
                coleccion = formData.collection,
                rarity = formData.rarity
            )

            if (success) {
                return Pair(UploadCosmeticResponse.OK, "Everything's fine")
            } else {
                val file = getCosmeticPath(uploadDir = uploadsubdir, filename =  formData.fileName)
                if (file.exists()) file.delete()

                return Pair(UploadCosmeticResponse.DB_ERROR, "Error al guardar en BD")
            }
        }
        catch (e : ExposedSQLException) {
            val msg = "SqlException: ${e.message}"
            println(msg)

            return Pair(UploadCosmeticResponse.DB_ERROR, msg)
        }
        catch (e: Exception) {
            val msg = "Exception! Tipo: ${e.javaClass.name}. Message ${e.message}"
            println(msg)
            e.printStackTrace()

            return Pair(UploadCosmeticResponse.EXCEPTION, msg)
        }
    }

    override suspend fun updateCosmetic(cid: Int, formData : FormClientData): Pair<UploadCosmeticResponse, String> {

        if (formData.fileBytes != null){
            if (formData.type == CosmeticType.PET && (!formData.fileName.endsWith(".json"))) {
                return Pair(UploadCosmeticResponse.MISSING_FILE, "Falta el archivo Lottie o no es formato .json")
            } else if (formData.type == CosmeticType.AVATAR_PART && (!formData.fileName.endsWith(".png") || !formData.fileName.endsWith(".jpg"))){
                return Pair(UploadCosmeticResponse.MISSING_FILE, "No se adjunto imagen")
            }
        }

        try {
            val uploadsubdir = if (formData.type == CosmeticType.PET) { "pets" } else { "avatar" }

            val ent = getDatabaseDaoImpl().getCosmetic(cid) ?: return Pair(UploadCosmeticResponse.NO_COSMETIC, "No cosmetic with that ID")
            if ((formData.fileBytes != null && formData.fileName.isNotBlank()) && (formData.type == CosmeticType.PET || formData.type == CosmeticType.AVATAR_PART)) {
                val cos = getCosmeticPath(uploadsubdir, ent.assetRef)
                if (cos.exists()){
                    cos.delete()
                }

                saveFileIntoCosmetics(
                    uploadDir = uploadsubdir,
                    filename = formData.fileName,
                    data = formData.fileBytes!!
                )
            }


            val success = getDatabaseDaoImpl().adminUpdateCosmetic(
                cid = cid,
                name = Json.encodeToString(
                    NamesCosmetic(
                        formData.engName,
                        formData.espName,
                        formData.rusName
                    )),
                desc = formData.desc,
                type = formData.type,
                price = formData.price,
                assetRef = if (formData.fileBytes != null) formData.fileName else ent.assetRef,
                theme = formData.theme ?: "",
                coleccion = formData.collection,
                rarity = formData.rarity
            )

            if (success) {
                return Pair(UploadCosmeticResponse.OK, "Everything's fine")
            } else {
                val file = getCosmeticPath(uploadDir = uploadsubdir, filename = formData.fileName)
                if (file.exists()) file.delete()

                return Pair(UploadCosmeticResponse.DB_ERROR, "Error al guardar en BD")
            }
        }
        catch (e : ExposedSQLException) {
            val msg = "SqlException: ${e.message}"
            println(msg)

            return Pair(UploadCosmeticResponse.DB_ERROR, msg)
        }
        catch (e: Exception) {
            val msg = "Exception! Tipo: ${e.javaClass.name}. Message ${e.message}"
            println(msg)
            e.printStackTrace()

            return Pair(UploadCosmeticResponse.EXCEPTION, msg)
        }
    }

    override suspend fun deleteCosmetic(cid: Int): Boolean {
        try {
            if (getDatabaseDaoImpl().admindeleteCosmetic(cid)){
                return true
            }else {
                return false
            }
        } catch (e : Exception) {
            val msg = "Exception! Tipo: ${e.javaClass.name}. Message ${e.message}"
            println(msg)
            e.printStackTrace()

            return false
        }
    }

    override suspend fun listUsers(): Pair<Boolean, List<DatosSimpleUsuarios>> {
        try {
            val users = getDatabaseDaoImpl().adminGetAllUsers()
            return Pair(true, users)
        } catch (e : Exception) {
            val msg = "Exception! Tipo: ${e.javaClass.name}. Message ${e.message}"
            println(msg)
            e.printStackTrace()

            return Pair(false, emptyList())
        }
    }

    override suspend fun getUserData(uid: Int): DatosSimpleUsuarios? {
        try {
            val data = getDatabaseDaoImpl().getUsuarioByID(id = uid)
            if (data != null){
                return DatosSimpleUsuarios(
                    id = data.id.value,
                    nombre = data.nombre,
                    rol = if (data.rol == UserRoles.ADMIN_USER) {"Admin"} else {"User"},
                    nivel = data.nivel
                )
            }else{
                return null
            }
        } catch (e : Exception) {
            val msg = "Exception! Tipo: ${e.javaClass.name}. Message ${e.message}"
            println(msg)
            e.printStackTrace()

            return null
        }
    }
}

package admin

import com.astrais.db.DatosSimpleUsuarios

enum class UploadCosmeticResponse{
    OK,
    MISSING_FILE,
    DB_ERROR,
    EXCEPTION,
    NO_COSMETIC,
    NOT_ADMIN
}

interface AdminRepo {
    suspend fun uploadCosmetic(formData : FormClientData) : Pair<UploadCosmeticResponse, String>
    suspend fun updateCosmetic(cid : Int, formData : FormClientData) : Pair<UploadCosmeticResponse, String>
    suspend fun deleteCosmetic(cid : Int) : Boolean

    suspend fun listUsers() : Pair<Boolean, List<DatosSimpleUsuarios>>
    suspend fun getUserData(uid : Int) : DatosSimpleUsuarios?

    suspend fun editUserResources(uid: Int, xpTotal : Int, ludiones : Int) : UploadCosmeticResponse
}

fun getAdminRepoImpl() : AdminRepo{
    return AdminRepoImpl()
}
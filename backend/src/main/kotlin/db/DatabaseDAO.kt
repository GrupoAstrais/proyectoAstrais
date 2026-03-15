package com.astrais.db

interface DatabaseDAO {
    /**
     * Crea un usuario en la base de datos
     * @return Devuelve el ID de usuario
     */
    suspend fun createUser(nombreusu : String,
                           emailusu : String,
                           passwordusu : String,
                           lang : String = LANG_CODE_ENGLISH,
                           utcOffset : Float = 0f,
                           role : UserRoles = UserRoles.NORMAL_USER
    ) : Int
    suspend fun getUsuario(emailusu: String): EntidadUsuario?
    suspend fun getUsuarioByID(id: Int): EntidadUsuario?
    suspend fun deleteUsuario(id: Int) : Boolean
}

fun getDatabaseDaoImpl() : DatabaseDAO{
    return DatabaseDAOImpl()
}
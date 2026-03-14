package com.astrais.db

interface DatabaseDAO {
    suspend fun crearUsuario(nombreusu : String, emailusu : String, passwordusu : String, lang : String = LANG_CODE_ENGLISH) : Int
    suspend fun getUsuario(emailusu: String): EntidadUsuario?
    suspend fun getUsuarioByID(id: Int): EntidadUsuario?
    suspend fun deleteUsuario(id: Int) : Boolean
}

fun getDatabaseDaoImpl() : DatabaseDAO{
    return DatabaseDAOImpl()
}
package com.astrais.db

interface DatabaseDAO {
    suspend fun crearUsuario(nombreusu : String, emailusu : String, passwordusu : String, lang : String = LANG_CODE_ENGLISH)
}
package com.astrais.db

import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction

class DatabaseDAOImpl : DatabaseDAO {
    override suspend fun crearUsuario(nombreusu : String, emailusu: String, passwordusu: String, lang : String){
        suspendTransaction {
            TablaUsuario.insert {
                it[nombre] = nombreusu
                it[email] = emailusu
                it[contrasenia] = passwordusu
                it[idioma] = lang
            }
        }
    }
}
package com.astrais.db

import kotlinx.datetime.toKotlinLocalDate
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import java.time.LocalDate

class DatabaseDAOImpl : DatabaseDAO {
    override suspend fun createUser(
        nombreusu: String,
        emailusu: String,
        passwordusu: String,
        lang: String,
        utcOffset: Float,
        role: UserRoles
    ): Int {
        return suspendTransaction {
            TablaUsuario.insert {
                it[nombre] = nombreusu
                it[email] = emailusu
                it[contrasenia] = passwordusu
                it[idioma] = lang
                it[zona_horaria] = utcOffset
                it[rol] = role
                it[ultimo_login] = LocalDate.now().toKotlinLocalDate()
            }[TablaUsuario.id].value

        }
    }

    override suspend fun getUsuario(emailusu: String) : EntidadUsuario? {
        return suspendTransaction {
            EntidadUsuario.find {
                TablaUsuario.email.eq(emailusu)
            }.singleOrNull()
        }
    }

    override suspend fun getUsuarioByID(id: Int): EntidadUsuario? {
        return suspendTransaction {
            EntidadUsuario.find {
                TablaUsuario.id.eq(id)
            }.singleOrNull()
        }
    }

    override suspend fun deleteUsuario(id: Int) : Boolean {
        return suspendTransaction {
            TablaUsuario.deleteWhere {
                TablaUsuario.id.eq(id)
            } > 0
        }
    }

    override suspend fun setUserLastLogin(ent: EntidadUsuario) {
        suspendTransaction {
            ent.ultimo_login = java.time.LocalDate.now().toKotlinLocalDate()
        }
    }
}

suspend fun da(){
    val get = getDatabaseDaoImpl().getUsuarioByID(0)
}
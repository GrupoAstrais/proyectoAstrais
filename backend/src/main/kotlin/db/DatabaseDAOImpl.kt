package com.astrais.db

import kotlinx.datetime.toKotlinLocalDate
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.inSubQuery
import org.jetbrains.exposed.v1.core.or
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import java.time.LocalDate

class DatabaseDAOImpl : DatabaseDAO {
    // https://www.jetbrains.com/help/exposed/dsl-querying-data.html


    override suspend fun createUser(
        nombreusu: String,
        emailusu: String,
        passwordusu: String,
        lang: String,
        utcOffset: Float,
        role: UserRoles
    ): Int {
        return suspendTransaction {
            EntidadUsuario.new {
                nombre = nombreusu
                email = emailusu
                contrasenia = passwordusu
                idioma = lang
                zona_horaria = utcOffset
                rol = role
                ultimo_login = LocalDate.now().toKotlinLocalDate()
            }.id.value
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
            EntidadUsuario.findById(id)
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

    override suspend fun createGroup(grpownerId: Int, grpname: String, grpdescription: String, personal : Boolean): Int {
        return suspendTransaction {
            EntidadGrupo.new {
                es_grupo_personal = personal
                owner = EntityID(grpownerId, TablaUsuario)
                nombre = grpname
                descripcion = grpdescription
            }.id.value
        }
    }

    override suspend fun getGroupById(id: Int): EntidadGrupo? {
        return suspendTransaction {
            EntidadGrupo.findById(id)
        }
    }

    override suspend fun getGroupsOfUser(idusuario: Int) : List<EntidadGrupo> {
        return suspendTransaction {
            val eid = EntityID(idusuario, TablaUsuario)
            // Subquery en TablaGrupoIntegrantes
            val subquery = TablaGrupoUsuario
                .select(TablaGrupoUsuario.gid)
                .where { return@where TablaGrupoUsuario.uid.eq(eid) }

            // El find
            EntidadGrupo.find {
                TablaGrupo.owner.eq(eid).or(TablaGrupo.id.inSubQuery(subquery))
            }.toList()
        }
    }

    override suspend fun getUserRoleOnGroup(idusuario: Int, idgrupo: Int): GroupRoles? {
        return suspendTransaction {
            EntidadGrupoUsuario.find{
                TablaGrupoUsuario.gid.eq(EntityID(idgrupo, TablaGrupo))
            }.singleOrNull()?.role
        }
    }

    override suspend fun addUserToGroup(idusuario: Int, idgrupo: Int): Boolean {
        return suspendTransaction {
            // Es hasta divertido hacer queries aqui
            val tableExists = TablaGrupo.selectAll().where { TablaUsuario.id.eq(idgrupo) }.empty().not()

            if (tableExists){
                //EntidadGrupoUsuario.new me daba un error raro
                TablaGrupoUsuario.insert {
                    it[gid] = EntityID(idgrupo, TablaGrupo)
                    it[uid] = EntityID(idusuario, TablaUsuario)
                }.insertedCount > 0
            }else{
                false
            }
        }

    }


}


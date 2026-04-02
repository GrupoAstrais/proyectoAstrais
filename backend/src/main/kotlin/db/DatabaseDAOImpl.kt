package com.astrais.db

import java.time.LocalDate
import kotlinx.datetime.toKotlinLocalDate
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.inSubQuery
import org.jetbrains.exposed.v1.core.or
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction

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
                    }
                    .id
                    .value
        }
    }

    override suspend fun getUsuario(emailusu: String): EntidadUsuario? {
        return suspendTransaction {
            EntidadUsuario.find { TablaUsuario.email.eq(emailusu) }.singleOrNull()
        }
    }

    override suspend fun getUsuarioByID(id: Int): EntidadUsuario? {
        return suspendTransaction { EntidadUsuario.findById(id) }
    }

    override suspend fun deleteUsuario(id: Int): Boolean {
        return suspendTransaction { TablaUsuario.deleteWhere { TablaUsuario.id.eq(id) } > 0 }
    }

    override suspend fun setUserLastLogin(ent: EntidadUsuario) {
        suspendTransaction { ent.ultimo_login = java.time.LocalDate.now().toKotlinLocalDate() }
    }

    override suspend fun createGroup(
            grpownerId: Int,
            grpname: String,
            grpdescription: String,
            personal: Boolean
    ): Int {
        return suspendTransaction {
            EntidadGrupo.new {
                        es_grupo_personal = personal
                        owner = EntityID(grpownerId, TablaUsuario)
                        nombre = grpname
                        descripcion = grpdescription
                    }
                    .id
                    .value
        }
    }

    override suspend fun getGroupById(id: Int): EntidadGrupo? {
        return suspendTransaction { EntidadGrupo.findById(id) }
    }

    override suspend fun getGroupsOfUser(idusuario: Int): List<EntidadGrupo> {
        return suspendTransaction {
            val eid = EntityID(idusuario, TablaUsuario)
            // Subquery en TablaGrupoIntegrantes
            val subquery =
                    TablaGrupoUsuario.select(TablaGrupoUsuario.gid).where {
                        return@where TablaGrupoUsuario.uid.eq(eid)
                    }

            // El find
            EntidadGrupo.find { TablaGrupo.owner.eq(eid).or(TablaGrupo.id.inSubQuery(subquery)) }
                    .toList()
        }
    }

    override suspend fun getUserRoleOnGroup(idusuario: Int, idgrupo: Int): GroupRoles? {
        return suspendTransaction {
            EntidadGrupoUsuario.find {
                        TablaGrupoUsuario.gid
                                .eq(EntityID(idgrupo, TablaGrupo))
                                .and(TablaGrupoUsuario.uid.eq(EntityID(idusuario, TablaUsuario)))
                    }
                    .singleOrNull()
                    ?.role
        }
    }

    override suspend fun addUserToGroup(idusuario: Int, idgrupo: Int): Boolean {
        return suspendTransaction {
            // Es hasta divertido hacer queries aqui
            val tableExists =
                    TablaGrupo.selectAll().where { TablaGrupo.id.eq(idgrupo) }.empty().not()

            if (tableExists) {
                // EntidadGrupoUsuario.new me daba un error raro
                TablaGrupoUsuario.insert {
                            it[gid] = EntityID(idgrupo, TablaGrupo)
                            it[uid] = EntityID(idusuario, TablaUsuario)
                        }
                        .insertedCount > 0
            } else {
                false
            }
        }
    }

    override suspend fun createTarea(
            gid: Int,
            titulo: String,
            descripcion: String,
            tipo: TaskType,
            prioridad: Int,
            recompensaXp: Int,
            recompensaLudion: Int
    ): Int {
        return suspendTransaction {
            EntidadTarea.new {
                        id_grupo = EntityID(gid, TablaGrupo)
                        this.titulo = titulo
                        this.descripcion = descripcion
                        this.tipo = tipo
                        estado = TaskState.ACTIVE
                        this.prioridad = prioridad
                        recompensa_xp = recompensaXp
                        recompensa_ludion = recompensaLudion
                    }
                    .id
                    .value
        }
    }

    override suspend fun getTareasByGroup(gid: Int): List<EntidadTarea> {
        return suspendTransaction {
            EntidadTarea.find { TablaTarea.id_grupo.eq(EntityID(gid, TablaGrupo)) }.toList()
        }
    }

    override suspend fun completeTarea(tid: Int, uid: Int): Boolean {
        return suspendTransaction {
            val tarea = EntidadTarea.findById(tid) ?: return@suspendTransaction false
            val usuario = EntidadUsuario.findById(uid) ?: return@suspendTransaction false

            if (tarea.estado == TaskState.COMPLETE) {
                tarea.estado = TaskState.ACTIVE
                tarea.fecha_completado = null
            } else {
                tarea.estado = TaskState.COMPLETE
                tarea.fecha_completado = java.time.LocalDate.now().toKotlinLocalDate()

                if (!tarea.recompensa_reclamada) {
                    usuario.xp_actual += tarea.recompensa_xp
                    usuario.xp_total += tarea.recompensa_xp
                    usuario.ludiones += tarea.recompensa_ludion
                    usuario.total_tareas_completadas += 1
                    
                    // He hecho esto para probar la logica de niveles. Habra que hacerle su funcion aparte
                    var xpParaSiguienteNivel = (usuario.nivel + 1) * 100
                    while (usuario.xp_actual >= xpParaSiguienteNivel) {
                        usuario.xp_actual -= xpParaSiguienteNivel
                        usuario.nivel += 1
                        xpParaSiguienteNivel = (usuario.nivel + 1) * 100
                    }

                    tarea.recompensa_reclamada = true
                }
            }
            true
        }
    }
}

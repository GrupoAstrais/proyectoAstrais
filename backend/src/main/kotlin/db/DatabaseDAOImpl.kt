package com.astrais.db

import CosmeticResponseDTO
import com.astrais.LANG_CODE_ENGLISH
import com.astrais.auth.GoogleUserInfo
import java.time.LocalDate
import kotlinx.datetime.toKotlinLocalDate
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.jdbc.*
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
        // Primero borramos al usuario de los grupos
        TablaGrupoUsuario.deleteWhere {
            TablaGrupoUsuario.uid.eq(id)
        }
        // Luego, hacemos a otro usuario el owner
        val subquery = EntidadGrupo.find {
            TablaGrupo.owner.eq(id).and(TablaGrupo.es_grupo_personal.eq(false))
        }.toList()

        subquery.forEach { grp->
            val gid = grp.id.value
            val newOwner = TablaGrupoUsuario.selectAll()
                .where {
                    (TablaGrupoUsuario.gid.eq(gid)).and(TablaGrupoUsuario.uid.neq(id))
                }
                .limit(1)
                .firstOrNull()

            if (newOwner != null) {
                grp.owner = newOwner[TablaGrupoUsuario.uid]
            }
        }

        // Borramos grupo personal
        TablaGrupo.deleteWhere {
            TablaGrupo.owner.eq(id).and(TablaGrupo.es_grupo_personal.eq(true))
        }

        // Luego el usuario
        return suspendTransaction { TablaUsuario.deleteWhere { TablaUsuario.id.eq(id) } > 0 }
    }

    override suspend fun setUserLastLogin(ent: EntidadUsuario) {
        suspendTransaction { ent.ultimo_login = java.time.LocalDate.now().toKotlinLocalDate() }
    }

    override suspend fun checkForOauth(provider_uid : String, auth : AuthProvider) : Boolean {
        return suspendTransaction {
            !TablaCredencialesAuth.selectAll().where {
                (TablaCredencialesAuth.provider.eq(auth)).and(TablaCredencialesAuth.provider_uid.eq(provider_uid))
            }.empty()
        }
    }

    override suspend fun logOrCreateOauthUser(
        provider_uid: String,
        auth: AuthProvider
    ) : Pair<Int, Boolean> {
        suspendTransaction {
            val notExistUser = TablaCredencialesAuth.selectAll().where {
                (TablaCredencialesAuth.provider.eq(auth)).and(TablaCredencialesAuth.provider_uid.eq(provider_uid))
            }.singleOrNull()

            if (notExistUser == null){
                val newuser = EntidadUsuario.new {
                    this.nombre = "Astrais User"
                    this.email = null
                    this.contrasenia = null
                    this.idioma = LANG_CODE_ENGLISH
                    this.zona_horaria = 0.0f
                    this.rol = UserRoles.NORMAL_USER
                    this.ultimo_login = LocalDate.now().toKotlinLocalDate()
                }

                EntidadCredencialesAuth.new {
                    this.uid = newuser.id
                    this.provider = auth
                    this.provider_uid = provider_uid
                }
                return@suspendTransaction Pair(newuser.id, true)
            }else{
                return@suspendTransaction Pair(notExistUser[TablaCredencialesAuth.id], false)
            }

        }
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

    override suspend fun getGroupByTask(tid: Int): EntidadGrupo? {
        return suspendTransaction {
            val gid = TablaTarea.select(TablaTarea.id_grupo).where {
                TablaTarea.id_grupo.eq(tid)
            }.firstOrNull()?.get(TablaTarea.id_grupo)

            gid?.let {
                EntidadGrupo.findById(it.value)
            }
        }
    }

    override suspend fun editTask(gid : Int, titulo: String?, descripcion: String?, prioridad: Int?) {
        suspendTransaction {
            val grp = EntidadGrupo.find {
                TablaGrupo.id.eq(gid)
            }.singleOrNull()

            if (titulo != null){
                grp?.nombre = titulo
            }
            if (descripcion != null) {
                grp?.descripcion = descripcion
            }
            //if (prioridad != null){
            //    grp?.prioridad = prioridad
            //}
        }
    }

    override suspend fun checkIfUserIsAdmin(uid: Int, gid: Int): Boolean {
        return suspendTransaction {
            !TablaGrupo.selectAll().where { TablaGrupo.id.eq(gid).and(TablaGrupo.owner.eq(gid)) }.empty()
        }
    }

    override suspend fun editGroup(gid: Int, name: String?, desc: String?) : Boolean {
        return suspendTransaction {
            val group = EntidadGrupo.findById(gid) ?: return@suspendTransaction false

            if (name != null){
                group.nombre = name
            }
            if (desc != null){
                group.descripcion = desc
            }

            true
        }
    }

    override suspend fun deleteGroup(gid: Int): Boolean {
        return suspendTransaction {
            TablaGrupoUsuario.deleteWhere {
                TablaGrupoUsuario.gid.eq(gid)
            }

            TablaGrupo.deleteWhere {
                TablaGrupo.id.eq(gid)
            } > 0
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

                    // He hecho esto para probar la logica de niveles. Habra que hacerle su funcion
                    // aparte
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

    override suspend fun deleteTarea(tid: Int): Boolean {
        return suspendTransaction {
            TablaTarea.deleteWhere {
                TablaTarea.id.eq(tid)
            } > 0
        }
    }

    override suspend fun buyCosmetic(uid: Int, cosmeticId: Int): Boolean {
        return suspendTransaction {
            val usuario = EntidadUsuario.findById(uid) ?: return@suspendTransaction false
            val cosmetico = EntidadCosmetico.findById(cosmeticId) ?: return@suspendTransaction false

            val yaLoTiene =
                    EntidadInventario.find {
                                (TablaInventario.id_usuario eq uid) and
                                        (TablaInventario.id_cosmetico eq cosmeticId)
                            }
                            .empty()
                            .not()

            if (yaLoTiene || usuario.ludiones < cosmetico.precioLudiones)
                    return@suspendTransaction false

            usuario.ludiones -= cosmetico.precioLudiones
            EntidadInventario.new {
                id_usuario = EntityID(uid, TablaUsuario)
                id_cosmetico = EntityID(cosmeticId, TablaCosmetico)
                fecha_compra = java.time.LocalDate.now().toKotlinLocalDate()
            }
            true
        }
    }

    override suspend fun getStoreItems(uid: Int): List<CosmeticResponseDTO> {
        return suspendTransaction {
            val inventarioUsuario =
                    EntidadInventario.find { TablaInventario.id_usuario eq uid }.map {
                        it.id_cosmetico.value
                    }
            val usuario = EntidadUsuario.findById(uid)

            EntidadCosmetico.all().map { cosmetico ->
                val isEquipped =
                        if (cosmetico.tipo == CosmeticType.PET) {
                            usuario?.id_mascota_equipada?.value == cosmetico.id.value
                        } else if (cosmetico.tipo == CosmeticType.APP_THEME) {
                            usuario?.themeColors == cosmetico.tema
                        } else false

                CosmeticResponseDTO(
                        id = cosmetico.id.value,
                        name = cosmetico.nombre,
                        desc = cosmetico.descripcion,
                        type = cosmetico.tipo.name,
                        price = cosmetico.precioLudiones,
                        assetRef = cosmetico.assetRef,
                        theme = cosmetico.tema,
                        coleccion = cosmetico.coleccion, 
                        owned = inventarioUsuario.contains(cosmetico.id.value),
                        equipped = isEquipped
                )
            }
        }
    }

    override suspend fun equipCosmetic(uid: Int, cosmeticId: Int): Boolean {
        return suspendTransaction {
            val usuario = EntidadUsuario.findById(uid) ?: return@suspendTransaction false

            if (cosmeticId == 0) return@suspendTransaction false

            val loTiene =
                    EntidadInventario.find {
                                (TablaInventario.id_usuario eq uid) and
                                        (TablaInventario.id_cosmetico eq cosmeticId)
                            }
                            .empty()
                            .not()

            if (loTiene) {
                val cosmetico =
                        EntidadCosmetico.findById(cosmeticId) ?: return@suspendTransaction false

                if (cosmetico.tipo == CosmeticType.PET) {
                    if (usuario.id_mascota_equipada?.value == cosmeticId) {
                        usuario.id_mascota_equipada = null
                    } else {
                        usuario.id_mascota_equipada = EntityID(cosmeticId, TablaCosmetico)
                    }
                } else if (cosmetico.tipo == CosmeticType.APP_THEME) {
                    if (usuario.themeColors == cosmetico.tema) {
                        usuario.themeColors = null
                    } else {
                        usuario.themeColors = cosmetico.tema
                    }
                }
                true
            } else false
        }
    }

    override suspend fun createCosmetic(
            name: String,
            desc: String,
            type: CosmeticType,
            price: Int,
            assetRef: String,
            theme: String,
            coleccion: String
    ): Boolean {
        return suspendTransaction {
            EntidadCosmetico.new {
                this.nombre = name
                this.descripcion = desc
                this.tipo = type
                this.precioLudiones = price
                this.assetRef = assetRef
                this.tema = theme
                this.coleccion = coleccion
            }
            true
        }
    }
}

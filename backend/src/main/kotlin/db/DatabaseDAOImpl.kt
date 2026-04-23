package com.astrais.db

import AvatarLayer
import CosmeticResponseDTO
import LANG_CODE_ENGLISH
import avatar.AvatarLayerDTO
import com.astrais.mainlogger
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

    override suspend fun editUser(
        uid : Int,
        nombreusu: String?,
        lang: String?,
        utcOffset: Float?,
    ): Boolean {
        return suspendTransaction {
            val user = EntidadUsuario.findById(uid) ?: return@suspendTransaction false

            if (nombreusu != null){
                user.nombre = nombreusu
            }
            if (lang != null){
                user.idioma = lang
            }
            if (utcOffset != null){
                user.zona_horaria = utcOffset
            }

            true

        }
    }

    override suspend fun setupUserEmail(uid: Int, newEmail: String?, newPassword: String?) : Boolean {
        return suspendTransaction {
            val user = EntidadUsuario.findById(uid) ?: return@suspendTransaction false

            // Pone nuevo correo y se asegura de tener que confirmar otra vez.
            if (newEmail != null){
                user.email = newEmail
                user.esta_confirmado = 0
                mainlogger.info("User ${user.id} changed email!")
            }

            if (newPassword != null){
                user.contrasenia = newPassword
                mainlogger.info("User ${user.id} changed password!")
            }

            true
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
            uid.eq(id)
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
            owner.eq(id).and(es_grupo_personal.eq(true))
        }

        // Luego el usuario
        return suspendTransaction { TablaUsuario.deleteWhere { TablaUsuario.id.eq(id) } > 0 }
    }

    override suspend fun setUserLastLogin(ent: EntidadUsuario) {
        suspendTransaction { ent.ultimo_login = LocalDate.now().toKotlinLocalDate() }
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
        return suspendTransaction {
            val notExistUser = TablaCredencialesAuth.selectAll().where {
                (TablaCredencialesAuth.provider.eq(auth)).and(TablaCredencialesAuth.provider_uid.eq(provider_uid))
            }.singleOrNull()

            if (notExistUser == null) {
                val newuser = EntidadUsuario.new {
                    this.nombre = "Astrais User"
                    this.email = null
                    this.contrasenia = null
                    this.idioma = LANG_CODE_ENGLISH
                    this.zona_horaria = 0.0f
                    this.rol = UserRoles.NORMAL_USER
                    this.ultimo_login = LocalDate.now().toKotlinLocalDate()
                }

                TablaCredencialesAuth.insert {
                    it[uid] = newuser.id.value
                    it[provider] = auth
                    it[TablaCredencialesAuth.provider_uid] = provider_uid
                }

                Pair(newuser.id.value, true)

            } else {
                Pair(notExistUser[TablaCredencialesAuth.uid].value, false)
            }
        }
    }

    override suspend fun addOauthToAccount(uid: Int, provider_uid: String, auth: AuthProvider): BuyCosmeticResponse {
        return suspendTransaction {
            // Comprobacion de si el usuario existe
            if (TablaUsuario.selectAll().where {
                    TablaUsuario.id.eq(uid)
                }.empty()){
                return@suspendTransaction BuyCosmeticResponse.USER_NOT_FOUND
            }

            // No deberia tener mas de uno por cada tipo de oauth vinculados en una cuenta
            val existCred = !TablaCredencialesAuth.selectAll().where {
                (TablaCredencialesAuth.provider.eq(auth))
            }.empty()
            if (existCred) {
                return@suspendTransaction BuyCosmeticResponse.ALREADY_HAS_OBJECT
            }

            // Añade metodo oauth
            TablaCredencialesAuth.insert {
                it[TablaCredencialesAuth.uid] = uid
                it[TablaCredencialesAuth.provider] = auth
                it[TablaCredencialesAuth.provider_uid] = provider_uid
            }

            return@suspendTransaction BuyCosmeticResponse.OKAY
        }
    }

    override suspend fun deleteOauthFromAccount(uid: Int, auth: AuthProvider) : BuyCosmeticResponse{
        return suspendTransaction {
            // Comprobacion de si el usuario existe
            if (TablaUsuario.selectAll().where {
                    TablaUsuario.id.eq(uid)
                }.empty()){
                return@suspendTransaction BuyCosmeticResponse.USER_NOT_FOUND
            }

            val methodCheck = countUserLoginMehods(uid)
            if (methodCheck <= 1){
                return@suspendTransaction BuyCosmeticResponse.NO_METHOD_REMAIN
            }

            TablaCredencialesAuth.deleteWhere {
                TablaCredencialesAuth.uid.eq(uid).and(TablaCredencialesAuth.provider.eq(auth))
            }

            return@suspendTransaction BuyCosmeticResponse.OKAY
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
        recompensaLudion: Int,
        extraUnico : TareaUniqueData?,
        extraHabito : TareaHabitData?,
    ): Int {
        return suspendTransaction {
            if (tipo == TaskType.UNICO && extraUnico == null){
                return@suspendTransaction -1
            } else if (tipo == TaskType.HABITO && extraHabito == null){
                return@suspendTransaction -2
            } else{
                val nuevaTarea = EntidadTarea.new {
                    this.id_grupo = EntityID(gid, TablaGrupo)
                    this.titulo = titulo
                    this.descripcion = descripcion
                    this.tipo = tipo
                    this.estado = TaskState.ACTIVE
                    this.prioridad = prioridad
                    this.recompensa_xp = recompensaXp
                    this.recompensa_ludion = recompensaLudion
                    this.fecha_creacion = LocalDate.now().toKotlinLocalDate()
                    this.fecha_actualizado = this.fecha_creacion

                    if (extraUnico?.idObjetivo != null) {
                        this.id_objetivo = EntityID(extraUnico.idObjetivo, TablaTareaObjetivo)
                    }
                }

                if (tipo == TaskType.UNICO){
                    EntidadTareaUnica.new {
                        this.id_tarea = nuevaTarea.id
                        this.fecha_vencimiento = extraUnico!!.fechaLimite
                    }
                } else if (tipo == TaskType.HABITO){
                    EntidadTareaHabito.new {
                        this.id_tarea = nuevaTarea.id
                        this.variacion_freq = extraHabito!!.numeroFrecuencia
                        this.frecuencia = extraHabito.frequency
                        this.ultima_vez_completada = null
                    }
                }

                nuevaTarea.id.value
            }
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

    override suspend fun editTask(gid : Int, titulo: String?, descripcion: String?, prioridad: Int?) : Boolean {
        return suspendTransaction {
            val grp = EntidadTarea.find {
                TablaTarea.id.eq(gid)
            }.singleOrNull()
            if (grp == null){
                return@suspendTransaction false
            }

            if (!titulo.isNullOrEmpty()){
                grp.titulo = titulo
            }
            if (!descripcion.isNullOrEmpty()) {
                grp.descripcion = descripcion
            }
            grp.fecha_actualizado = LocalDate.now().toKotlinLocalDate()

            return@suspendTransaction true
        }
    }

    override suspend fun checkIfUserIsAdmin(uid: Int, gid: Int): Boolean {
        return suspendTransaction {
            !TablaGrupo.selectAll().where { TablaGrupo.id.eq(gid).and(TablaGrupo.owner.eq(uid)) }.empty()
        }
    }

    override suspend fun editGroup(gid: Int, name: String?, desc: String?) : Boolean {
        return suspendTransaction {
            val group = EntidadGrupo.findById(gid) ?: return@suspendTransaction false

            if (!name.isNullOrEmpty()){
                group.nombre = name
            }
            if (!desc.isNullOrEmpty()){
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
                id.eq(gid)
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

            if (tarea.tipo == TaskType.HABITO) {
                val habito = EntidadTareaHabito.find { TablaTareaHabito.id_tarea eq tarea.id }.singleOrNull()
                if (habito != null) {
                    val hoy = LocalDate.now().toKotlinLocalDate()

                    // Evitar farmeo
                    if (habito.ultima_vez_completada == hoy) return@suspendTransaction false

                    habito.ultima_vez_completada = hoy
                    habito.racha_actual += 1
                    if (habito.racha_actual > habito.mejor_racha) {
                        habito.mejor_racha = habito.racha_actual
                    }

                    recompensarUsuario(usuario, tarea)
                    return@suspendTransaction true
                }
            }

            if (tarea.estado != TaskState.COMPLETE) {
                tarea.estado = TaskState.COMPLETE
                tarea.fecha_completado = LocalDate.now().toKotlinLocalDate()

                if (!tarea.recompensa_reclamada) {
                    recompensarUsuario(usuario, tarea)
                    tarea.recompensa_reclamada = true
                }

                if (tarea.id_objetivo != null) {
                    val objetivoId = tarea.id_objetivo!!

                    val subtareas = EntidadTarea.find { TablaTarea.id_objetivo eq objetivoId }
                    val todasCompletas = subtareas.all { it.estado == TaskState.COMPLETE }

                    if (todasCompletas) {
                        val objetivoPadre = EntidadTareaObjetivo.findById(objetivoId.value)
                        val tareaPadre = objetivoPadre?.id_tarea?.let { EntidadTarea.findById(it.value) }

                        if (tareaPadre != null && tareaPadre.estado != TaskState.COMPLETE) {
                            tareaPadre.estado = TaskState.COMPLETE
                            tareaPadre.fecha_completado = LocalDate.now().toKotlinLocalDate()

                            if (!tareaPadre.recompensa_reclamada) {
                                recompensarUsuario(usuario, tareaPadre)
                                tareaPadre.recompensa_reclamada = true
                            }
                        }
                    }
                }
            }
            true
        }
    }

    private fun recompensarUsuario(usuario: EntidadUsuario, tarea: EntidadTarea) {
        val hoy = LocalDate.now().toKotlinLocalDate()

        if (usuario.ultima_fecha_ganancia != hoy) {
            usuario.ludiones_ganados_hoy = 0
            usuario.ultima_fecha_ganancia = hoy
        }

        val LIMITE_DIARIO = 500
        var ludionesFinales = tarea.recompensa_ludion

        if (tarea.tipo != TaskType.OBJETIVO) {
            val disponibles = LIMITE_DIARIO - usuario.ludiones_ganados_hoy
            if (ludionesFinales > disponibles) {
                ludionesFinales = if (disponibles > 0) disponibles else 0
            }
            usuario.ludiones_ganados_hoy += ludionesFinales
        }

        usuario.ludiones += ludionesFinales
        usuario.xp_actual += tarea.recompensa_xp
        usuario.xp_total += tarea.recompensa_xp
        usuario.total_tareas_completadas += 1

        var xpParaSiguienteNivel = (usuario.nivel + 1) * 100
        while (usuario.xp_actual >= xpParaSiguienteNivel) {
            usuario.xp_actual -= xpParaSiguienteNivel
            usuario.nivel += 1
            xpParaSiguienteNivel = (usuario.nivel + 1) * 100
        }
    }

    override suspend fun deleteTarea(tid: Int): Boolean {
        return suspendTransaction {
            TablaTarea.deleteWhere {
                id.eq(tid)
            } > 0
        }
    }

    override suspend fun buyCosmetic(uid: Int, cosmeticId: Int): BuyCosmeticResponse {
        return suspendTransaction {
            val usuario = EntidadUsuario.findById(uid) ?: return@suspendTransaction BuyCosmeticResponse.USER_NOT_FOUND
            val cosmetico = EntidadCosmetico.findById(cosmeticId) ?: return@suspendTransaction BuyCosmeticResponse.COSMETIC_NOT_FOUND

            val yaLoTiene =
                    EntidadInventario.find {
                        (TablaInventario.id_usuario eq uid) and (TablaInventario.id_cosmetico eq cosmeticId)
                    }
                    .empty()
                    .not()

            if (yaLoTiene){
                return@suspendTransaction BuyCosmeticResponse.ALREADY_HAS_OBJECT
            } else if (usuario.ludiones < cosmetico.precioLudiones){
                return@suspendTransaction BuyCosmeticResponse.INSUFICIENT_CURRENCY
            } else {
                usuario.ludiones -= cosmetico.precioLudiones
                EntidadInventario.new {
                    id_usuario = EntityID(uid, TablaUsuario)
                    id_cosmetico = EntityID(cosmeticId, TablaCosmetico)
                    fecha_compra = LocalDate.now().toKotlinLocalDate()
                }
                return@suspendTransaction BuyCosmeticResponse.OKAY
            }
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

    override suspend fun equipCosmetic(uid: Int, cosmeticId: Int): BuyCosmeticResponse {
        return suspendTransaction {
            val usuario = EntidadUsuario.findById(uid) ?: return@suspendTransaction BuyCosmeticResponse.USER_NOT_FOUND

            if (cosmeticId == 0) return@suspendTransaction BuyCosmeticResponse.COSMETIC_NOT_FOUND

            val loTiene =
                    EntidadInventario.find {
                        (TablaInventario.id_usuario eq uid) and (TablaInventario.id_cosmetico eq cosmeticId)
                    }
                    .empty()
                    .not()

            if (loTiene) {
                val cosmetico = EntidadCosmetico.findById(cosmeticId) ?: return@suspendTransaction BuyCosmeticResponse.COSMETIC_NOT_FOUND

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
                } else if (cosmetico.tipo == CosmeticType.AVATAR_PART) {
                    val noLoPuso = (TablaAvatarEquipado.innerJoin(
                        TablaCosmetico
                    )).selectAll().where {
                        TablaCosmetico.layer.isNotNull().and(TablaCosmetico.layer.eq(cosmetico.layer))
                    }.singleOrNull()

                    if (noLoPuso != null){
                        TablaAvatarEquipado.deleteWhere {
                            id.eq(noLoPuso[id])
                        }
                    }
                    EntidadAvatarEquipado.new {
                        this.id_usuario = EntityID(uid, TablaUsuario)
                        this.id_cosmetico = EntityID(cosmeticId, TablaCosmetico)
                    }
                }

                return@suspendTransaction BuyCosmeticResponse.OKAY
            } else {
                return@suspendTransaction BuyCosmeticResponse.ALREADY_HAS_OBJECT
            }
        }
    }

    override suspend fun createCosmetic(
            name: String,
            desc: String,
            type: CosmeticType,
            price: Int,
            assetRef: String,
            theme: String,
            coleccion: String,
            layer : AvatarLayer?
    ): Boolean {
        return suspendTransaction {
            if (type == CosmeticType.AVATAR_PART && layer != null){
                return@suspendTransaction false
            }

            EntidadCosmetico.new {
                this.nombre = name
                this.descripcion = desc
                this.tipo = type
                this.precioLudiones = price
                this.assetRef = assetRef
                this.tema = theme
                this.coleccion = coleccion
                this.layer = layer
            }
            true
        }
    }

    override suspend fun retrieveAvatar(uid: Int) : List<AvatarLayerDTO> {
        return suspendTransaction {
            (TablaInventario.innerJoin(TablaCosmetico)).selectAll().where {
                (TablaInventario.id_usuario eq uid).and(TablaCosmetico.tipo eq CosmeticType.AVATAR_PART)
            }.orderBy(TablaCosmetico.layer).map {
                AvatarLayerDTO(
                    slot = it[TablaCosmetico.tema],
                    layer = it[TablaCosmetico.layer]!!,
                    assetRef = it[TablaCosmetico.assetRef],
                    cosmeticId = it[TablaCosmetico.id].value
                )
            }
        }
    }

    override suspend fun saveConfirmationCode(uid: Int, code: String) {
        suspendTransaction {
            EntidadConfirmacionUsuario.new {
                this.uid = EntityID(uid, TablaUsuario)
                this.codigo_confirmacion = code
            }
        }
    }

    override suspend fun verifyConfirmationCode(email: String, code: String): Boolean {
        return suspendTransaction {
            val user = EntidadUsuario.find { TablaUsuario.email eq email }.singleOrNull() ?: return@suspendTransaction false
            val confirmacion = EntidadConfirmacionUsuario.find {
                (TablaConfirmacionUsuario.uid eq user.id) and (TablaConfirmacionUsuario.codigo_confirmacion eq code)
            }.singleOrNull()

            if (confirmacion != null) {
                user.esta_confirmado = 1
                confirmacion.delete()
                true
            } else {
                false
            }
        }
    }

    override suspend fun isUserConfirmed(email: String): Boolean {
        return suspendTransaction {
            val user = EntidadUsuario.find { TablaUsuario.email eq email }.singleOrNull()
            user?.esta_confirmado == 1
        }
    }
}

/**
 * Cuenta los metodos de login disponibles para el usuario en este momento, eso incluye Oauth y por correo
 * @param uid ID del usuario
 */
suspend fun countUserLoginMehods(uid : Int) : Int {
    return suspendTransaction {
        val data = EntidadUsuario.findById(uid) ?: return@suspendTransaction -1
        var contador = 0

        if (data.email != null && data.contrasenia != null){
            contador++
        }

        contador += TablaCredencialesAuth.selectAll().where {
            TablaCredencialesAuth.uid.eq(uid)
        }.count().toInt()

        return@suspendTransaction contador
    }
}
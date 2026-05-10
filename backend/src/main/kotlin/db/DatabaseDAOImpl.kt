package com.astrais.db

import CosmeticResponseDTO
import LANG_CODE_ENGLISH
import LANG_CODE_RUSSIAN
import LANG_CODE_SPANISH
import ROLE_USERMOD
import ROLE_USERNORMAL
import ROLE_USEROWNER
import admin.NamesCosmetic
import admin.RarityType
import com.astrais.mainlogger
import kotlinx.datetime.*
import java.time.LocalDate
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.jdbc.*
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction

class DatabaseDAOImpl : DatabaseDAO {
    // https://www.jetbrains.com/help/exposed/dsl-querying-data.html

    /** Tiempo actual en UTC */
    private fun nowUtc(): LocalDateTime =
        Clock.System.now().toLocalDateTime(TimeZone.UTC)

    private fun EntidadCosmetico.isDefaultAppTheme(): Boolean =
        tipo == CosmeticType.APP_THEME && coleccion.equals("DEFAULT", ignoreCase = true)

    private fun EntidadCosmetico.isOwnedByUser(inventarioUsuario: Collection<Int>): Boolean =
        inventarioUsuario.contains(id.value) || isDefaultAppTheme()

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
        return suspendTransaction {
            try {
                // Borramos credenciales de autenticacion (OAuth, email/passwd)
                TablaCredencialesAuth.deleteWhere { uid.eq(id) }

                // Borramos codigos de confirmacion pendientes
                TablaConfirmacionUsuario.deleteWhere { uid.eq(id) }

                // Borramos invitaciones creadas por el usuario
                TablaGrupoInvites.deleteWhere { created_by_uid.eq(id) }

                // Borramos al usuario de los grupos
                TablaGrupoUsuario.deleteWhere { uid.eq(id) }

                // Para grupos no personales donde el usuario es owner:
                // transferir a otro miembro o borrar el grupo si no hay mas miembros
                val ownedGroups = EntidadGrupo.find {
                    TablaGrupo.owner.eq(id).and(TablaGrupo.es_grupo_personal.eq(false))
                }.toList()

                ownedGroups.forEach { grp ->
                    val gid = grp.id.value
                    val newOwner = TablaGrupoUsuario.selectAll()
                        .where {
                            (TablaGrupoUsuario.gid.eq(gid)).and(TablaGrupoUsuario.uid.neq(id))
                        }
                        .limit(1)
                        .firstOrNull()

                    if (newOwner != null) {
                        grp.owner = newOwner[TablaGrupoUsuario.uid]
                    } else {
                        // No hay mas miembros, borramos el grupo (cascade borra tareas, invites, audit)
                        grp.delete()
                    }
                }

                // Borramos grupo personal
                TablaGrupo.deleteWhere {
                    owner.eq(id).and(es_grupo_personal.eq(true))
                }

                // Borramos el usuario
                TablaUsuario.deleteWhere { TablaUsuario.id.eq(id) } > 0
            } catch (e: Exception) {
                false
            }
        }
    }

    override suspend fun setUserLastLogin(ent: EntidadUsuario) {
        suspendTransaction { ent.ultimo_login = LocalDate.now().toKotlinLocalDate() }
    }

    override suspend fun checkIfUserIsServerAdmin(uid: Int) : Boolean {
        return suspendTransaction {
            //val user = EntidadUsuario.findById(uid) ?: return@suspendTransaction false
            val user = TablaUsuario.select(listOf(TablaUsuario.rol)).where {
                TablaUsuario.id.eq(uid)
            }.singleOrNull() ?: return@suspendTransaction false

            return@suspendTransaction user.get(TablaUsuario.rol) == UserRoles.ADMIN_USER
        }
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

                val newgroup = EntidadGrupo.new {
                    this.nombre = "Astrais User"
                    this.descripcion=""
                    this.owner = newuser.id
                    this.es_grupo_personal = true
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

    override suspend fun setUserResources(uid: Int, xpTotal: Int, xpActual: Int, level: Int, ludiones: Int): Boolean {
        return suspendTransaction {
            val ent = EntidadUsuario.findById(uid) ?: return@suspendTransaction false

            if (xpTotal > -1 && xpActual > -1 && level > -1){
                ent.xp_total = xpTotal
                ent.xp_actual = xpActual
                ent.nivel = level
            }
            if (ludiones > -1){
                ent.ludiones = ludiones
            }

            return@suspendTransaction true
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
                it[provider] = auth
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
                TablaCredencialesAuth.uid.eq(uid).and(provider.eq(auth))
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
            TablaGrupoUsuario
                .select(TablaGrupoUsuario.role)
                .where {
                    TablaGrupoUsuario.gid
                        .eq(EntityID(idgrupo, TablaGrupo))
                        .and(TablaGrupoUsuario.uid.eq(EntityID(idusuario, TablaUsuario)))
                }
                .singleOrNull()
                ?.get(TablaGrupoUsuario.role)
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
                            it[role] = GroupRoles.USER
                            it[joined_at] = nowUtc()
                        }
                        .insertedCount > 0
            } else {
                false
            }
        }
    }

    override suspend fun removeUserFromGroup(idusuario: Int, idgrupo: Int): Boolean {
        return suspendTransaction {
            TablaGrupoUsuario.deleteWhere {
                gid.eq(idgrupo).and(uid.eq(idusuario))
            } > 0
        }
    }

    override suspend fun passGroupOwnership(gid: Int, newOwnerId: Int): Boolean {
        return suspendTransaction {
            val group = EntidadGrupo.findById(gid) ?: return@suspendTransaction false
            val oldOwnerId = group.owner.value

            group.owner = EntityID(newOwnerId, TablaUsuario)

            // El nuevo owner se representa en Group.owner, no en RelGroupUser.
            TablaGrupoUsuario.deleteWhere {
                TablaGrupoUsuario.gid.eq(gid).and(uid.eq(newOwnerId))
            }

            // El owner anterior mantiene acceso como MOD.
            val oldOwnerRelationExists = !TablaGrupoUsuario.selectAll().where {
                TablaGrupoUsuario.gid.eq(gid).and(TablaGrupoUsuario.uid.eq(oldOwnerId))
            }.empty()

            if (!oldOwnerRelationExists) {
                TablaGrupoUsuario.insert {
                    it[TablaGrupoUsuario.gid] = EntityID(gid, TablaGrupo)
                    it[uid] = EntityID(oldOwnerId, TablaUsuario)
                    it[role] = GroupRoles.MOD
                    it[joined_at] = nowUtc()
                }
            } else {
                TablaGrupoUsuario.update({
                    TablaGrupoUsuario.gid.eq(gid).and(TablaGrupoUsuario.uid.eq(oldOwnerId))
                }) {
                    it[role] = GroupRoles.MOD
                }
            }

            true
        }
    }

    override suspend fun createTarea(
        gid: Int, titulo: String, descripcion: String, tipo: TaskType, prioridad: Int,
        recompensaXp: Int, recompensaLudion: Int, extraUnico: TareaUniqueData?, extraHabito: TareaHabitData?,
        idObjetivo: Int?
    ): Int {
        return suspendTransaction {
            if (tipo == TaskType.UNICO && extraUnico == null) return@suspendTransaction -1
            if (tipo == TaskType.HABITO && extraHabito == null) return@suspendTransaction -2

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

                if (idObjetivo != null) {
                    val objetivoPadre = EntidadTareaObjetivo.find {
                        TablaTareaObjetivo.id_tarea eq idObjetivo
                    }.singleOrNull()

                    if (objetivoPadre != null) {
                        this.id_objetivo = objetivoPadre.id
                    }
                }
            }

            when (tipo) {
                TaskType.UNICO -> {
                    EntidadTareaUnica.new {
                        this.id_tarea = nuevaTarea.id
                        this.fecha_vencimiento = extraUnico!!.fechaLimite
                    }
                }
                TaskType.HABITO -> {
                    EntidadTareaHabito.new {
                        this.id_tarea = nuevaTarea.id
                        this.variacion_freq = extraHabito!!.numeroFrecuencia
                        this.frecuencia = extraHabito.frequency
                        this.ultima_vez_completada = null
                    }
                }
                TaskType.OBJETIVO -> {
                    EntidadTareaObjetivo.new {
                        this.id_tarea = nuevaTarea.id
                    }
                }
            }
            nuevaTarea.id.value
        }
    }



    override suspend fun getGroupByTask(tid: Int): EntidadGrupo? {
        return suspendTransaction {
            val gid = TablaTarea.select(TablaTarea.id_grupo).where {
                TablaTarea.id.eq(tid)
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
            if (prioridad != null) {
                grp.prioridad = prioridad
                // TODO: Recalcular recompensas
            }

            grp.fecha_actualizado = LocalDate.now().toKotlinLocalDate()

            return@suspendTransaction true
        }
    }

    override suspend fun checkIfUserIsGroupAdmin(uid: Int, gid: Int): Boolean {
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

        // Persistimos cuánto se otorgó realmente para que el undo (uncomplete)
        // pueda devolver exactamente la misma cantidad.
        tarea.ludiones_otorgados = ludionesFinales

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
            TablaTareaUnica.deleteWhere { id_tarea eq tid }
            TablaTareaHabito.deleteWhere { id_tarea eq tid }
            TablaTareaObjetivo.deleteWhere { id_tarea eq tid }

            TablaTarea.deleteWhere { id eq tid } > 0
        }
    }

    override suspend fun buyCosmetic(uid: Int, cosmeticId: Int): BuyCosmeticResponse {
        return suspendTransaction {
            val usuario = EntidadUsuario.findById(uid) ?: return@suspendTransaction BuyCosmeticResponse.USER_NOT_FOUND
            val cosmetico = EntidadCosmetico.findById(cosmeticId) ?: return@suspendTransaction BuyCosmeticResponse.COSMETIC_NOT_FOUND

            if (cosmetico.isDefaultAppTheme()) {
                return@suspendTransaction BuyCosmeticResponse.OKAY
            }

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

    override suspend fun getStoreItems(uid: Int, translated : Boolean): List<CosmeticResponseDTO> {
        return suspendTransaction {
            val inventarioUsuario =
                    EntidadInventario.find { TablaInventario.id_usuario eq uid }.map {
                        it.id_cosmetico.value
                    }.toSet()
            val usuario = EntidadUsuario.findById(uid)

            EntidadCosmetico.all().map { cosmetico ->
                val isEquipped =
                        if (cosmetico.tipo == CosmeticType.PET) {
                            usuario?.id_mascota_equipada?.value == cosmetico.id.value
                        } else if (cosmetico.tipo == CosmeticType.APP_THEME) {
                            (cosmetico.isDefaultAppTheme() && usuario != null && usuario.themeColors == null) || usuario?.themeColors == cosmetico.tema
                        } else if (cosmetico.tipo == CosmeticType.AVATAR_PART) {
                            usuario?.id_avatar_equipado?.value == cosmetico.id.value
                        } else {
                            false
                        }

                val finalName = if (translated) {
                    val fname = Json.decodeFromString<NamesCosmetic>(cosmetico.nombre)

                    when (usuario?.idioma) {
                        LANG_CODE_SPANISH -> {
                            fname.espName
                        }
                        LANG_CODE_RUSSIAN -> {
                            fname.rusName
                        }
                        else -> {
                            fname.engName
                        }
                    }
                }else {
                    cosmetico.nombre
                }

                CosmeticResponseDTO(
                        id = cosmetico.id.value,
                        name = finalName,
                        desc = cosmetico.descripcion,
                        type = cosmetico.tipo.name,
                        price = cosmetico.precioLudiones,
                        assetRef = cosmetico.assetRef,
                        theme = cosmetico.tema,
                        coleccion = cosmetico.coleccion, 
                        rarity = cosmetico.rareza.name,
                        owned = cosmetico.isOwnedByUser(inventarioUsuario),
                        equipped = isEquipped
                )
            }
        }
    }

    override suspend fun equipCosmetic(uid: Int, cosmeticId: Int): BuyCosmeticResponse {
        return suspendTransaction {
            val usuario = EntidadUsuario.findById(uid) ?: return@suspendTransaction BuyCosmeticResponse.USER_NOT_FOUND

            if (cosmeticId == 0) return@suspendTransaction BuyCosmeticResponse.COSMETIC_NOT_FOUND

            val cosmetico = EntidadCosmetico.findById(cosmeticId) ?: return@suspendTransaction BuyCosmeticResponse.COSMETIC_NOT_FOUND

            val loTiene =
                    EntidadInventario.find {
                        (TablaInventario.id_usuario eq uid) and (TablaInventario.id_cosmetico eq cosmeticId)
                    }
                    .empty()
                    .not()

            if (loTiene || cosmetico.isDefaultAppTheme()) {
                if (cosmetico.tipo == CosmeticType.PET) {
                    if (usuario.id_mascota_equipada?.value == cosmeticId) {
                        usuario.id_mascota_equipada = null
                    } else {
                        usuario.id_mascota_equipada = EntityID(cosmeticId, TablaCosmetico)
                    }
                } else if (cosmetico.tipo == CosmeticType.APP_THEME) {
                    usuario.themeColors = if (cosmetico.isDefaultAppTheme()) null else cosmetico.tema
                } else if (cosmetico.tipo == CosmeticType.AVATAR_PART) {
                    if (usuario.id_avatar_equipado?.value == cosmeticId) {
                        usuario.id_avatar_equipado = null
                    } else {
                        usuario.id_avatar_equipado = EntityID(cosmeticId, TablaCosmetico)
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
            rarity: RarityType
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
                this.rareza = rarity
            }
            true
        }
    }

    override suspend fun getCosmetic(cid: Int): EntidadCosmetico? {
        return suspendTransaction {
            EntidadCosmetico.findById(cid)
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

    override suspend fun getUserEquippedAvatar(uid: Int): EntidadCosmetico? {
        return suspendTransaction {
            val id = TablaUsuario.select(TablaUsuario.id_avatar_equipado).singleOrNull()?.get(TablaUsuario.id_avatar_equipado) ?: return@suspendTransaction null
            return@suspendTransaction EntidadCosmetico.findById(id)
        }
    }

    override suspend fun adminUpdateCosmetic(
        cid: Int,
        name: String,
        desc: String,
        type: CosmeticType,
        price: Int,
        assetRef: String,
        theme: String,
        coleccion: String,
        rarity: RarityType
    ) : Boolean {
        return suspendTransaction {

            val identity = EntidadCosmetico.findById(cid) ?: return@suspendTransaction false
            identity.nombre = name
            identity.descripcion = desc
            //identity.tipo = type
            identity.precioLudiones = price
            if (type != CosmeticType.APP_THEME && assetRef.isNotBlank()){
                identity.assetRef = assetRef
            }
            if (type == CosmeticType.APP_THEME && theme.isNotBlank()){
                identity.tema = theme
            }
            identity.coleccion = coleccion
            identity.rareza = rarity
            true
        }
    }

    override suspend fun admindeleteCosmetic(cid: Int) : Boolean {
        return suspendTransaction {
            TablaCosmetico.deleteWhere {
                id.eq(cid)
            } > 0
        }
    }

    override suspend fun adminGetAllUsers() : List<DatosSimpleUsuarios> {
        return suspendTransaction {
            TablaUsuario.select(listOf(TablaUsuario.id, TablaUsuario.nombre, TablaUsuario.rol, TablaUsuario.nivel, TablaUsuario.esta_confirmado, TablaUsuario.xp_total, TablaUsuario.ludiones)).map {

                val rolFinal = if (it.get(TablaUsuario.rol) == UserRoles.ADMIN_USER){
                    "Admin"
                } else {
                    "Usuario"
                }

                DatosSimpleUsuarios(
                    id = it.get(TablaUsuario.id).value,
                    nombre = it.get(TablaUsuario.nombre),
                    rol = rolFinal,
                    nivel = it.get(TablaUsuario.nivel),
                    confirmed = it.get(TablaUsuario.esta_confirmado) == 1,
                    xp = it.get(TablaUsuario.xp_total),
                    ludiones = it.get(TablaUsuario.ludiones)
                )
            }
        }
    }

    override suspend fun adminGetAllGroups(): List<DatosSimpleGrupo> {
        return suspendTransaction {
            TablaGrupo.select(listOf(TablaGrupo.id, TablaGrupo.nombre, TablaGrupo.descripcion, TablaGrupo.owner)).where { TablaGrupo.es_grupo_personal.eq(false) }.map {
                val ownerName = TablaUsuario.select(TablaUsuario.nombre).where { TablaUsuario.id.eq(it.get(TablaGrupo.owner)) }.singleOrNull()?.get(TablaUsuario.nombre) ?: "No hay owner"
                DatosSimpleGrupo(
                    id = it.get(TablaGrupo.id).value,
                    nombre = it.get(TablaGrupo.nombre),
                    descripcion = it.get(TablaGrupo.descripcion),
                    ownerId = it.get(TablaGrupo.owner).value,
                    ownerNombre = ownerName
                )
            }
        }
    }

    override suspend fun createGroupInvite(
        gid: Int,
        code: String,
        codeHash: String,
        createdByUid: Int,
        createdAt: LocalDateTime,
        expiresAt: LocalDateTime?,
        maxUses: Int?,
    ): Int {
        return suspendTransaction {
            TablaGrupoInvites.insertAndGetId {
                it[TablaGrupoInvites.gid] = EntityID(gid, TablaGrupo)
                it[TablaGrupoInvites.code] = code
                it[code_hash] = codeHash
                it[created_by_uid] = EntityID(createdByUid, TablaUsuario)
                it[created_at] = createdAt
                it[expires_at] = expiresAt
                it[max_uses] = maxUses
            }.value
        }
    }

    override suspend fun revokeGroupInvite(
        gid: Int,
        codeHash: String,
        revokedAt: LocalDateTime,
    ): Boolean {
        return suspendTransaction {
            TablaGrupoInvites.update({
                (TablaGrupoInvites.gid eq EntityID(gid, TablaGrupo)) and
                    (TablaGrupoInvites.code_hash eq codeHash) and
                    TablaGrupoInvites.revoked_at.isNull()
            }) {
                it[revoked_at] = revokedAt
            } > 0
        }
    }

    override suspend fun listGroupInvites(gid: Int, includeRevoked: Boolean): List<GroupInviteDb> {
        return suspendTransaction {
            val base = TablaGrupoInvites.selectAll().where { TablaGrupoInvites.gid eq EntityID(gid, TablaGrupo) }
            val rows = if (includeRevoked) base else base.andWhere { TablaGrupoInvites.revoked_at.isNull() }
            rows.map {
                GroupInviteDb(
                    id = it[TablaGrupoInvites.id].value,
                    gid = it[TablaGrupoInvites.gid].value,
                    code = it[TablaGrupoInvites.code],
                    codeHash = it[TablaGrupoInvites.code_hash],
                    createdByUid = it[TablaGrupoInvites.created_by_uid].value,
                    createdAt = it[TablaGrupoInvites.created_at],
                    expiresAt = it[TablaGrupoInvites.expires_at],
                    revokedAt = it[TablaGrupoInvites.revoked_at],
                    maxUses = it[TablaGrupoInvites.max_uses],
                    usesCount = it[TablaGrupoInvites.uses_count],
                )
            }
        }
    }

    override suspend fun getGroupInviteByHash(codeHash: String): GroupInviteDb? {
        return suspendTransaction {
            TablaGrupoInvites.selectAll().where { TablaGrupoInvites.code_hash eq codeHash }.singleOrNull()?.let {
                GroupInviteDb(
                    id = it[TablaGrupoInvites.id].value,
                    gid = it[TablaGrupoInvites.gid].value,
                    code = it[TablaGrupoInvites.code],
                    codeHash = it[TablaGrupoInvites.code_hash],
                    createdByUid = it[TablaGrupoInvites.created_by_uid].value,
                    createdAt = it[TablaGrupoInvites.created_at],
                    expiresAt = it[TablaGrupoInvites.expires_at],
                    revokedAt = it[TablaGrupoInvites.revoked_at],
                    maxUses = it[TablaGrupoInvites.max_uses],
                    usesCount = it[TablaGrupoInvites.uses_count],
                )
            }
        }
    }

    override suspend fun tryConsumeInvite(inviteId: Int, now: LocalDateTime): Boolean {
        return suspendTransaction {
            TablaGrupoInvites.update({
                (TablaGrupoInvites.id eq inviteId) and
                    TablaGrupoInvites.revoked_at.isNull() and
                    ((TablaGrupoInvites.expires_at.isNull()) or (TablaGrupoInvites.expires_at greater now)) and
                    ((TablaGrupoInvites.max_uses.isNull()) or (TablaGrupoInvites.uses_count less TablaGrupoInvites.max_uses))
            }) {
                it.update(uses_count, uses_count + 1)
            } > 0
        }
    }

    override suspend fun appendGroupAuditEvent(
        gid: Int,
        actorUid: Int?,
        eventType: String,
        payloadJson: String?,
        createdAt: LocalDateTime,
    ): Int {
        return suspendTransaction {
            TablaGrupoAuditLog.insertAndGetId {
                it[TablaGrupoAuditLog.gid] = EntityID(gid, TablaGrupo)
                it[actor_uid] = actorUid?.let { uid -> EntityID(uid, TablaUsuario) }
                it[event_type] = eventType
                it[payload_json] = payloadJson
                it[created_at] = createdAt
            }.value
        }
    }

    override suspend fun listGroupAuditEvents(gid: Int, limit: Int, offset: Long): List<GroupAuditEventDb> {
        return suspendTransaction {
            TablaGrupoAuditLog.selectAll()
                .where { TablaGrupoAuditLog.gid eq EntityID(gid, TablaGrupo) }
                .orderBy(TablaGrupoAuditLog.id, SortOrder.DESC)
                .limit(limit)
                .offset(offset)
                .map {
                    GroupAuditEventDb(
                        id = it[TablaGrupoAuditLog.id].value,
                        gid = it[TablaGrupoAuditLog.gid].value,
                        actorUid = it[TablaGrupoAuditLog.actor_uid]?.value,
                        eventType = it[TablaGrupoAuditLog.event_type],
                        payloadJson = it[TablaGrupoAuditLog.payload_json],
                        createdAt = it[TablaGrupoAuditLog.created_at],
                    )
                }
        }
    }

    override suspend fun listGroupMembers(gid: Int): List<GroupMemberDb> {
        return suspendTransaction {
            val group = EntidadGrupo.findById(gid) ?: return@suspendTransaction emptyList()

            val owner = EntidadUsuario.findById(group.owner.value)
            val ownerRow = owner?.let {
                GroupMemberDb(
                    uid = it.id.value,
                    name = it.nombre,
                    role = ROLE_USEROWNER,
                    joinedAt = nowUtc(),
                )
            }

            val members = (TablaGrupoUsuario innerJoin TablaUsuario).selectAll().where {
                TablaGrupoUsuario.gid.eq(EntityID(gid, TablaGrupo))
            }.map {
                GroupMemberDb(
                    uid = it[TablaUsuario.id].value,
                    name = it[TablaUsuario.nombre],
                    role = if (it[TablaGrupoUsuario.role] == GroupRoles.MOD) ROLE_USERMOD else ROLE_USERNORMAL,
                    joinedAt = it[TablaGrupoUsuario.joined_at],
                )
            }

            if (ownerRow == null) members else listOf(ownerRow) + members
        }
    }

    override suspend fun setGroupMemberRole(gid: Int, uid: Int, role: GroupRoles): Boolean {
        return suspendTransaction {
            TablaGrupoUsuario.update({
                TablaGrupoUsuario.gid.eq(gid).and(TablaGrupoUsuario.uid.eq(uid))
            }) {
                it[TablaGrupoUsuario.role] = role
            } > 0
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

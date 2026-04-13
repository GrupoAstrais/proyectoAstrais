package com.astrais.db

import java.time.LocalDate
import kotlinx.datetime.toKotlinLocalDate
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.dao.id.CompositeID
import org.jetbrains.exposed.v1.core.dao.id.CompositeIdTable
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.dao.*
import org.jetbrains.exposed.v1.datetime.date
import org.jetbrains.exposed.v1.datetime.datetime

// Constantes unicos de la base de datos
// TODO: Revisitar esto y mover los que se necesite

const val USER_NAME_LENGTH = 256
const val USER_MAIL_LENGTH = 128
const val AWARD_TITLE_LENGTH = 72
const val TAREA_TITLE_LENGTH = 72

const val GROUP_NAME_LENGTH = 256

const val IMAGE_PATH_SIZE = 48

const val CONFIRM_CODE_SIZE = 6

enum class AuthProvider {
    GOOGLE
}

enum class UserRoles {
    NORMAL_USER,
    ADMIN_USER
}

enum class GroupRoles {
    MOD,
    USER
}

enum class TaskType {
    UNICO,
    OBJETIVO,
    HABITO
}

enum class TaskState {
    ACTIVE,
    COMPLETE,
    DUE
}

enum class CosmeticType {
    PET,
    PET_SKIN,
    APP_THEME
}

object TablaUsuario : IntIdTable("Users") {
    // El nombre del usuario
    val nombre = varchar("name", USER_NAME_LENGTH)
    // Rol del usuario
    val rol = enumerationByName<UserRoles>("role", 20).default(UserRoles.NORMAL_USER)
    // Numero de horas de desplazamiento desde UTC+0 (Tiempo universal coordinado) (España seria
    // UTC+1 por referencia)
    val zona_horaria = float("utc_offset").default(0f)
    // El idioma siguiendo la ISO 639-2/3 (Con 3 caracteres como: ESP, ENG, ITA, FRA, RUS, POR, CHN)
    // (https://es.wikipedia.org/wiki/ISO_639-3)
    val idioma = varchar("language", 3)
    // El nivel actual del usuario
    val nivel = integer("level").default(0)
    // El XP que llevas para el siguiente nivel
    val xp_actual = integer("current_xp").default(0)
    // El total de XP del usuario
    val xp_total = integer("total_xp").default(0)
    // Los ludiones que posee el usuario
    val ludiones = integer("ludions").default(0)
    // El total de tareas que han sido completadas
    val total_tareas_completadas = integer("total_task_done").default(0)
    // La racha actual de logins
    val racha_login_actual = integer("current_streak").default(0)
    // La racha maxima de logins alcanzada por el usuario
    val racha_login_mayor = integer("greatest_streak").default(0)
    // Fecha del ultimo login hecho
    val ultimo_login = date("last_login").nullable()

    // Email del usuario
    val email = varchar("email", USER_MAIL_LENGTH).nullable()
    // BCRYPT tiene un limite de 72 bytes
    val contrasenia = varchar("hash_passwd", 72).nullable()
    // Dice si el mail del usuario esta confirmado. No deberia dejar meterse por mail si el email no
    // es null y no esta confirmado.
    val esta_confirmado = integer("is_mail_confirmed").default(0)

    // Qué mascota está equipada. Le hago referencia en la base de datos para que se refleje en web
    // y android
    val id_mascota_equipada =
            optReference("equipped_pet_id", TablaCosmetico, onDelete = ReferenceOption.SET_NULL)

    // JSON con los colores
    val themeColors = varchar("theme_colors", 255).nullable()
    // TODO: Piezas de avatar
}

class EntidadUsuario(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<EntidadUsuario>(TablaUsuario)

    var nombre by TablaUsuario.nombre
    var rol by TablaUsuario.rol
    var zona_horaria by TablaUsuario.zona_horaria
    var idioma by TablaUsuario.idioma
    var nivel by TablaUsuario.nivel
    var xp_actual by TablaUsuario.xp_actual
    var xp_total by TablaUsuario.xp_total
    var ludiones by TablaUsuario.ludiones
    var total_tareas_completadas by TablaUsuario.total_tareas_completadas
    var racha_login_actual by TablaUsuario.racha_login_actual
    var racha_login_mayor by TablaUsuario.racha_login_mayor
    var ultimo_login by TablaUsuario.ultimo_login
    var email by TablaUsuario.email
    var contrasenia by TablaUsuario.contrasenia
    var esta_confirmado by TablaUsuario.esta_confirmado
    var id_mascota_equipada by TablaUsuario.id_mascota_equipada
    var themeColors by TablaUsuario.themeColors
}

object TablaConfirmacionUsuario : IntIdTable("UserConfirm") {
    val uid = reference("user_id", TablaUsuario, onDelete = ReferenceOption.CASCADE)
    val codigo_confirmacion = varchar("confirm_code", CONFIRM_CODE_SIZE)
}

class EntidadConfirmacionUsuario(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<EntidadConfirmacionUsuario>(TablaConfirmacionUsuario)

    var uid by TablaConfirmacionUsuario.uid
    var codigo_confirmacion by TablaConfirmacionUsuario.codigo_confirmacion
}

object TablaCredencialesAuth : Table("AuthCredentials") {
    val uid = reference("user_id", TablaUsuario, onDelete = ReferenceOption.CASCADE)
    val provider = enumerationByName<AuthProvider>("provider", 16)
    val provider_uid = varchar("provider_user_id", 128).nullable()

    override val primaryKey = PrimaryKey(uid, provider)
}

//class EntidadCredencialesAuth(id: EntityID<CompositeID>) : CompositeEntity(id) {
//    companion object : CompositeEntityClass<EntidadCredencialesAuth>(TablaCredencialesAuth)
//
//    var uid by TablaCredencialesAuth.uid
//    var provider by TablaCredencialesAuth.provider
//    var provider_uid by TablaCredencialesAuth.provider_uid
//}

object TableLogro : IntIdTable("Awards") {
    val titulo = varchar("title", AWARD_TITLE_LENGTH)
    val descripcion = text("desc")
    val icono = varchar("icon", IMAGE_PATH_SIZE)
    val condicion = text("cond")
    val completado = bool("is_complete")
    val recompensa_ludiones = integer("rec_ludion")
    val es_activo = bool("is_active")
}

object TablaGrupo : IntIdTable("Group") {
    val es_grupo_personal = bool("isPersonal")
    val nombre = varchar("name", GROUP_NAME_LENGTH)
    val descripcion = text("desc").default("")
    val owner = reference("owner_id", TablaUsuario)
    //val prioridad = integer("priority")
}

class EntidadGrupo(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<EntidadGrupo>(TablaGrupo)

    var es_grupo_personal by TablaGrupo.es_grupo_personal
    var nombre by TablaGrupo.nombre
    var descripcion by TablaGrupo.descripcion
    var owner by TablaGrupo.owner
    //var prioridad by TablaGrupo.prioridad
}

object TablaGrupoUsuario : CompositeIdTable("RelGroupUser") {
    val gid = reference("group_id", TablaGrupo, onDelete = ReferenceOption.CASCADE)
    val uid = reference("user_id", TablaUsuario, onDelete = ReferenceOption.CASCADE)
    val role = enumerationByName<GroupRoles>("role", 25)
    override val primaryKey = PrimaryKey(gid, uid)
}

class EntidadGrupoUsuario(id: EntityID<CompositeID>) : CompositeEntity(id) {
    companion object : CompositeEntityClass<EntidadGrupoUsuario>(TablaGrupoUsuario)

    var gid by TablaGrupoUsuario.gid
    var uid by TablaGrupoUsuario.uid
    var role by TablaGrupoUsuario.role
}

object TablaTarea : IntIdTable("Task") {
    val id_grupo = reference("gid", TablaGrupo, onDelete = ReferenceOption.CASCADE)
    val titulo = varchar("title", TAREA_TITLE_LENGTH)
    val descripcion = text("desc").default("")
    val tipo = enumerationByName<TaskType>("type", 16)
    val estado = enumerationByName<TaskState>("state", 16)
    val prioridad = integer("priority")
    val fecha_creacion = date("creation_date").default(LocalDate.now().toKotlinLocalDate())
    val fecha_actualizado = date("update_date").default(LocalDate.now().toKotlinLocalDate())
    val fecha_completado = date("done_date").nullable()
    val recompensa_xp = integer("reward_xp").default(0)
    val recompensa_ludion = integer("reward_ludion").default(0)

    val recompensa_reclamada = bool("reward_claimed").default(false)

    val id_objetivo =
            optReference("objective_id", TablaTareaObjetivo, onDelete = ReferenceOption.CASCADE)
}

class EntidadTarea(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<EntidadTarea>(TablaTarea)

    var id_grupo by TablaTarea.id_grupo
    var titulo by TablaTarea.titulo
    var descripcion by TablaTarea.descripcion
    var tipo by TablaTarea.tipo
    var estado by TablaTarea.estado
    var prioridad by TablaTarea.prioridad
    var fecha_creacion by TablaTarea.fecha_creacion
    var fecha_actualizado by TablaTarea.fecha_actualizado
    var fecha_completado by TablaTarea.fecha_completado
    var recompensa_xp by TablaTarea.recompensa_xp
    var recompensa_ludion by TablaTarea.recompensa_ludion

    var recompensa_reclamada by TablaTarea.recompensa_reclamada

    var id_objetivo by TablaTarea.id_objetivo
}

object TablaTareaUnica : IntIdTable("TaskUnique") {
    val id_tarea = reference("tid", TablaTarea, onDelete = ReferenceOption.CASCADE)
    val fecha_vencimiento = datetime("due_date").nullable()
}
class EntidadTareaUnica(id : EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<EntidadTareaUnica>(TablaTareaUnica)

    var id_tarea by TablaTareaUnica.id_tarea
    var fecha_vencimiento by TablaTareaUnica.fecha_vencimiento
}

object TablaTareaObjetivo : IntIdTable("TaskObjective") {
    val id_tarea = reference("tid", TablaTarea, onDelete = ReferenceOption.CASCADE)

}
class EntidadTareaObjetivo(id : EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<EntidadTareaObjetivo>(TablaTareaObjetivo)

    var id_tarea by TablaTareaObjetivo.id_tarea
}


object TablaTareaHabito : IntIdTable("TaskHabit") {
    val id_tarea = reference("tid", TablaTarea, onDelete = ReferenceOption.CASCADE)
    val racha_actual = integer("current_streak").default(0)
    val mejor_racha = integer("best_streak").default(0)
    val ultima_vez_completada = date("last_completion").nullable()

    val variacion_freq = integer("frqvar")
    val frecuencia = varchar("frequency", 10)
}
class EntidadTareaHabito(id : EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<EntidadTareaHabito>(TablaTareaHabito)

    var id_tarea by TablaTareaHabito.id_tarea
    var racha_actual by TablaTareaHabito.racha_actual
    var mejor_racha by TablaTareaHabito.mejor_racha
    var ultima_vez_completada by TablaTareaHabito.ultima_vez_completada

    var variacion_freq by TablaTareaHabito.variacion_freq
    var frecuencia by TablaTareaHabito.frecuencia
}

object TablaCosmetico : IntIdTable("Cosmetic") {
    val nombre = varchar("name", 50)
    val descripcion = varchar("desc", 255)
    val tipo = enumerationByName<CosmeticType>("type", 20)
    val precioLudiones = integer("price_ludions").default(0)
    val assetRef = varchar("asset_ref", 100)
    val tema = varchar("theme", 255).default("DEFAULT")
    val coleccion = varchar("coleccion", 50).default("DEFAULT")
}

class EntidadCosmetico(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<EntidadCosmetico>(TablaCosmetico)

    var nombre by TablaCosmetico.nombre
    var descripcion by TablaCosmetico.descripcion
    var tipo by TablaCosmetico.tipo
    var precioLudiones by TablaCosmetico.precioLudiones
    var assetRef by TablaCosmetico.assetRef
    var tema by TablaCosmetico.tema
    var coleccion by TablaCosmetico.coleccion
}

object TablaInventario : IntIdTable("Inventory") {
    val id_usuario = reference("user_id", TablaUsuario, onDelete = ReferenceOption.CASCADE)
    val id_cosmetico = reference("cosmetic_id", TablaCosmetico, onDelete = ReferenceOption.CASCADE)
    val fecha_compra = date("purchase_date")
}

class EntidadInventario(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<EntidadInventario>(TablaInventario)

    var id_usuario by TablaInventario.id_usuario
    var id_cosmetico by TablaInventario.id_cosmetico
    var fecha_compra by TablaInventario.fecha_compra
}

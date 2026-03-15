package com.astrais.db

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.dao.Entity
import org.jetbrains.exposed.v1.dao.IntEntity
import org.jetbrains.exposed.v1.dao.IntEntityClass
import org.jetbrains.exposed.v1.datetime.date

const val USER_NAME_LENGTH = 256
const val USER_MAIL_LENGTH = 128

const val AWARD_TITLE_LENGTH = 72
const val IMAGE_PATH_SIZE = 48

const val CONFIRM_CODE_SIZE = 6

enum class AuthProvider{
    SERVIDOR,
    GOOGLE
}
enum class UserRoles {
    NORMAL_USER,
    ADMIN_USER
}

const val LANG_CODE_SPANISH = "ESP"
const val LANG_CODE_ENGLISH = "ENG"
const val LANG_CODE_RUSSIAN = "RUS"

object TablaUsuario : IntIdTable("Users") {
    // El nombre del usuario
    val nombre = varchar("name", USER_NAME_LENGTH)
    // Rol del usuario
    val role = enumerationByName<UserRoles>("role", 20).default(UserRoles.NORMAL_USER)
    // Numero de horas de desplazamiento desde UTC+0 (Tiempo universal coordinado) (España seria UTC+1 por referencia)
    val zona_horaria = float("utc_offset").default(0f)
    // El idioma siguiendo la ISO 639-2/3 (Con 3 caracteres como: ESP, ENG, ITA, FRA, RUS, POR, CHN) (https://es.wikipedia.org/wiki/ISO_639-3)
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
    val ultimo_login = date("last_login")

    // Email del usuario
    val email = varchar("email", USER_MAIL_LENGTH).nullable()
    // BCRYPT tiene un limite de 72 bytes
    val contrasenia = varchar("hash_passwd", 72).nullable()
    // Dice si el mail del usuario esta confirmado. No deberia dejar meterse si no.
    val esta_confirmado = integer("is_mail_confirmed").default(0)

    // TODO: Piezas de avatar
}

class EntidadUsuario(id : EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<EntidadUsuario>(TablaUsuario)

    var nombre                   by TablaUsuario.nombre
    val role                     by TablaUsuario.role
    var zona_horaria             by TablaUsuario.zona_horaria
    var idioma                   by TablaUsuario.idioma
    var nivel                    by TablaUsuario.nivel
    var xp_actual                by TablaUsuario.xp_actual
    var xp_total                 by TablaUsuario.xp_total
    var ludiones                 by TablaUsuario.ludiones
    var total_tareas_completadas by TablaUsuario.total_tareas_completadas
    var racha_login_actual       by TablaUsuario.racha_login_actual
    var racha_login_mayor        by TablaUsuario.racha_login_mayor
    var ultimo_login             by TablaUsuario.ultimo_login
    var email                    by TablaUsuario.email
    var contrasenia              by TablaUsuario.contrasenia
    var esta_confirmado          by TablaUsuario.esta_confirmado
}


object TablaConfirmacionUsuario : Table("UserConfirm") {
    val uid = reference("user_id", TablaUsuario, onDelete = ReferenceOption.CASCADE)
    val codigo_confirmacion = varchar("confirm_code", CONFIRM_CODE_SIZE)

    override val primaryKey = PrimaryKey(uid)
}

object TablaCredencialesAuth : Table("AuthCredentials") {
    val uid = reference("user_id", TablaUsuario, onDelete = ReferenceOption.CASCADE)
    val provider = enumerationByName<AuthProvider>("provider", 16).default(AuthProvider.SERVIDOR)

    override val primaryKey = PrimaryKey(arrayOf(uid, provider))
}

object TableLogro : IntIdTable("Awards") {
    val titulo = varchar("title", AWARD_TITLE_LENGTH)
    val descripcion = text("desc")
    val icono = varchar("icon", IMAGE_PATH_SIZE)
    val condicion = text("cond")
    val completado = bool("is_complete")
    val recompensa_ludiones = integer("rec_ludion")
    val es_activo = bool("is_active")
}
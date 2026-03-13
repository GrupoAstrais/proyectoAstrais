package com.astrais.db

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.datetime.date

const val USER_NAME_LENGTH = 256
const val USER_MAIL_LENGTH = 128

const val AUTH_PROD_SELF = "Servidor"
const val AUTH_PROD_GOOGLE = "Google"

const val AWARD_TITLE_LENGTH = 72

const val IMAGE_PATH_SIZE = 48

object TablaUsuario : IntIdTable("Users") {
    // El nombre del usuario
    val nombre = varchar("name", USER_NAME_LENGTH)
    // Numero de horas de desplazamiento desde UTC+0 (Tiempo universal coordinado) (España seria UTC+1 por referencia)
    val zona_horaria = integer("utf_offset").default(0)
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

    // TODO: Piezas de avatar
}

object TablaCredencialesAuth : Table("AuthCredentials") {
    val uid = reference("user_id", TablaUsuario, onDelete = ReferenceOption.CASCADE)
    val provider = varchar("provider", 16).default(AUTH_PROD_SELF)

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
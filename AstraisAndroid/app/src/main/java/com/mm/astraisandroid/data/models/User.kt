package com.mm.astraisandroid.data.models

/**
 * Modelo de dominio que representa al usuario actual de la aplicación.
 *
 * @property id Identificador único del usuario.
 * @property name Nombre de usuario (nickname) visible en la app.
 * @property level Nivel actual alcanzado por el usuario.
 * @property currentXp Puntos de experiencia actuales en el nivel en curso.
 * @property totalXp Puntos de experiencia totales acumulados históricamente.
 * @property ludiones Moneda virtual del juego, utilizada para comprar en la tienda.
 * @property personalGid Identificador del grupo personal asociado al usuario.
 * @property equippedPetRef Referencia al asset de la mascota que el usuario tiene equipada.
 * @property equippedAvatarRef Referencia al asset del avatar que el usuario tiene equipado.
 * @property theme Tema de colores equipado actualmente.
 */
data class User(
    val id: Int,
    val name: String,
    val level: Int,
    val currentXp: Int,
    val totalXp: Int,
    val ludiones: Int,
    val personalGid: Int?,
    val equippedPetRef: String?,
    val equippedAvatarRef: String?,
    val theme: Theme?
)

/**
 * Modelo de dominio que define la paleta de colores actual de la aplicación.
 *
 * @property primary Color principal, usado para elementos clave como botones destacados.
 * @property secondary Color secundario, usado para elementos de soporte o acentos.
 * @property tertiary Color terciario, usado para detalles o variaciones adicionales.
 * @property background Color de fondo general de la aplicación.
 * @property backgroundAlt Color de fondo alternativo.
 * @property surface Color de las superficies superpuestas al fondo.
 * @property text Color principal del texto para asegurar buena legibilidad.
 * @property error Color utilizado para indicar estados de error o acciones destructivas.
 */
data class Theme(
    val primary: String,
    val secondary: String,
    val tertiary: String,
    val background: String,
    val backgroundAlt: String,
    val surface: String,
    val text: String,
    val error: String
)
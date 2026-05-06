package com.mm.astraisandroid.data.api

import kotlinx.serialization.Serializable

/**
 * DTO para la edición del perfil de usuario.
 *
 * Los campos nulos no se modifican en el servidor, permitiendo actualizaciones selectivas.
 *
 * @property uid Identificador del usuario a editar.
 * @property nombreusu Nuevo nombre de usuario, o el actual si no se desea cambiar.
 * @property lang Nuevo código de idioma, o `null` para mantener el actual.
 * @property utcOffset Nuevo desplazamiento horario, o `null` para mantener el actual.
 */
@Serializable
data class EditUserRequest(
    val uid: Int,
    val nombreusu: String,
    val lang: String?,
    val utcOffset: Float?
)

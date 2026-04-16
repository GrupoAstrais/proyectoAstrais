package com.mm.astraisandroid.data.api

import kotlinx.serialization.Serializable

/**
 * DTO que representa la estructura de una respuesta de error devuelta por el servidor.
 *
 * @property errorCode Código numérico específico del error.
 * @property errorText Descripción detallada del error.
 * @property error Mensaje de error corto o clave del error.
 */
@Serializable
data class ServerErrorResponse(
    val errorCode: Int? = null,
    val errorText: String? = null,
    val error: String? = null
)

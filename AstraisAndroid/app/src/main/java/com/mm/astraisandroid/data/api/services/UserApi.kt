package com.mm.astraisandroid.data.api.services

import com.mm.astraisandroid.data.api.*
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.patch
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import javax.inject.Inject

/**
 * Cliente HTTP para la API de perfil de usuario.
 *
 * Encapsula las llamadas de red para obtener el perfil del usuario autenticado
 * y actualizar el nombre o preferencias de idioma.
 *
 * @property client Cliente HTTP Ktor configurado con autenticación JWT.
 */
class UserApi @Inject constructor(private val client: HttpClient) {

    /**
     * Obtiene la información del perfil del usuario autenticado.
     *
     * @return [UserMeResponse] con los datos del perfil del usuario.
     * @throws IllegalStateException Si el servidor devuelve un estado HTTP no exitoso.
     */
    suspend fun getMe(): UserMeResponse {
        val req = client.get("$BASE_URL/auth/me")
        if (req.status != HttpStatusCode.OK) {
            val errResponse = req.body<ServerErrorResponse>()
            error(errResponse.errorText ?: errResponse.error ?: "Error desconocido")
        }
        return req.body<UserMeResponse>()
    }

    /**
     * Actualiza el nombre de usuario del usuario autenticado.
     *
     * @param uid Identificador del usuario.
     * @param newName Nuevo nombre de usuario.
     * @throws IllegalStateException Si el servidor devuelve un código de error HTTP.
     */
    suspend fun updateUsername(uid: Int, newName: String) {
        val req = client.patch("$BASE_URL/auth/editUser") {
            setBody(EditUserRequest(uid = uid, nombreusu = newName, lang = null, utcOffset = null))
            header(HttpHeaders.ContentType, ContentType.Application.Json)
        }
        if (req.status != HttpStatusCode.OK) {
            error("Fallo al actualizar el nombre")
        }
    }

    /**
     * Actualiza el perfil completo del usuario (nombre e idioma).
     *
     * @param uid Identificador del usuario.
     * @param newName Nuevo nombre de usuario.
     * @param language Nuevo código de idioma.
     * @throws IllegalStateException Si el servidor devuelve un código de error HTTP.
     */
    suspend fun updateProfile(uid: Int, newName: String, language: String) {
        val req = client.patch("$BASE_URL/auth/editUser") {
            setBody(EditUserRequest(uid = uid, nombreusu = newName, lang = language, utcOffset = null))
            header(HttpHeaders.ContentType, ContentType.Application.Json)
        }
        if (req.status != HttpStatusCode.OK) {
            error("Fallo al actualizar el perfil")
        }
    }
}
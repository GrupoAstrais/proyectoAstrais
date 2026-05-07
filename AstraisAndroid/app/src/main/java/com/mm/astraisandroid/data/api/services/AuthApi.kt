package com.mm.astraisandroid.data.api.services

import com.mm.astraisandroid.data.api.*
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import javax.inject.Inject

/**
 * Cliente HTTP para la API de autenticación.
 *
 * Encapsula las llamadas de red relacionadas con login, registro, verificación de email
 * y autenticación con Google.
 *
 * @property client Cliente HTTP de Ktor configurado con autenticación JWT.
 */
class AuthApi @Inject constructor(private val client: HttpClient) {

    /**
     * Realiza el inicio de sesión con credenciales de email y contraseña.
     *
     * @param request Datos de acceso (email y contraseña).
     * @return [LoginResponse] con el token JWT y datos del usuario.
     * @throws IllegalStateException Si el servidor devuelve un código de error HTTP.
     */
    suspend fun performLogin(request: LoginRequest): LoginResponse {
        val req = client.post("$BASE_URL/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        if (req.status != HttpStatusCode.OK) {
            val errResponse = req.body<ServerErrorResponse>()
            error(errResponse.errorText ?: errResponse.error ?: "Error desconocido")
        }
        return req.body<LoginResponse>()
    }

    /**
     * Registra un nuevo usuario en el sistema.
     *
     * @param request Datos de registro (email, contraseña, nombre).
     * @throws IllegalStateException Si el servidor devuelve un código de error HTTP.
     */
    suspend fun performRegister(request: RegisterRequest) {
        val req = client.post("$BASE_URL/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        if (req.status != HttpStatusCode.OK) {
            val errResponse = req.body<ServerErrorResponse>()
            error(errResponse.errorText ?: errResponse.error ?: "Error desconocido")
        }
    }

    /**
     * Verifica el código de confirmación de email enviado al usuario.
     *
     * @param request Código de verificación y datos asociados.
     * @throws IllegalStateException Si el servidor devuelve un código de error HTTP.
     */
    suspend fun verifyEmail(request: MailVerifierRequest) {
        val req = client.post("$BASE_URL/auth/verify") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        if (req.status != HttpStatusCode.OK) {
            val errResponse = req.body<ServerErrorResponse>()
            error(errResponse.errorText ?: errResponse.error ?: "Error al verificar el código")
        }
    }

    /**
     * Realiza el inicio de sesión mediante autenticación de Google (OAuth).
     *
     * @param idToken Token de identidad proporcionado por Google Sign-In.
     * @return [LoginResponse] con el token JWT y datos del usuario.
     * @throws IllegalStateException Si el servidor devuelve un código de error HTTP.
     */
    suspend fun performGoogleLogin(idToken: String): LoginResponse {
        val req = client.post("$BASE_URL/auth/google/androidlogin") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("idToken" to idToken))
        }
        if (req.status != HttpStatusCode.OK) {
            val errResponse = req.body<ServerErrorResponse>()
            error(errResponse.errorText ?: errResponse.error ?: "Error al verificar en el backend")
        }
        return req.body<LoginResponse>()
    }
}
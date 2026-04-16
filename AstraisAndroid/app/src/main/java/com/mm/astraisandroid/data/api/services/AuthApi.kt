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

class AuthApi @Inject constructor(private val client: HttpClient) {

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
package com.mm.astraisandroid.data.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.contentType
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import javax.inject.Inject

// 192.168.1.131
// 192.168.0.97
const val BASE_URL = "http://192.168.1.129:5684"

class BackendRepository(private val client: HttpClient) {

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

    suspend fun getTareas(gid: Int): List<TareaResponse> {
        val req = client.get("$BASE_URL/tasks/$gid") {
            contentType(ContentType.Application.Json)
        }
        if (req.status != HttpStatusCode.OK) {
            val errResponse = req.body<ServerErrorResponse>()
            error(errResponse.errorText ?: errResponse.error ?: "Error desconocido")
        }
        return req.body<List<TareaResponse>>()
    }

    suspend fun createTarea(request: CreateTareaRequest) {
        val req = client.post("$BASE_URL/tasks") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        if (req.status != HttpStatusCode.OK && req.status != HttpStatusCode.Created) {
            val errResponse = req.body<ServerErrorResponse>()
            error(errResponse.errorText ?: errResponse.error ?: "Error desconocido")
        }
    }

    suspend fun completarTarea(tid: Int) {
        val req = client.patch("$BASE_URL/tasks/$tid/complete") {
            contentType(ContentType.Application.Json)
        }
        if (req.status != HttpStatusCode.OK) {
            val errResponse = req.body<ServerErrorResponse>()
            error(errResponse.errorText ?: errResponse.error ?: "Error desconocido")
        }
    }

    suspend fun getMe(): UserMeResponse {
        val req = client.get("$BASE_URL/auth/me")
        if (req.status != HttpStatusCode.OK) {
            val errResponse = req.body<ServerErrorResponse>()
            error(errResponse.errorText ?: errResponse.error ?: "Error desconocido")
        }
        return req.body<UserMeResponse>()
    }

    suspend fun getStoreItems(): List<CosmeticResponse> {
        val req = client.get("$BASE_URL/store/items")
        if (req.status != HttpStatusCode.OK) error("Error al cargar la tienda")
        return req.body()
    }

    suspend fun buyCosmetic(id: Int) {
        val req = client.post("$BASE_URL/store/buy/$id")
        if (req.status != HttpStatusCode.OK) error("Fondos insuficientes")
    }

    suspend fun equipCosmetic(id: Int) {
        val req = client.post("$BASE_URL/store/equip/$id")
        if (req.status != HttpStatusCode.OK) error("Error al equipar")
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
package com.mm.astraisandroid.data.api

import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.contentType
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode

// 192.168.1.131
// 192.168.0.97
const val BASE_URL = "http://192.168.1.129:5684"

object BackendRepository {
    suspend fun performLogin(request: LoginRequest) : Result<LoginResponse> = runCatching {
        val req = client.post("$BASE_URL/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        if (req.status != HttpStatusCode.OK) {
            val errResponse = req.body<ServerErrorResponse>()
            val mensaje = errResponse.errorText ?: errResponse.error ?: "Error desconocido"
            error("Error: $mensaje")
        }

        // Devuelve el body
        req.body<LoginResponse>()
    }

    suspend fun performRegister(request: RegisterRequest) : Result<Unit> = runCatching {
        val req = client.post("$BASE_URL/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }

        if (req.status == HttpStatusCode.OK){
            return@runCatching
        } else {
            val errResponse = req.body<ServerErrorResponse>()
            val mensaje = errResponse.errorText ?: errResponse.error ?: "Error desconocido"
            error("Error: $mensaje")
    }
    }

    suspend fun getTareas(gid: Int): Result<List<TareaResponse>> = runCatching {
        val req = client.get("$BASE_URL/tasks/$gid") {
            contentType(ContentType.Application.Json)
        }
        if (req.status != HttpStatusCode.OK) {
            val errResponse = req.body<ServerErrorResponse>()
            val mensaje = errResponse.errorText ?: errResponse.error ?: "Error desconocido"
            error("Error: $mensaje")
        }
        req.body<List<TareaResponse>>()
    }

    suspend fun createTarea(request: CreateTareaRequest): Result<Unit> = runCatching {
        val req = client.post("$BASE_URL/tasks") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        if (req.status != HttpStatusCode.OK && req.status != HttpStatusCode.Created) {
            val errResponse = req.body<ServerErrorResponse>()
            val mensaje = errResponse.errorText ?: errResponse.error ?: "Error desconocido"
            error("Error: $mensaje")
        }
    }

    suspend fun completarTarea(tid: Int): Result<Unit> = runCatching {
        val req = client.patch("$BASE_URL/tasks/$tid/complete") {
            contentType(ContentType.Application.Json)
        }
        if (req.status != HttpStatusCode.OK) {
            val errResponse = req.body<ServerErrorResponse>()
            val mensaje = errResponse.errorText ?: errResponse.error ?: "Error desconocido"
            error("Error: $mensaje")
        }
    }

    suspend fun getMe(): Result<UserMeResponse> = runCatching {
        val req = client.get("$BASE_URL/auth/me")
        if (req.status != HttpStatusCode.OK) {
            val errResponse = req.body<ServerErrorResponse>()
            val mensaje = errResponse.errorText ?: errResponse.error ?: "Error desconocido"
            error("Error: $mensaje")
        }
        req.body<UserMeResponse>()
    }

    suspend fun getStoreItems(): Result<List<CosmeticResponse>> = runCatching {
        val req = client.get("$BASE_URL/store/items")
        if (req.status != HttpStatusCode.OK) error("Error al cargar la tienda")
        req.body()
    }

    suspend fun buyCosmetic(id: Int): Result<Unit> = runCatching {
        val req = client.post("$BASE_URL/store/buy/$id")
        if (req.status != HttpStatusCode.OK) error("Fondos insuficientes")
    }

    suspend fun equipCosmetic(id: Int): Result<Unit> = runCatching {
        val req = client.post("$BASE_URL/store/equip/$id")
        if (req.status != HttpStatusCode.OK) error("Error al equipar")
    }
}


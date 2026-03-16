package com.mm.astraisandroid.api

import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.contentType
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import javax.security.auth.login.LoginException
// 192.168.1.131
// 192.168.0.97
const val BASE_URL = "http://192.168.1.131:5684"

object BackendRepository {
    suspend fun performLogin(request: LoginRequest) : Result<LoginResponse> = runCatching {
        val req = client.post("$BASE_URL/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        if (req.status != HttpStatusCode.OK){
            error("Error del servidor! Error enviado: " + req.body<ServerErrorResponse>().error)
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
            error("Error del servidor! Envio "+ req.status.toString() + "! Mensaje: " + req.body<ServerErrorResponse>().error)
        }
    }
}


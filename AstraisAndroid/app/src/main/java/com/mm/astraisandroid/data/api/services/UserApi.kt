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

class UserApi @Inject constructor(private val client: HttpClient) {

    suspend fun getMe(): UserMeResponse {
        val req = client.get("$BASE_URL/auth/me")
        if (req.status != HttpStatusCode.OK) {
            val errResponse = req.body<ServerErrorResponse>()
            error(errResponse.errorText ?: errResponse.error ?: "Error desconocido")
        }
        return req.body<UserMeResponse>()
    }

    suspend fun updateUsername(uid: Int, newName: String) {
        val req = client.patch("$BASE_URL/auth/editUser") {
            setBody(EditUserRequest(uid = uid, nombreusu = newName, lang = null, utcOffset = null))
            header(HttpHeaders.ContentType, ContentType.Application.Json)
        }
        if (req.status != HttpStatusCode.OK) {
            error("Fallo al actualizar el nombre")
        }
    }

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
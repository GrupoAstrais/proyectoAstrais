package com.mm.astraisandroid.data.api.services

import com.mm.astraisandroid.data.api.*
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.serialization.json.Json
import javax.inject.Inject

class GroupApi @Inject constructor(private val client: HttpClient) {

    suspend fun getGroups(): AllGroupsResponse {
        val req = client.get("$BASE_URL/group/userGroups") {
            contentType(ContentType.Application.Json)
        }
        if (req.status != HttpStatusCode.OK) {
            val rawText = req.bodyAsText()
            val errMessage = try {
                val errResponse = Json { ignoreUnknownKeys = true }.decodeFromString<ServerErrorResponse>(rawText)
                errResponse.errorText ?: errResponse.error ?: "Error en servidor: $rawText"
            } catch (e: Exception) {
                "HTTP ${req.status.value}: $rawText"
            }
            error(errMessage)
        }
        return req.body<AllGroupsResponse>()
    }

    suspend fun createGroup(request: CreateGroupRequest): Int {
        val req = client.post("$BASE_URL/groups/createGroup") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        if (req.status != HttpStatusCode.OK && req.status != HttpStatusCode.Created) {
            val rawText = req.bodyAsText()
            val errMessage = try {
                val errResponse = Json { ignoreUnknownKeys = true }.decodeFromString<ServerErrorResponse>(rawText)
                errResponse.errorText ?: errResponse.error ?: "Error en servidor: $rawText"
            } catch (e: Exception) {
                "HTTP ${req.status.value}: $rawText"
            }
            error(errMessage)
        }
        return req.body<CreateGroupResponse>().groupId
    }

    suspend fun deleteGroup(request: DeleteGroupRequest) {
        val req = client.delete("$BASE_URL/groups/deleteGroup") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        if (req.status != HttpStatusCode.OK) {
            val rawText = req.bodyAsText()
            val errMessage = try {
                val errResponse = Json { ignoreUnknownKeys = true }.decodeFromString<ServerErrorResponse>(rawText)
                errResponse.errorText ?: errResponse.error ?: "Error en servidor: $rawText"
            } catch (e: Exception) {
                "HTTP ${req.status.value}: $rawText"
            }
            error(errMessage)
        }
    }

    suspend fun editGroup(request: EditGroupRequest) {
        val req = client.patch("$BASE_URL/groups/editGroup") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        if (req.status != HttpStatusCode.OK) {
            val rawText = req.bodyAsText()
            val errMessage = try {
                val errResponse = Json { ignoreUnknownKeys = true }.decodeFromString<ServerErrorResponse>(rawText)
                errResponse.errorText ?: errResponse.error ?: "Error en servidor: $rawText"
            } catch (e: Exception) {
                "HTTP ${req.status.value}: $rawText"
            }
            error(errMessage)
        }
    }
}

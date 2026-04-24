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
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject
@Serializable
private data class CreateTaskResponse(val id: Int)
class TaskApi @Inject constructor(private val client: HttpClient) {

    suspend fun getTareas(gid: Int): List<TaskResponse> {
        val req = client.post("$BASE_URL/tasks/$gid") {
            contentType(ContentType.Application.Json)
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
        return req.body<List<TaskResponse>>()
    }

    suspend fun createTarea(request: CreateTareaRequest): Int {
        val req = client.post("$BASE_URL/tasks") {
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
        return req.body<CreateTaskResponse>().id
    }

    suspend fun completarTarea(tid: Int) {
        val req = client.patch("$BASE_URL/tasks/$tid/complete") {
            contentType(ContentType.Application.Json)
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
    }

    suspend fun deleteTarea(tid: Int) {
        val req = client.delete("$BASE_URL/tasks/$tid/delete")
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
    }

    suspend fun editarTarea(tid: Int, request: EditTareaRequest) {
        val req = client.patch("$BASE_URL/tasks/$tid/edit") {
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
    }

    suspend fun uncompleteTarea(tid: Int) {
        val req = client.patch("$BASE_URL/tasks/$tid/uncomplete") {
            contentType(ContentType.Application.Json)
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
    }
}
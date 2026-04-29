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

/**
 * Respuesta interna del servidor al crear una nueva tarea.
 * @property id Identificador asignado por el servidor a la tarea recién creada.
 */
@Serializable
private data class CreateTaskResponse(val id: Int)

/**
 * Respuesta interna del servidor al listar las tareas de un grupo.
 * @property taskList Lista de [TaskResponse] que contiene todas las tareas del grupo.
 */
@Serializable
private data class GetTasksResponse(val taskList: List<TaskResponse>)

/**
 * Cliente HTTP para la API de tareas de Astrais.
 *
 * Encapsula todas las llamadas de red relacionadas con tareas: listado, creación,
 * edición, eliminación, completado y descomplete. Cada método lanza una excepción
 * con el mensaje de error del servidor si la respuesta HTTP no es exitosa,
 * permitiendo que las capas superiores (repositorio, ViewModel) la capturen.
 *
 * El cliente Ktor inyectado (`client`) ya incluye el JWT de acceso en las cabeceras
 * de autorización gracias a la configuración del módulo de Hilt.
 *
 * @property client Cliente HTTP de Ktor configurado con autenticación JWT.
 */
class TaskApi @Inject constructor(private val client: HttpClient) {

    /**
     * Recupera la lista de tareas de un grupo desde el servidor.
     * Llama a `POST /tasks/{gid}`.
     *
     * @param gid Identificador del grupo cuyas tareas se quieren obtener.
     * @return Lista de [TaskResponse] con las tareas del grupo.
     * @throws IllegalStateException Si el servidor devuelve un código de error HTTP.
     */
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
        return req.body<GetTasksResponse>().taskList
    }

    /**
     * Crea una nueva tarea en el grupo indicado.
     * Llama a `POST /tasks` con el cuerpo [CreateTareaRequest].
     * El solicitante debe ser Owner o Moderador del grupo.
     *
     * @param request Datos de la tarea a crear, incluyendo tipo y datos extra según corresponda.
     * @return Identificador de la tarea recién creada asignado por el servidor.
     * @throws IllegalStateException Si el servidor devuelve un código de error HTTP.
     */
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

    /**
     * Marca una tarea como completada en el servidor.
     * Llama a `PATCH /tasks/{tid}/complete`.
     * El servidor otorga las recompensas de XP y Ludiones al usuario autenticado.
     *
     * @param tid Identificador de la tarea a completar.
     * @throws IllegalStateException Si el servidor devuelve un código de error HTTP.
     */
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

    /**
     * Elimina permanentemente una tarea del servidor.
     * Llama a `DELETE /tasks/{tid}/delete`.
     * Solo el Owner y los Moderadores del grupo pueden eliminar tareas.
     *
     * @param tid Identificador de la tarea a eliminar.
     * @throws IllegalStateException Si el servidor devuelve un código de error HTTP.
     */
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

    /**
     * Edita los campos de una tarea existente de forma parcial.
     * Llama a `PATCH /tasks/{tid}/edit` con el cuerpo [EditTareaRequest].
     * Los campos `null` en el cuerpo no son modificados por el servidor.
     *
     * @param tid Identificador de la tarea a editar.
     * @param request Datos de edición; los campos `null` no se modifican.
     * @throws IllegalStateException Si el servidor devuelve un código de error HTTP.
     */
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

    /**
     * Revierte el estado de completado de una tarea a activa.
     * Llama a `PATCH /tasks/{tid}/uncomplete`.
     * El servidor descuenta las recompensas que habían sido otorgadas y retrocede
     * la racha si se trata de un hábito completado hoy.
     *
     * @param tid Identificador de la tarea a revertir.
     * @throws IllegalStateException Si el servidor devuelve un código de error HTTP.
     */
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
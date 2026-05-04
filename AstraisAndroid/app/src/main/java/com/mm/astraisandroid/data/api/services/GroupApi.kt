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

/**
 * Cliente HTTP para la API de grupos.
 *
 * Encapsula todas las llamadas de red relacionadas con grupos: listado, creación,
 * edición, eliminación, gestión de miembros, sistema de invitaciones y auditoría.
 * Cada método lanza una excepción con el mensaje de error del servidor si la respuesta
 * HTTP no es exitosa, permitiendo que las capas superiores (repositorio, ViewModel)
 * la capturen con un bloque `try/catch`.
 *
 *
 * @property client Cliente HTTP de Ktor configurado con autenticación JWT.
 */
class GroupApi @Inject constructor(private val client: HttpClient) {

    /**
     * Recupera la lista de grupos del usuario autenticado.
     * Llama a `GET /group/userGroups`. Filtra los grupos personales en el servidor.
     *
     * @return [AllGroupsResponse] con la lista de grupos y el rol del usuario en cada uno.
     * @throws IllegalStateException Si el servidor devuelve un código de error HTTP.
     */
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

    /**
     * Crea un nuevo grupo en el servidor con el usuario autenticado como Owner.
     * Llama a `POST /groups/createGroup`.
     *
     * @param request Datos del nuevo grupo (nombre y descripción).
     * @return El GID del grupo recén creado.
     * @throws IllegalStateException Si el servidor devuelve un código de error HTTP.
     */
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

    /**
     * Elimina permanentemente un grupo del servidor.
     * Llama a `DELETE /groups/deleteGroup`. Solo el Owner puede realizar esta operación.
     *
     * @param request Identificador del grupo a eliminar.
     * @throws IllegalStateException Si el servidor devuelve un código de error HTTP
     *   (ej. 403 si el usuario no es el Owner).
     */
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

    /**
     * Edita los metadatos (nombre y/o descripción) de un grupo.
     * Llama a `PATCH /groups/editGroup`. Solo Owners y Moderadores pueden editar grupos.
     *
     * @param request Datos de edición; los campos nulos no se modifican en el servidor.
     * @throws IllegalStateException Si el servidor devuelve un código de error HTTP.
     */
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

    /**
     * Agrega un usuario a un grupo de forma directa.
     * Llama a `POST /groups/addUser`. Solo Owners y Moderadores pueden añadir usuarios.
     *
     * @param request GID del grupo e identificador del usuario a añadir.
     * @throws IllegalStateException Si el servidor devuelve un código de error HTTP.
     */
    suspend fun addUser(request: AddUserRequest) {
        val req = client.post("$BASE_URL/groups/addUser") {
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

    /**
     * Expulsa a un miembro de un grupo.
     * Llama a `POST /groups/removeUser`. El Owner no puede ser expulsado.
     *
     * @param request GID del grupo e identificador del usuario a expulsar.
     * @throws IllegalStateException Si el servidor devuelve un código de error HTTP.
     */
    suspend fun removeUser(request: RemoveUserRequest) {
        val req = client.post("$BASE_URL/groups/removeUser") {
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

    /**
     * Transfiere la propiedad de un grupo del Owner actual a otro miembro.
     * Llama a `PATCH /groups/passOwnership`. Solo el Owner puede transferir la propiedad.
     *
     * @param request GID del grupo e identificador del nuevo Owner.
     * @throws IllegalStateException Si el servidor devuelve un código de error HTTP.
     */
    suspend fun passOwnership(request: PassOwnershipRequest) {
        val req = client.patch("$BASE_URL/groups/passOwnership") {
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

    /**
     * Genera una URL de invitación para el grupo indicado.
     * Llama a `POST /groups/inviteUrl`. Internamente el servidor crea un token seguro.
     *
     * @param request GID del grupo para el que se genera la invitación.
     * @return URL completa lista para compartir.
     * @throws IllegalStateException Si el servidor devuelve un código de error HTTP.
     */
    suspend fun createInviteUrl(request: InviteUrlRequest): String {
        val req = client.post("$BASE_URL/groups/inviteUrl") {
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
        return req.body<InviteUrlResponse>().inviteUrl
    }

    /**
     * Une al usuario autenticado a un grupo a través de una URL de invitación.
     * Llama a `POST /groups/joinByUrl`. Soporta URLs con `?code=` y con `?gid=` (legado).
     *
     * @param request URL de invitación completa.
     * @throws IllegalStateException Si el servidor devuelve un código de error HTTP
     *   (p. ej. URL inválida, código expirado o usuario ya miembro).
     */
    suspend fun joinByUrl(request: JoinByUrlRequest) {
        val req = client.post("$BASE_URL/groups/joinByUrl") {
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

    /**
     * Une al usuario autenticado a un grupo mediante un código de invitación.
     * Llama a `POST /groups/joinByCode`. Valida revocación, expiración y límite de usos.
     *
     * @param request Código de invitación.
     * @throws IllegalStateException Si el servidor devuelve un código de error HTTP.
     */
    suspend fun joinByCode(request: JoinByCodeRequest) {
        val req = client.post("$BASE_URL/groups/joinByCode") {
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

    /**
     * Crea una invitación segura basada en token para el grupo indicado.
     * Llama a `POST /groups/invites`. Permite especificar expiración y límite de usos.
     * Solo Owners y Moderadores pueden crear invitaciones.
     *
     * @param request Datos de la invitación (GID, expiración opcional y máximo de usos).
     * @return [InviteOut] con el código y la URL generados.
     * @throws IllegalStateException Si el servidor devuelve un código de error HTTP.
     */
    suspend fun createInvite(request: CreateInviteRequest): InviteOut {
        val req = client.post("$BASE_URL/groups/invites") {
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
        return req.body()
    }

    /**
     * Lista todas las invitaciones de un grupo (activas, revocadas y expiradas).
     * Llama a `GET /groups/{gid}/invites`. Solo Owners y Moderadores pueden consultar.
     *
     * @param gid Identificador del grupo cuyas invitaciones se quieren listar.
     * @return [ListInvitesResponse] con la lista de invitaciones.
     * @throws IllegalStateException Si el servidor devuelve un código de error HTTP.
     */
    suspend fun listInvites(gid: Int): ListInvitesResponse {
        val req = client.get("$BASE_URL/groups/$gid/invites") {
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
        return req.body()
    }

    /**
     * Revoca una invitación activa de un grupo.
     * Llama a `POST /groups/invites/revoke`. Solo Owners y Moderadores pueden revocar.
     *
     * @param request GID del grupo y código de invitación a revocar.
     * @throws IllegalStateException Si el servidor devuelve un código de error HTTP.
     */
    suspend fun revokeInvite(request: RevokeInviteRequest) {
        val req = client.post("$BASE_URL/groups/invites/revoke") {
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

    /**
     * Recupera la lista de miembros de un grupo.
     * Llama a `GET /groups/{gid}/members`. Cualquier miembro del grupo puede consultar.
     *
     * @param gid Identificador del grupo.
     * @return [GroupMembersResponse] con la lista de miembros, sus roles y fechas de unión.
     * @throws IllegalStateException Si el servidor devuelve un código de error HTTP.
     */
    suspend fun getMembers(gid: Int): GroupMembersResponse {
        val req = client.get("$BASE_URL/groups/$gid/members") {
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
        return req.body()
    }

    /**
     * Permite al usuario autenticado abandonar voluntariamente un grupo.
     * Llama a `POST /groups/leave`. El Owner no puede abandonar sin transferir la propiedad.
     *
     * @param request GID del grupo que el usuario desea abandonar.
     * @throws IllegalStateException Si el servidor devuelve un código de error HTTP
     *   (p. ej. 400 si el usuario es el Owner).
     */
    suspend fun leaveGroup(request: LeaveGroupRequest) {
        val req = client.post("$BASE_URL/groups/leave") {
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

    /**
     * Cambia el rol de un miembro del grupo.
     * Llama a `PATCH /groups/setMemberRole`. Solo el Owner puede invocar esta operación.
     *
     * @param request GID del grupo, identificador del miembro y nuevo rol.
     * @throws IllegalStateException Si el servidor devuelve un código de error HTTP.
     */
    suspend fun setMemberRole(request: SetMemberRoleRequest) {
        val req = client.patch("$BASE_URL/groups/setMemberRole") {
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

    /**
     * Recupera el historial de eventos de auditoría de un grupo con paginación.
     * Llama a `GET /groups/{gid}/audit`. Cualquier miembro del grupo puede consultar.
     *
     * @param gid Identificador del grupo.
     * @param limit Número máximo de eventos a devolver (por defecto 50, máximo 200).
     * @param offset Desplazamiento para paginación (por defecto 0).
     * @return [AuditEventsResponse] con la lista paginada de eventos.
     * @throws IllegalStateException Si el servidor devuelve un código de error HTTP.
     */
    suspend fun getAudit(gid: Int, limit: Int = 50, offset: Long = 0L): AuditEventsResponse {
        val req = client.get("$BASE_URL/groups/$gid/audit?limit=$limit&offset=$offset") {
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
        return req.body()
    }
}

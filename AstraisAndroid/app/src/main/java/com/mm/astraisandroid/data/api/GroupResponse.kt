package com.mm.astraisandroid.data.api

import kotlinx.serialization.Serializable

/**
 * DTO de salida que representa un grupo al que pertenece el usuario.
 * Recibido dentro de [AllGroupsResponse] desde el endpoint `/group/userGroups`.
 *
 * @property id Identificador único del grupo en el servidor.
 * @property name Nombre del grupo.
 * @property description Descripción del grupo.
 * @property role Rol del usuario autenticado en este grupo: `0` = Miembro, `1` = Moderador,
 *   `2` = Owner.
 */
@Serializable
data class SingleGroupOut(
    val id: Int,
    val name: String,
    val description: String,
    val role: Int
)

/**
 * Respuesta del endpoint `GET /group/userGroups`.
 * Contiene la lista completa de grupos a los que pertenece el usuario autenticado.
 *
 * @property groupList Lista de grupos con el rol del usuario en cada uno.
 */
@Serializable
data class AllGroupsResponse(
    val groupList: List<SingleGroupOut>
)

/**
 * Petición para crear un grupo nuevo.
 * Enviado al endpoint `POST /groups/createGroup`.
 *
 * @property name Nombre del grupo. Debe tener al menos 3 caracteres.
 * @property desc Descripción opcional del grupo.
 */
@Serializable
data class CreateGroupRequest(
    val name: String,
    val desc: String
)

/**
 * Petición para agregar un usuario a un grupo de forma directa.
 * Enviado al endpoint `POST /groups/addUser`.
 * Solo Owners y Moderadores pueden invocar este endpoint con éxito.
 *
 * @property gid Identificador del grupo destino.
 * @property userId Identificador del usuario que se desea añadir.
 */
@Serializable
data class AddUserRequest(
    val gid: Int,
    val userId: Int
)

/**
 * Petición para expulsar a un miembro de un grupo.
 * Enviado al endpoint `POST /groups/removeUser`.
 * El Owner no puede ser expulsado mediante esta operación.
 *
 * @property gid Identificador del grupo.
 * @property userId Identificador del usuario a expulsar.
 */
@Serializable
data class RemoveUserRequest(
    val gid: Int,
    val userId: Int
)

/**
 * Petición para transferir la propiedad de un grupo.
 * Enviado al endpoint `PATCH /groups/passOwnership`.
 * Solo el Owner actual puede realizar esta operación.
 *
 * @property gid Identificador del grupo cuya propiedad se transfiere.
 * @property newOwnerUserId Identificador del miembro que pasará a ser el nuevo Owner.
 */
@Serializable
data class PassOwnershipRequest(
    val gid: Int,
    val newOwnerUserId: Int
)

/**
 * Petición para generar una URL de invitación (endpoint legado).
 * Enviado al endpoint `POST /groups/inviteUrl`.
 *
 * @property gid Identificador del grupo para el que se genera la invitación.
 */
@Serializable
data class InviteUrlRequest(
    val gid: Int
)

/**
 * Respuesta del endpoint `POST /groups/inviteUrl`.
 *
 * @property inviteUrl URL completa lista para compartir con el usuario invitado.
 */
@Serializable
data class InviteUrlResponse(
    val inviteUrl: String
)

/**
 * Petición para unirse a un grupo mediante una URL de invitación.
 * Enviado al endpoint `POST /groups/joinByUrl`.
 * Soporta tanto URLs con `?code=` (token seguro) como `?gid=` (flujo legado).
 *
 * @property inviteUrl La URL completa de invitación recibida por el usuario.
 */
@Serializable
data class JoinByUrlRequest(
    val inviteUrl: String
)

/**
 * Petición para eliminar permanentemente un grupo.
 * Enviado al endpoint `DELETE /groups/deleteGroup`.
 * Solo el Owner puede realizar esta operación.
 *
 * @property gid Identificador del grupo a eliminar.
 */
@Serializable
data class DeleteGroupRequest(
    val gid: Int
)

/**
 * Petición para editar los metadatos de un grupo.
 * Enviado al endpoint `PATCH /groups/editGroup`.
 * Los campos nulos no se modifican en el servidor.
 *
 * @property gid Identificador del grupo a editar.
 * @property name Nuevo nombre del grupo, o `null` para no cambiarlo.
 * @property desc Nueva descripción del grupo, o `null` para no cambiarla.
 */
@Serializable
data class EditGroupRequest(
    val gid: Int,
    val name: String? = null,
    val desc: String? = null
)

/**
 * Respuesta del endpoint `POST /groups/createGroup`.
 *
 * @property groupId Identificador del grupo recén creado en el servidor.
 */
@Serializable
data class CreateGroupResponse(
    val groupId: Int
)

/**
 * Petición para crear una invitación segura con token.
 * Enviado al endpoint `POST /groups/invites`.
 *
 * @property gid Identificador del grupo para el que se crea la invitación.
 * @property expiresInSeconds Segundos de validez desde la creación, o `null` para sin
 *   expiración.
 * @property maxUses Límite máximo de usos, o `null` para usos ilimitados.
 */
@Serializable
data class CreateInviteRequest(
    val gid: Int,
    val expiresInSeconds: Long? = null,
    val maxUses: Int? = null,
)

/**
 * DTO de salida que representa una invitación de grupo recibida del servidor.
 * Devuelto por los endpoints de creación (`POST /groups/invites`) y de listado
 * (`GET /groups/{gid}/invites`).
 *
 * @property code Código de invitación. Solo está disponible en la
 *   respuesta de creación; en el listado este campo puede estar vacío.
 * @property inviteUrl URL completa lista para compartir con el usuario invitado.
 * @property expiresAt Fecha y hora de expiración como cadena ISO-8601 en UTC, o `null`
 *   si no expira.
 * @property maxUses Número máximo de usos permitidos, o `null` si es ilimitado.
 * @property usesCount Número de veces que la invitación ha sido consumida.
 * @property revokedAt Fecha y hora de revocación como cadena ISO-8601 en UTC, o `null`
 *   si no ha sido revocada.
 */
@Serializable
data class InviteOut(
    val code: String,
    val inviteUrl: String,
    val expiresAt: String? = null,
    val maxUses: Int? = null,
    val usesCount: Int = 0,
    val revokedAt: String? = null,
)

/**
 * Respuesta del endpoint `GET /groups/{gid}/invites`.
 *
 * @property invites Lista de todas las invitaciones del grupo, incluyendo las revocadas
 *   y expiradas.
 */
@Serializable
data class ListInvitesResponse(
    val invites: List<InviteOut>
)

/**
 * Petición para revocar una invitación activa.
 * Enviado al endpoint `POST /groups/invites/revoke`.
 *
 * @property gid Identificador del grupo al que pertenece la invitación.
 * @property code Código de invitación en texto claro que se desea revocar.
 */
@Serializable
data class RevokeInviteRequest(
    val gid: Int,
    val code: String
)

/**
 * Petición para unirse a un grupo mediante un código de invitación directo.
 * Enviado al endpoint `POST /groups/joinByCode`.
 *
 * @property code Código de invitación en texto claro recibido por el usuario.
 */
@Serializable
data class JoinByCodeRequest(
    val code: String
)

/**
 * DTO de salida que representa a un miembro de un grupo.
 * Recibido dentro de [GroupMembersResponse] desde el endpoint
 * `GET /groups/{gid}/members`.
 *
 * @property uid Identificador único del usuario miembro.
 * @property name Nombre de usuario visible en la interfaz.
 * @property role Rol del miembro: `0` = Miembro normal, `1` = Moderador, `2` = Owner.
 * @property joinedAt Fecha de incorporación como cadena ISO-8601, o `null` si no está
 *   disponible.
 */
@Serializable
data class GroupMemberOut(
    val uid: Int,
    val name: String,
    val role: Int,
    val joinedAt: String? = null,
)

/**
 * Respuesta del endpoint `GET /groups/{gid}/members`.
 *
 * @property members Lista completa de miembros del grupo, incluyendo al Owner.
 */
@Serializable
data class GroupMembersResponse(
    val members: List<GroupMemberOut>
)

/**
 * Petición para que el usuario autenticado abandone un grupo.
 * Enviado al endpoint `POST /groups/leave`.
 * El Owner no puede usar este endpoint sin transferir primero la propiedad.
 *
 * @property gid Identificador del grupo que el usuario desea abandonar.
 */
@Serializable
data class LeaveGroupRequest(
    val gid: Int
)

/**
 * Petición para cambiar el rol de un miembro del grupo.
 * Enviado al endpoint `PATCH /groups/setMemberRole`.
 * Solo el Owner puede invocar este endpoint.
 *
 * @property gid Identificador del grupo.
 * @property userId Identificador del miembro cuyo rol se va a modificar.
 * @property role Nuevo rol: `0` = Miembro normal, `1` = Moderador.
 */
@Serializable
data class SetMemberRoleRequest(
    val gid: Int,
    val userId: Int,
    val role: Int
)

/**
 * DTO de salida que representa un evento del log de auditoría de un grupo.
 * Recibido dentro de [AuditEventsResponse] desde el endpoint
 * `GET /groups/{gid}/audit`.
 *
 * @property id Identificador único del evento.
 * @property actorUid Identificador del usuario que provocó el evento, o `null` si fue
 *   el sistema.
 * @property eventType Tipo de evento. Valores conocidos: `invite_created`,
 *   `invite_revoked`, `member_joined_by_invite`, `member_left`, `member_role_changed`.
 * @property payloadJson Datos contextuales del evento en formato JSON, o `null` si no
 *   aplica.
 * @property createdAt Marca de tiempo del evento como cadena ISO-8601 en UTC.
 */
@Serializable
data class AuditEventOut(
    val id: Int,
    val actorUid: Int? = null,
    val eventType: String,
    val payloadJson: String? = null,
    val createdAt: String
)

/**
 * Respuesta del endpoint `GET /groups/{gid}/audit`.
 *
 * @property events Lista paginada de eventos de auditoría ordenada cronológicamente.
 */
@Serializable
data class AuditEventsResponse(
    val events: List<AuditEventOut>
)

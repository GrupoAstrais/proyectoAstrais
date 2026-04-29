package groups.types

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

/**
 * DTO que encapsula la salida de un solo grupo.
 * Retornado habitualmente desde el endpoint /group/userGroups.
 */
@Serializable
data class SingleGroupOut(
    /** @property id El identificador único del grupo. */
    val id : Int,
    /** @property name El nombre formal del grupo. */
    val name : String,
    /** @property description Una descripción sobre el propósito del grupo. */
    val description : String,
    /** * @property role El rol del usuario sobre este grupo.
     * @see ROLE_USERNORMAL
     * @see ROLE_USERMOD
     * @see ROLE_USEROWNER
     */
    val role: Int
)

/**
 * Salida de la ruta /group/userGroups
 */
@Serializable
data class AllGroupsResponse(
    val groupList : List<SingleGroupOut>
)

/**
 * Petición para crear un nuevo grupo.
 *
 * @property name Nombre del grupo. Debe tener al menos 3 caracteres.
 * @property desc Descripción opcional del grupo.
 */
@Serializable
data class CreateGroupRequest(
    val name : String,
    val desc : String
)

/**
 * Petición para agregar un usuario a un grupo de forma directa.
 * Solo los Owners y Moderadores pueden usar este endpoint.
 *
 * @property gid Identificador del grupo al que se quiere agregar al usuario.
 * @property userId Identificador del usuario que se desea añadir.
 */
@Serializable
data class AddUserRequest(
    val gid : Int,
    val userId : Int
)

/**
 * Petición para eliminar un grupo permanentemente.
 * Solo el Owner del grupo puede realizar esta operación.
 *
 * @property gid Identificador del grupo a eliminar.
 */
@Serializable
data class DeleteGroupRequest (
    val gid : Int
)

/**
 * Petición para editar los metadatos de un grupo.
 * Los campos nulos no se modifican.
 * Solo Owners y Moderadores pueden editar el grupo.
 *
 * @property gid Identificador del grupo a editar.
 * @property name Nuevo nombre del grupo.
 * @property desc Nueva descripción del grupo.
 */
@Serializable
data class EditGroupRequest (
    val gid : Int,
    val name: String? = null,
    val desc: String? = null
)

/**
 * Petición para expulsar a un usuario de un grupo.
 * El Owner no puede ser expulsado mediante esta operación.
 *
 * @property gid Identificador del grupo del que se expulsa al usuario.
 * @property userId Identificador del usuario a expulsar.
 */
@Serializable
data class RemoveUserRequest(
    val gid: Int,
    val userId: Int
)

/**
 * Petición para transferir la propiedad de un grupo a otro miembro.
 * Solo el Owner actual puede realizar esta operación.
 * El nuevo Owner debe ser ya miembro del grupo.
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
 * Petición para generar una URL de invitación básica.
 * En producción el endpoint `/groups/inviteUrl` delega internamente en [createInvite].
 *
 * @property gid Identificador del grupo para el que se genera la invitación.
 */
@Serializable
data class InviteUrlRequest(
    val gid: Int
)

/**
 * Respuesta de los endpoints de generación de URL de invitación básica.
 *
 * @property inviteUrl URL completa lista para compartir. Contiene el código como parámetro
 *   de consulta y apunta al endpoint de redirección configurado en `INVITE_BASE_URL`.
 */
@Serializable
data class InviteUrlResponse(
    val inviteUrl: String
)

/**
 * Petición para unirse a un grupo a través de una URL de invitación.
 * La URL puede contener un parámetro `code` o un parámetro `gid` (hay que cambiar esto).
 *
 * @property inviteUrl La URL completa de invitación recibida por el usuario.
 */
@Serializable
data class JoinByUrlRequest(
    val inviteUrl: String
)

/**
 * Petición para crear una invitación segura basada en token (SHA-256).
 * Permite configurar una expiración temporal y un límite de usos máximos.
 *
 * @property gid Identificador del grupo para el que se crea la invitación.
 * @property expiresInSeconds Tiempo de validez en segundos desde la creación, o `null` para
 *   invitación sin expiración.
 * @property maxUses Número máximo de veces que se puede usar la invitación, o `null` para
 *   usos ilimitados.
 */
@Serializable
data class CreateInviteRequest(
    val gid: Int,
    val expiresInSeconds: Long? = null,
    val maxUses: Int? = null,
)

/**
 * DTO de salida que representa una invitación de grupo creada.
 * Devuelto por los endpoints de creación y listado de invitaciones.
 *
 * @property code Código de invitación.
 *   El backend almacena únicamente su hash SHA-256; este valor solo se devuelve en la
 *   respuesta de creación.
 * @property inviteUrl URL completa lista para compartir con el usuario invitado.
 * @property expiresAt Fecha y hora de expiración en UTC, o `null` si no expira.
 * @property maxUses Número máximo de usos permitidos, o `null` si es ilimitado.
 * @property usesCount Número de veces que la invitación ha sido consumida exitosamente.
 * @property revokedAt Fecha y hora de revocación en UTC, o `null` si no ha sido revocada.
 */
@Serializable
data class InviteOut(
    val code: String,
    val inviteUrl: String,
    val expiresAt: kotlinx.datetime.LocalDateTime? = null,
    val maxUses: Int? = null,
    val usesCount: Int = 0,
    val revokedAt: kotlinx.datetime.LocalDateTime? = null,
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
 * Solo Owners y Moderadores pueden revocar invitaciones.
 *
 * @property gid Identificador del grupo al que pertenece la invitación.
 * @property code Código de invitación que se desea revocar.
 */
@Serializable
data class RevokeInviteRequest(
    val gid: Int,
    val code: String,
)

/**
 * Petición para unirse a un grupo mediante un código de invitación directo.
 *
 * @property code Código de invitación recibido por el usuario.
 */
@Serializable
data class JoinByCodeRequest(
    val code: String
)

/**
 * DTO de salida que representa a un miembro de un grupo.
 * Devuelto por el endpoint `GET /groups/{gid}/members`.
 *
 * @property uid Identificador único del usuario miembro.
 * @property name Nombre de usuario visible en la interfaz.
 * @property role Rol del miembro en el grupo expresado como entero:
 *   `0` = Miembro normal, `1` = Moderador, `2` = Owner.
 * @property joinedAt Fecha y hora en que el usuario se unió al grupo, o `null` si no
 *   está disponible.
 */
@Serializable
data class GroupMemberOut(
    val uid: Int,
    val name: String,
    val role: Int,
    val joinedAt: LocalDateTime? = null,
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
 * Cuerpo de la petición para que el usuario autenticado abandone un grupo.
 * El Owner no puede usar este endpoint; debe transferir la propiedad antes.
 *
 * @property gid Identificador del grupo que el usuario desea abandonar.
 */
@Serializable
data class LeaveGroupRequest(
    val gid: Int
)

/**
 * Petición para cambiar el rol de un miembro en el grupo.
 * Solo el Owner puede invocar este endpoint.
 *
 * @property gid Identificador del grupo.
 * @property userId Identificador del miembro cuyo rol se va a modificar.
 * @property role Nuevo rol expresado como entero: `0` = Miembro normal, `1` = Moderador.
 *   No se puede asignar el rol de Owner (2) mediante este endpoint.
 */
@Serializable
data class SetMemberRoleRequest(
    val gid: Int,
    val userId: Int,
    val role: Int,
)

/**
 * DTO de salida que representa un evento del log de auditoría de un grupo.
 * Devuelto por el endpoint `GET /groups/{gid}/audit`.
 *
 * @property id Identificador único del evento de auditoría.
 * @property actorUid Identificador del usuario que provocó el evento, o `null` si la
 *   acción fue del sistema.
 * @property eventType Tipo de evento como cadena de texto. Valores conocidos:
 *   `invite_created`, `invite_revoked`, `member_joined_by_invite`, `member_left`,
 *   `member_role_changed`.
 * @property payloadJson Carga útil del evento en formato JSON, o `null` si no aplica.
 *   Contiene datos contextuales del evento (ej. el ID del invite o el nuevo rol).
 * @property createdAt Marca de tiempo UTC en la que ocurrió el evento.
 */
@Serializable
data class AuditEventOut(
    val id: Int,
    val actorUid: Int? = null,
    val eventType: String,
    val payloadJson: String? = null,
    val createdAt: LocalDateTime,
)

/**
 * Respuesta del endpoint `GET /groups/{gid}/audit`.
 *
 * @property events Lista paginada de eventos de auditoría del grupo, ordenada
 *   cronológicamente.
 */
@Serializable
data class AuditEventsResponse(
    val events: List<AuditEventOut>
)

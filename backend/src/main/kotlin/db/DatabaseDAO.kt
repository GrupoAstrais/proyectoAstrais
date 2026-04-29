package com.astrais.db

import AvatarLayer
import CosmeticResponseDTO
import LANG_CODE_ENGLISH
import admin.RarityType
import avatar.AvatarLayerDTO
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.v1.core.dao.id.EntityID

enum class BuyCosmeticResponse{
    OKAY,
    USER_NOT_FOUND,
    COSMETIC_NOT_FOUND,
    INSUFICIENT_CURRENCY,
    ALREADY_HAS_OBJECT,
    NO_METHOD_REMAIN
}

data class TareaUniqueData(
    val fechaLimite : LocalDateTime
)

data class TareaHabitData(
    val numeroFrecuencia : Int,
    val frequency : String
)

@Serializable
data class DatosSimpleUsuarios(
    val id: Int,
    val nombre: String,
    val rol: String,
    val nivel: Int
)

@Serializable
data class DatosSimpleGrupo(
    val id: Int,
    val nombre: String,
    val descripcion: String,
    val ownerNombre: String,
)

data class GroupInviteDb(
    val id: Int,
    val gid: Int,
    val code: String,
    val codeHash: String,
    val createdByUid: Int,
    val createdAt: LocalDateTime,
    val expiresAt: LocalDateTime?,
    val revokedAt: LocalDateTime?,
    val maxUses: Int?,
    val usesCount: Int,
)

data class GroupAuditEventDb(
    val id: Int,
    val gid: Int,
    val actorUid: Int?,
    val eventType: String,
    val payloadJson: String?,
    val createdAt: LocalDateTime,
)

data class GroupMemberDb(
    val uid: Int,
    val name: String,
    val role: Int,
    val joinedAt: LocalDateTime,
)

interface DatabaseDAO {
    /**
     * Crea un usuario en la base de datos
     * @return Devuelve el ID de usuario
     */
    suspend fun createUser(
        nombreusu: String,
        emailusu: String,
        passwordusu: String,
        lang: String = LANG_CODE_ENGLISH,
        utcOffset: Float = 0f,
        role: UserRoles = UserRoles.NORMAL_USER
    ): Int

    suspend fun editUser(
        uid : Int,
        nombreusu: String?,
        lang: String?,
        utcOffset: Float?,
    ) : Boolean

    suspend fun setupUserEmail(
        uid: Int,
        newEmail : String?,
        newPassword : String?
    ) : Boolean

    /**
     * Buscamos un usuario por su email.
     * @param emailusu El correo del usuario
     * @return Los datos del usuario, NULL si no se encontro
     */
    suspend fun getUsuario(emailusu: String): EntidadUsuario?

    /**
     * Buscamos un usuario por su ID.
     * @param id El ID del usuario
     * @return Los datos del usuario, NULL si no se encontro
     */
    suspend fun getUsuarioByID(id: Int): EntidadUsuario?

    /**
     * Borrado del usuario identificado por ID
     * @param id El ID del usuario
     * @return Si borro el usuario o no
     */
    suspend fun deleteUsuario(id: Int): Boolean

    /**
     * Se cambia la fecha del ultimo login al actual
     * @param ent El usuario concreto
     */
    suspend fun setUserLastLogin(ent: EntidadUsuario)

    /**
     * Comprueba si el usuario es admin del servidor entero
     */
    suspend fun checkIfUserIsServerAdmin(uid: Int) : Boolean

    /**
     * Mira si le UID del provider ya esta asignado a una cuenta
     */
    suspend fun checkForOauth(provider_uid : String, auth : AuthProvider) : Boolean

    /**
     * Intenta loguear o registrar un nuevo usuario con las credenciales de OAuth indicadas
     * @param provider_uid El UID que el proveedor OAuth nos otorga
     * @param auth El tipo de provedor oauth que hace la operacion
     * @return Un pair, El primer elemento es el ID, el otro dice si se tuvo que registrar un usuario
     */
    suspend fun logOrCreateOauthUser(
        provider_uid : String,
        auth : AuthProvider
        ) : Pair<Int, Boolean>

    /**
     * Añade oauth a una cuenta ya existente
     * @return OKAY si esta bien, USER_NOT_FOUND si el uid es incorrecto, ALREADY_HAS_OBJECT si ya hay vinculado un metodo oauth del mismo tipo
     */
    suspend fun addOauthToAccount(
        uid : Int,
        provider_uid: String,
        auth: AuthProvider
    ) : BuyCosmeticResponse

    /**
     * Borra un metodo oauth de una cuenta. Si la cuenta tuviera la posibilidad de quedarse sin forma de acceder (sin oauth ni correo) no lo permite.
     * @return OKAY si bien, USER_NOT_FOUND si el uid es incorrecto, NO_METHOD_REMAIN si al borrar la cuenta se fuera a quedar sin metodo de logueo
     */
    suspend fun deleteOauthFromAccount(
        uid : Int,
        auth: AuthProvider
    ) : BuyCosmeticResponse

    /**
     * Se crea un grupo para el usuario indicado
     * @param grpownerId El ID del usuario que crea el grupo
     * @param grpname El nombre del grupo
     * @param grpdescription La descripcion del grupo, opcional.
     * @param personal Indicador si el grupo se considera personal o no
     * @return El ID del grupo
     */
    suspend fun createGroup(
        grpownerId: Int,
        grpname: String,
        grpdescription: String = "",
        personal: Boolean = false
    ): Int

    /**
     * Se consigue la informacion del grupo por un ID
     * @param id El ID del grupo
     * @return Los datos del grupo, NULL si no se encontro
     */
    suspend fun getGroupById(id: Int): EntidadGrupo?

    /**
     * Devuelve una lista de grupos que el usuario tiene
     * @param idusuario El ID del usuario del que buscar los grupos
     * @return Lista de grupos que el usuario pertenece
     */
    suspend fun getGroupsOfUser(idusuario: Int): List<EntidadGrupo>

    /**
     * Consigue el rol de un usuario en un grupo
     * @return NULL si el usuario no es parte del grupo
     */
    suspend fun getUserRoleOnGroup(idusuario: Int, idgrupo: Int): GroupRoles?

    /**
     * Se añade el usuario con el ID indicado al grupo
     * @param idusuario ID del usuario a añadir
     * @param idgrupo ID del grupo a añadir
     */
    suspend fun addUserToGroup(idusuario: Int, idgrupo: Int): Boolean

    /**
     * Se elimina el usuario con el ID indicado del grupo
     * @param idusuario ID del usuario a eliminar
     * @param idgrupo ID del grupo del que se elimina
     */
    suspend fun removeUserFromGroup(idusuario: Int, idgrupo: Int): Boolean

    /**
     * Transfiere la propiedad de un grupo.
     * @param gid ID del grupo
     * @param newOwnerId ID del nuevo propietario
     */
    suspend fun passGroupOwnership(gid: Int, newOwnerId: Int): Boolean

    /**
     * Comprueba si el usuario indicado es admin del grupo
     */
    suspend fun checkIfUserIsGroupAdmin(uid : Int, gid : Int) : Boolean

    suspend fun editGroup(gid: Int, name: String?, desc: String?) : Boolean

    suspend fun deleteGroup(gid : Int) : Boolean

    /**
     * Se crea una tarea nueva
     * @param gid El grupo al que pertenecera la tarea
     * @param titulo El titulo de la tarea
     * @param descripcion La descripcion de la tarea
     * @param tipo El tipo de tarea a agregar
     * @param prioridad La prioridad que tiene la tarea
     * @param recompensaXp El XP que se concedera al usuario por realizarla
     * @param recompensaLudion El numero de ludiones que se daran al usuario por realizar la tarea
     * @return El ID de la tarea creada. -1 si extraUnico es null y es una tarea unica, -2 si extraHabito es null y es una habito
     */
    suspend fun createTarea(
        gid: Int,
        titulo: String,
        descripcion: String = "",
        tipo: TaskType,
        prioridad: Int = 0,
        recompensaXp: Int = 0,
        recompensaLudion: Int = 0,
        extraUnico : TareaUniqueData? = null,
        extraHabito : TareaHabitData? = null,
        idObjetivo : Int? = null
    ): Int

    suspend fun getGroupByTask(tid : Int) : EntidadGrupo?

    suspend fun editTask(
        gid : Int,
        titulo : String? = null,
        descripcion: String? = null,
        prioridad: Int? = null
    ) : Boolean

    suspend fun getTareasByGroup(gid: Int): List<EntidadTarea>

    suspend fun completeTarea(tid: Int, uid: Int): Boolean

    suspend fun deleteTarea(tid : Int) : Boolean

    // madre mia el formatter este
    suspend fun getStoreItems(uid: Int, translated : Boolean = true): List<CosmeticResponseDTO>
    suspend fun buyCosmetic(uid: Int, cosmeticId: Int): BuyCosmeticResponse
    suspend fun equipCosmetic(uid: Int, cosmeticId: Int): BuyCosmeticResponse
    suspend fun createCosmetic(
        name: String,
        desc: String,
        type: CosmeticType,
        price: Int,
        assetRef: String,
        theme: String,
        coleccion: String,
        layer : AvatarLayer?,
        rarity: RarityType
    ): Boolean

    suspend fun retrieveAvatar(uid: Int) : List<AvatarLayerDTO>

    suspend fun saveConfirmationCode(uid: Int, code: String)
    suspend fun verifyConfirmationCode(email: String, code: String): Boolean
    suspend fun isUserConfirmed(email: String): Boolean



    suspend fun adminUpdateCosmetic(cid: Int,
                                    name: String,
                                    desc: String,
                                    type: CosmeticType,
                                    price: Int,
                                    assetRef: String,
                                    theme: String,
                                    coleccion: String,
                                    layer : AvatarLayer?,
                                    rarity: RarityType) : Boolean
    suspend fun admindeleteCosmetic(cid : Int) : Boolean
    suspend fun adminGetAllUsers() : List<DatosSimpleUsuarios>
    suspend fun adminGetAllGroups() : List<DatosSimpleGrupo>

    suspend fun createGroupInvite(
        gid: Int,
        code: String,
        codeHash: String,
        createdByUid: Int,
        createdAt: LocalDateTime,
        expiresAt: LocalDateTime?,
        maxUses: Int?,
    ): Int

    suspend fun revokeGroupInvite(
        gid: Int,
        codeHash: String,
        revokedAt: LocalDateTime,
    ): Boolean

    suspend fun listGroupInvites(gid: Int, includeRevoked: Boolean = false): List<GroupInviteDb>

    suspend fun getGroupInviteByHash(codeHash: String): GroupInviteDb?

    suspend fun tryConsumeInvite(inviteId: Int, now: LocalDateTime): Boolean

    suspend fun appendGroupAuditEvent(
        gid: Int,
        actorUid: Int?,
        eventType: String,
        payloadJson: String?,
        createdAt: LocalDateTime,
    ): Int

    suspend fun listGroupAuditEvents(gid: Int, limit: Int, offset: Long): List<GroupAuditEventDb>

    suspend fun listGroupMembers(gid: Int): List<GroupMemberDb>

    suspend fun setGroupMemberRole(gid: Int, uid: Int, role: GroupRoles): Boolean
}

fun getDatabaseDaoImpl(): DatabaseDAO {
    return DatabaseDAOImpl()
}

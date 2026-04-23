package groups.types

import kotlinx.serialization.Serializable

/**
 * Salida de un grupo en /group/userGroups
 */
@Serializable
data class SingleGroupOut(
    /** ID del grupo */
    val id : Int,
    /** Nombre del grupo */
    val name : String,
    /** La descripcion del grupo*/
    val description : String,
    /** El rol expresado de forma entera
     * @see ROLE_USERNORMAL
     * @see ROLE_USERMOD
     * @see ROLE_USEROWNER */
    val role: Int
)

/**
 * Salida de la ruta /group/userGroups
 */
@Serializable
data class AllGroupsResponse(
    val groupList : List<SingleGroupOut>
)
@Serializable
data class CreateGroupRequest(
    val name : String,
    val desc : String
)

@Serializable
data class AddUserRequest(
    val gid : Int,
    val userId : Int
)

@Serializable
data class DeleteGroupRequest (
    val gid : Int
)

@Serializable
data class EditGroupRequest (
    val gid : Int,
    val name: String? = null,
    val desc: String? = null
)

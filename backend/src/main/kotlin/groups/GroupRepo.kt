package com.astrais.groups

import groups.types.*

val ADDUSEROP_OK = 0
val ADDUSEROP_NOGROUP = -1
val ADDUSEROP_ALREADYJOINED = -2

enum class AddUserReturn {
    OK,
    NOGROUP,
    ALREADYJOINED,
    NOPERMISSION,
    CONNERR
}

interface GroupRepo {
    /**
     * Devuelve una lista de grupos del usuario con el ID dado.
     * @param userId El ID del usuario
     */
    suspend fun getAllUserGroups(userId : Int) : List<SingleGroupOut>

    /**
     * Crea un grupo cerrado nuevo.
     * @param name Nombre del grupo
     * @param desc Descripcion del grupo
     * @return El ID del grupo
     */
    suspend fun createGroup(name : String, desc : String, ownerId : Int) : Int

    /**
     * Añade un usuario al grupo
     * @param requesterId El ID del usuario que pide que se agregue
     * @param userId El ID del usuario a intentar agregar
     * @param gid El ID del grupo a agregar
     */
    suspend fun addUser(requesterId : Int, userId: Int, gid : Int) : AddUserReturn

    /**
     * Se borra el grupo entero
     * @param gid El ID del grupo
     * @param uid El ID del usuario que quiere borrarlo
     * @return False si no tiene permisos
     */
    suspend fun deleteGroup(gid : Int, uid : Int) : Boolean

    /**
     * Se edita un grupo
     * @param gid El ID del grupo
     * @param uid El ID del usuario que quiere editarlo
     * @param name El nuevo nombre, NULL si no se quiere cambiar
     * @param desc La nueva descripcion, NULL si no se quiere cambiar
     */
    suspend fun editGroup(gid : Int, uid : Int, name: String? = null, desc: String? = null) : Boolean
}

fun getGroupRepoImpl() : GroupRepo{
    return GroupRepoImpl()
}
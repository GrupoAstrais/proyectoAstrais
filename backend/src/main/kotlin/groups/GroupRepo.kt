package com.astrais.groups

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
}

fun getGroupRepoImpl() : GroupRepo{
    return GroupRepoImpl()
}
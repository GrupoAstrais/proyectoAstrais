package com.astrais.db

import CosmeticResponseDTO
import com.astrais.LANG_CODE_ENGLISH

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
        suspend fun getUsuario(emailusu: String): EntidadUsuario?
        suspend fun getUsuarioByID(id: Int): EntidadUsuario?
        suspend fun deleteUsuario(id: Int): Boolean

        suspend fun setUserLastLogin(ent: EntidadUsuario)

        suspend fun createGroup(
                grpownerId: Int,
                grpname: String,
                grpdescription: String = "",
                personal: Boolean = false
        ): Int
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

        suspend fun addUserToGroup(idusuario: Int, idgrupo: Int): Boolean

        suspend fun createTarea(
                gid: Int,
                titulo: String,
                descripcion: String = "",
                tipo: TaskType,
                prioridad: Int = 0,
                recompensaXp: Int = 0,
                recompensaLudion: Int = 0
        ): Int

        suspend fun getTareasByGroup(gid: Int): List<EntidadTarea>

        suspend fun completeTarea(tid: Int, uid: Int): Boolean

        // madre mia el formatter este
        suspend fun getStoreItems(uid: Int): List<CosmeticResponseDTO>
        suspend fun buyCosmetic(uid: Int, cosmeticId: Int): Boolean
        suspend fun equipCosmetic(uid: Int, cosmeticId: Int): Boolean
        suspend fun createCosmetic(
                name: String,
                desc: String,
                type: CosmeticType,
                price: Int,
                assetRef: String,
                theme: String,
                coleccion: String
        ): Boolean
}

fun getDatabaseDaoImpl(): DatabaseDAO {
        return DatabaseDAOImpl()
}

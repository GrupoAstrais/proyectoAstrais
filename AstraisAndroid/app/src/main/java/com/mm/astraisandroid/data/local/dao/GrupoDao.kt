package com.mm.astraisandroid.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.mm.astraisandroid.data.local.entities.GrupoEntity
import kotlinx.coroutines.flow.Flow

/**
 * Interfaz de acceso a datos (DAO) para la gestión de grupos en la base de datos local Room.
 *
 * Proporciona operaciones de consulta, inserción y reemplazo completo de grupos,
 * siguiendo la estrategia de sincronización "replace-all" para mantener la caché local
 * consistente con el servidor.
 */
@Dao
interface GrupoDao {
    /**
     * Obtiene todos los grupos almacenados localmente ordenados alfabéticamente.
     * @return [Flow] que emite la lista de grupos cada vez que cambia la base de datos.
     */
    @Query("SELECT * FROM grupos ORDER BY name ASC")
    fun getAllGroups(): Flow<List<GrupoEntity>>

    /**
     * Inserta o actualiza una lista de grupos en la base de datos local.
     * @param groups Lista de entidades de grupo a insertar o reemplazar.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroups(groups: List<GrupoEntity>)

    /**
     * Elimina un grupo específico de la base de datos local.
     * @param gid Identificador del grupo a eliminar.
     */
    @Query("DELETE FROM grupos WHERE id = :gid")
    suspend fun deleteGroupById(gid: Int)

    /**
     * Elimina todos los grupos de la base de datos local.
     */
    @Query("DELETE FROM grupos")
    suspend fun clearAll()

    /**
     * Reemplaza completamente todos los grupos locales con una nueva lista del servidor.
     * Esta operación es atómica: primero limpia la tabla y luego inserta los nuevos datos.
     * @param groups Lista completa de grupos obtenidos del servidor.
     */
    @Transaction
    suspend fun replaceAll(groups: List<GrupoEntity>) {
        clearAll()
        insertGroups(groups)
    }
}

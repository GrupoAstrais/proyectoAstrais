package com.mm.astraisandroid.data.local.dao

import androidx.room.*
import com.mm.astraisandroid.data.local.entities.TareaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TareaDao {
    /**
     * Devuelve todas las tareas ordenadas por prioridad
     * */
    @Query("SELECT * FROM tareas ORDER BY prioridad DESC")
    fun getAllTareas(): Flow<List<TareaEntity>>

    /**
     * Inserta o actualiza tareas que vienen del backend
     * */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTareas(tareas: List<TareaEntity>): List<Long>

    /**
     * Marca una tarea como completada en local
     * */
    @Query("UPDATE tareas SET estado = 'COMPLETE', isPendingSync = 1 WHERE id = :tid")
    suspend fun markAsCompleted(tid: Int): Int

    @Query("DELETE FROM tareas WHERE id = :tid")
    suspend fun deleteTareaById(tid: Int)

    @Query("UPDATE tareas SET titulo = :titulo, descripcion = :desc, prioridad = :prio WHERE id = :tid")
    suspend fun updateTareaDetails(titulo: String, desc: String, prio: Int, tid: Int)

    /**
     * Elimina todas las tareas
     * */
    @Query("DELETE FROM tareas")
    suspend fun clearAll(): Int

    @Query("DELETE FROM tareas WHERE isPendingSync = 0")
    suspend fun clearSyncedTareas(): Int

    @Query("UPDATE tareas SET estado = 'ACTIVE' WHERE id = :id")
    suspend fun markAsActive(id: Int)

    @Query("UPDATE tareas SET id = :newId WHERE id = :oldId")
    suspend fun updateTareaId(oldId: Int, newId: Int)

    @Query("UPDATE tareas SET idObjetivo = :newId WHERE idObjetivo = :oldId")
    suspend fun updateParentId(oldId: Int, newId: Int)
}
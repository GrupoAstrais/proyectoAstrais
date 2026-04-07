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

    /**
     * Elimina todas las tareas
     * */
    @Query("DELETE FROM tareas")
    suspend fun clearAll(): Int

    @Query("DELETE FROM tareas WHERE isPendingSync = 0")
    suspend fun clearSyncedTareas(): Int
}
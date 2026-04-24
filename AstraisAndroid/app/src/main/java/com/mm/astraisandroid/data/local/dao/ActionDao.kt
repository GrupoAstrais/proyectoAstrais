package com.mm.astraisandroid.data.local.dao

import androidx.room.*
import com.mm.astraisandroid.data.local.entities.PendingAction

@Dao
interface ActionDao {
    /**
     * Inserta una nueva acción en la cola
     */
    @Insert
    suspend fun addAction(action: PendingAction): Long

    /**
     * Obtiene todas las acciones guardadas para ser procesadas por el SyncWorker
     */
    @Query("SELECT * FROM pending_actions ORDER BY createdAt ASC")
    suspend fun getAllPending(): List<PendingAction>

    /**
     * Elimina una acción una vez que el servidor ha confirmado que se procesó con éxito
     */
    @Delete
    suspend fun removeAction(action: PendingAction): Int

    @Query("UPDATE pending_actions SET targetId = :newId WHERE targetId = :oldId")
    suspend fun updateTargetIds(oldId: Int, newId: Int)
}
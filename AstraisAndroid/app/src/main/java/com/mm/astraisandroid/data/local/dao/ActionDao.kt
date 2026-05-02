package com.mm.astraisandroid.data.local.dao

import androidx.room.*
import com.mm.astraisandroid.data.local.entities.PendingAction

@Dao
interface ActionDao {
    @Insert
    suspend fun addAction(action: PendingAction): Long

    @Query("SELECT * FROM pending_actions ORDER BY createdAt ASC")
    suspend fun getAllPending(): List<PendingAction>

    @Query("SELECT * FROM pending_actions ORDER BY createdAt ASC")
    suspend fun getAllPendingOrdered(): List<PendingAction>

    @Delete
    suspend fun removeAction(action: PendingAction): Int

    @Query("DELETE FROM pending_actions WHERE actionId = :actionId")
    suspend fun deleteAction(actionId: Int): Int

    @Query("UPDATE pending_actions SET retryCount = retryCount + 1, lastError = :error WHERE actionId = :actionId")
    suspend fun incrementRetryCount(actionId: Int, error: String?)

    @Query("DELETE FROM pending_actions WHERE retryCount >= :maxRetries")
    suspend fun deleteExceededRetries(maxRetries: Int = 3): Int

    @Query("UPDATE pending_actions SET targetId = :newId WHERE targetId = :oldId")
    suspend fun updateTargetIds(oldId: Int, newId: Int)

    @Query("UPDATE pending_actions SET data = :newData WHERE actionId = :actionId")
    suspend fun updateActionData(actionId: Int, newData: String)

    @Query("DELETE FROM pending_actions")
    suspend fun clearAll()
}
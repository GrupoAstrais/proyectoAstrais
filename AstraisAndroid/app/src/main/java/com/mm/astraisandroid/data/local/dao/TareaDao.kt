package com.mm.astraisandroid.data.local.dao

import androidx.room.*
import com.mm.astraisandroid.data.local.entities.PendingAction
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

    /**
     * Elimina todas las tareas
     * */
    @Query("DELETE FROM tareas")
    suspend fun clearAll(): Int

    @Query("UPDATE tareas SET idObjetivo = :newId WHERE idObjetivo = :oldId")
    suspend fun updateParentId(oldId: Int, newId: Int)

    @Query("SELECT * FROM tareas WHERE id < 0")
    suspend fun getGuestTareas(): List<TareaEntity>

    @Query("UPDATE tareas SET isPendingSync = 1 WHERE id = :id")
    suspend fun markAsPendingSync(id: Int)

    @Query("SELECT * FROM tareas WHERE id = :id LIMIT 1")
    suspend fun getTaskById(id: Int): TareaEntity?

    @Query("SELECT * FROM tareas WHERE idObjetivo = :parentId")
    suspend fun getSubtasksForTask(parentId: Int): List<TareaEntity>

    @Query("SELECT * FROM tareas WHERE isPendingSync = 1")
    suspend fun getPendingSyncTasks(): List<TareaEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTarea(tarea: TareaEntity): Long

    @Query("UPDATE tareas SET estado = 'ACTIVE', isPendingSync = 1 WHERE id = :id")
    suspend fun markAsActive(id: Int)

    @Query("UPDATE tareas SET titulo = :titulo, descripcion = :desc, prioridad = :prio, extraUnicoFecha = :dueDate, extraHabitoFrecuencia = :freq, isPendingSync = 1 WHERE id = :tid")
    suspend fun updateTareaDetails(titulo: String, desc: String, prio: Int, dueDate: String?, freq: String?, tid: Int)

    @Query("DELETE FROM tareas WHERE isPendingSync = 0 AND id >= 0")
    suspend fun clearSyncedTareas(): Int

    @Query("UPDATE tareas SET id = :newId, isPendingSync = 0 WHERE id = :oldId")
    suspend fun updateTareaId(oldId: Int, newId: Int)

    @Transaction
    suspend fun completeTaskAndQueueAction(tid: Int, action: PendingAction, actionDao: ActionDao) {
        markAsCompleted(tid)
        actionDao.addAction(action)
    }

    @Transaction
    suspend fun uncompleteTaskAndQueueAction(tid: Int, action: PendingAction, actionDao: ActionDao) {
        markAsActive(tid)
        actionDao.addAction(action)
    }

    @Transaction
    suspend fun createTaskWithAction(entity: TareaEntity, action: PendingAction, actionDao: ActionDao) {
        insertTarea(entity)
        actionDao.addAction(action)
    }

    @Transaction
    suspend fun updateIdAndParentReferences(oldId: Int, newId: Int) {
        updateTareaId(oldId, newId)
        updateParentId(oldId, newId)
    }

    @Transaction
    suspend fun syncRemoteTasks(remoteEntities: List<TareaEntity>) {
        val pendingTasks = getPendingSyncTasks()
        val pendingIds = pendingTasks.map { it.id }.toSet()
        clearSyncedTareas()
        insertTareas(remoteEntities.filter { it.id !in pendingIds })
        insertTareas(pendingTasks)
    }
}
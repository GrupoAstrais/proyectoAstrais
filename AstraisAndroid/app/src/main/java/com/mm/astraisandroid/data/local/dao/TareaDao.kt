package com.mm.astraisandroid.data.local.dao

import androidx.room.*
import com.mm.astraisandroid.data.local.entities.PendingAction
import com.mm.astraisandroid.data.local.entities.TareaEntity
import kotlinx.coroutines.flow.Flow

/**
 * Interfaz de acceso a datos (DAO) para la gestión de tareas en la base de datos local Room.
 *
 * Proporciona operaciones de consulta, inserción, actualización y eliminación de tareas,
 * así como métodos transaccionales para mantener la consistencia entre tareas y acciones pendientes.
 */
@Dao
interface TareaDao {
    /**
     * Devuelve todas las tareas ordenadas por prioridad descendente.
     * @return [Flow] que emite la lista de tareas cada vez que cambia la base de datos.
     */
    @Query("SELECT * FROM tareas ORDER BY prioridad DESC")
    fun getAllTareas(): Flow<List<TareaEntity>>

    /**
     * Inserta o actualiza tareas provenientes del servidor en la base de datos local.
     * @param tareas Lista de entidades a insertar o reemplazar.
     * @return Lista de IDs de fila afectados por la operación.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTareas(tareas: List<TareaEntity>): List<Long>

    /**
     * Marca una tarea como completada en la base de datos local y la encola para sincronización.
     * @param tid Identificador de la tarea a completar.
     * @return Número de filas afectadas.
     */
    @Query("UPDATE tareas SET estado = 'COMPLETE', isPendingSync = 1 WHERE id = :tid")
    suspend fun markAsCompleted(tid: Int): Int

    /**
     * Elimina una tarea de la base de datos local por su identificador.
     * @param tid Identificador de la tarea a eliminar.
     */
    @Query("DELETE FROM tareas WHERE id = :tid")
    suspend fun deleteTareaById(tid: Int)

    /**
     * Elimina todas las tareas de la base de datos local.
     * @return Número de filas eliminadas.
     */
    @Query("DELETE FROM tareas")
    suspend fun clearAll(): Int

    /**
     * Actualiza el identificador padre de las subtareas cuando cambia el ID de una tarea padre.
     * @param oldId Identificador anterior de la tarea padre.
     * @param newId Nuevo identificador de la tarea padre.
     */
    @Query("UPDATE tareas SET idObjetivo = :newId WHERE idObjetivo = :oldId")
    suspend fun updateParentId(oldId: Int, newId: Int)

    /**
     * Obtiene todas las tareas creadas en modo invitado (IDs negativos).
     * @return Lista de tareas del usuario invitado.
     */
    @Query("SELECT * FROM tareas WHERE id < 0")
    suspend fun getGuestTareas(): List<TareaEntity>

    /**
     * Marca una tarea como pendiente de sincronización con el servidor.
     * @param id Identificador de la tarea a marcar.
     */
    @Query("UPDATE tareas SET isPendingSync = 1 WHERE id = :id")
    suspend fun markAsPendingSync(id: Int)

    /**
     * Obtiene una tarea específica por su identificador.
     * @param id Identificador de la tarea.
     * @return La entidad de la tarea o `null` si no existe.
     */
    @Query("SELECT * FROM tareas WHERE id = :id LIMIT 1")
    suspend fun getTaskById(id: Int): TareaEntity?

    /**
     * Obtiene todas las subtareas asociadas a una tarea padre.
     * @param parentId Identificador de la tarea padre.
     * @return Lista de subtareas.
     */
    @Query("SELECT * FROM tareas WHERE idObjetivo = :parentId")
    suspend fun getSubtasksForTask(parentId: Int): List<TareaEntity>

    /**
     * Obtiene todas las tareas que tienen cambios locales pendientes de sincronización.
     * @return Lista de tareas pendientes de sincronización.
     */
    @Query("SELECT * FROM tareas WHERE isPendingSync = 1")
    suspend fun getPendingSyncTasks(): List<TareaEntity>

    /**
     * Inserta una única tarea en la base de datos local.
     * @param tarea Entidad de la tarea a insertar.
     * @return ID de la fila insertada.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTarea(tarea: TareaEntity): Long

    /**
     * Marca una tarea como activa (no completada) y la encola para sincronización.
     * @param id Identificador de la tarea a reactivar.
     */
    @Query("UPDATE tareas SET estado = 'ACTIVE', isPendingSync = 1 WHERE id = :id")
    suspend fun markAsActive(id: Int)

    /**
     * Actualiza los detalles de una tarea existente y la marca como pendiente de sincronización.
     * @param titulo Nuevo título de la tarea.
     * @param desc Nueva descripción.
     * @param prio Nueva prioridad.
     * @param dueDate Nueva fecha límite para tareas únicas.
     * @param freq Nueva frecuencia para hábitos.
     * @param tid Identificador de la tarea a actualizar.
     */
    @Query("UPDATE tareas SET titulo = :titulo, descripcion = :desc, prioridad = :prio, extraUnicoFecha = :dueDate, extraHabitoFrecuencia = :freq, isPendingSync = 1 WHERE id = :tid")
    suspend fun updateTareaDetails(titulo: String, desc: String, prio: Int, dueDate: String?, freq: String?, tid: Int)

    /**
     * Elimina todas las tareas que ya están sincronizadas con el servidor (IDs no negativos).
     * Las tareas con cambios pendientes se conservan.
     * @return Número de filas eliminadas.
     */
    @Query("DELETE FROM tareas WHERE isPendingSync = 0 AND id >= 0")
    suspend fun clearSyncedTareas(): Int

    /**
     * Actualiza el identificador de una tarea (de temporal a servidor) y marca como sincronizada.
     * @param oldId Identificador temporal anterior.
     * @param newId Nuevo identificador asignado por el servidor.
     */
    @Query("UPDATE tareas SET id = :newId, isPendingSync = 0 WHERE id = :oldId")
    suspend fun updateTareaId(oldId: Int, newId: Int)

    /**
     * Completa una tarea y encola la acción pendiente de sincronización en una transacción atómica.
     * @param tid Identificador de la tarea a completar.
     * @param action Acción pendiente a registrar.
     * @param actionDao DAO para insertar la acción en la cola de sincronización.
     */
    @Transaction
    suspend fun completeTaskAndQueueAction(tid: Int, action: PendingAction, actionDao: ActionDao) {
        markAsCompleted(tid)
        actionDao.addAction(action)
    }

    /**
     * Revierte el estado completado de una tarea y encola la acción pendiente en una transacción atómica.
     * @param tid Identificador de la tarea a revertir.
     * @param action Acción pendiente a registrar.
     * @param actionDao DAO para insertar la acción en la cola de sincronización.
     */
    @Transaction
    suspend fun uncompleteTaskAndQueueAction(tid: Int, action: PendingAction, actionDao: ActionDao) {
        markAsActive(tid)
        actionDao.addAction(action)
    }

    /**
     * Inserta una nueva tarea y registra la acción pendiente en una transacción atómica.
     * @param entity Entidad de la tarea a insertar.
     * @param action Acción pendiente a registrar.
     * @param actionDao DAO para insertar la acción en la cola de sincronización.
     */
    @Transaction
    suspend fun createTaskWithAction(entity: TareaEntity, action: PendingAction, actionDao: ActionDao) {
        insertTarea(entity)
        actionDao.addAction(action)
    }

    /**
     * Actualiza el ID de una tarea y todas las referencias de sus subtareas en una transacción atómica.
     * @param oldId Identificador temporal anterior.
     * @param newId Nuevo identificador asignado por el servidor.
     */
    @Transaction
    suspend fun updateIdAndParentReferences(oldId: Int, newId: Int) {
        updateTareaId(oldId, newId)
        updateParentId(oldId, newId)
    }

    /**
     * Sincroniza las tareas remotas con la caché local preservando las tareas con cambios pendientes.
     * Elimina las tareas sincronizadas e inserta las remotas, reinsertando las pendientes al final.
     * @param remoteEntities Lista de tareas obtenidas del servidor.
     */
    @Transaction
    suspend fun syncRemoteTasks(remoteEntities: List<TareaEntity>) {
        val pendingTasks = getPendingSyncTasks()
        val pendingIds = pendingTasks.map { it.id }.toSet()
        clearSyncedTareas()
        insertTareas(remoteEntities.filter { it.id !in pendingIds })
        insertTareas(pendingTasks)
    }
}
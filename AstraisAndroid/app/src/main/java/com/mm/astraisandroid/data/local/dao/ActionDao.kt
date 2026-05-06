package com.mm.astraisandroid.data.local.dao

import androidx.room.*
import com.mm.astraisandroid.data.local.entities.PendingAction

/**
 * Interfaz de acceso a datos (DAO) para la gestión de acciones pendientes de sincronización.
 *
 * Administra la cola de operaciones offline (crear, completar, eliminar, editar tareas)
 * que deben enviarse al servidor cuando se restablece la conexión.
 */
@Dao
interface ActionDao {
    /**
     * Agrega una nueva acción a la cola de sincronización.
     * @param action Acción pendiente a registrar.
     * @return ID de la fila insertada.
     */
    @Insert
    suspend fun addAction(action: PendingAction): Long

    /**
     * Obtiene todas las acciones pendientes ordenadas por fecha de creación (FIFO).
     * @return Lista de acciones pendientes.
     */
    @Query("SELECT * FROM pending_actions ORDER BY createdAt ASC")
    suspend fun getAllPending(): List<PendingAction>

    /**
     * Obtiene todas las acciones pendientes ordenadas cronológicamente.
     * Alias de [getAllPending] para semántica explícita de ordenamiento.
     * @return Lista de acciones pendientes ordenadas.
     */
    @Query("SELECT * FROM pending_actions ORDER BY createdAt ASC")
    suspend fun getAllPendingOrdered(): List<PendingAction>

    /**
     * Elimina una acción específica de la cola tras sincronizarla exitosamente.
     * @param action Acción a eliminar.
     * @return Número de filas eliminadas.
     */
    @Delete
    suspend fun removeAction(action: PendingAction): Int

    /**
     * Elimina una acción de la cola por su identificador.
     * @param actionId Identificador de la acción a eliminar.
     * @return Número de filas eliminadas.
     */
    @Query("DELETE FROM pending_actions WHERE actionId = :actionId")
    suspend fun deleteAction(actionId: Int): Int

    /**
     * Incrementa el contador de reintentos de una acción y registra el último error.
     * @param actionId Identificador de la acción.
     * @param error Mensaje de error ocurrido durante el intento de sincronización.
     */
    @Query("UPDATE pending_actions SET retryCount = retryCount + 1, lastError = :error WHERE actionId = :actionId")
    suspend fun incrementRetryCount(actionId: Int, error: String?)

    /**
     * Elimina las acciones que han excedido el límite máximo de reintentos.
     * @param maxRetries Límite de reintentos antes de eliminar (por defecto 3).
     * @return Número de acciones eliminadas.
     */
    @Query("DELETE FROM pending_actions WHERE retryCount >= :maxRetries")
    suspend fun deleteExceededRetries(maxRetries: Int = 3): Int

    /**
     * Actualiza los identificadores de destino en las acciones pendientes cuando un ID temporal
     * se resuelve al ID asignado por el servidor.
     * @param oldId Identificador temporal anterior.
     * @param newId Nuevo identificador asignado por el servidor.
     */
    @Query("UPDATE pending_actions SET targetId = :newId WHERE targetId = :oldId")
    suspend fun updateTargetIds(oldId: Int, newId: Int)

    /**
     * Actualiza los datos JSON de una acción pendiente.
     * @param actionId Identificador de la acción.
     * @param newData Nuevo contenido JSON de la petición.
     */
    @Query("UPDATE pending_actions SET data = :newData WHERE actionId = :actionId")
    suspend fun updateActionData(actionId: Int, newData: String)

    /**
     * Elimina todas las acciones pendientes de la cola.
     */
    @Query("DELETE FROM pending_actions")
    suspend fun clearAll()
}
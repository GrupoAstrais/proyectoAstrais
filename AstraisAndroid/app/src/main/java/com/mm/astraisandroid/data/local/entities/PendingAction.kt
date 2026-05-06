package com.mm.astraisandroid.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad que representa una acción pendiente de sincronización con el servidor.
 *
 * Se almacena en la tabla `pending_actions` y forma parte de la cola de operaciones
 * offline. Cada acción contiene el tipo de operación y los datos JSON necesarios
 * para replicar la petición HTTP cuando se restablezca la conexión.
 *
 * @property actionId Identificador único auto-generado de la acción.
 * @property type Tipo de operación: `CREATE_TASK`, `COMPLETE_TASK`, `UNCOMPLETE_TASK`,
 *   `DELETE_TASK`, `EDIT_TASK`.
 * @property data Datos JSON de la petición HTTP a replicar.
 * @property targetId Identificador de la tarea objetivo, o `null` si no aplica.
 * @property createdAt Marca de tiempo de creación de la acción (epoch millis).
 * @property retryCount Número de intentos fallidos de sincronización.
 * @property lastError Último mensaje de error registrado durante la sincronización.
 */
@Entity(tableName = "pending_actions")
data class PendingAction(
    @PrimaryKey(autoGenerate = true) val actionId: Int = 0,
    val type: String,           // "CREATE_TASK", "COMPLETE_TASK", "UNCOMPLETE_TASK", "DELETE_TASK", "EDIT_TASK"
    val data: String,           // El JSON de la petición
    val targetId: Int? = null,  // ID de la tarea si es para completar
    val createdAt: Long = System.currentTimeMillis(),
    val retryCount: Int = 0,    // Contador de reintentos
    val lastError: String? = null // Último mensaje de error
)
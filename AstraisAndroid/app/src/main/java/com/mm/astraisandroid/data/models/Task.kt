package com.mm.astraisandroid.data.models

/**
 * Enum que representa los posibles estados de una tarea.
 */
enum class TaskState {
    ACTIVE,   // Tarea activa y pendiente de realizar
    COMPLETE, // Tarea ya finalizada
    DUE,
    UNKNOWN
}

/**
 * Enum que clasifica los diferentes tipos de tareas.
 */
enum class TaskType {
    UNICO,    // Tarea de una sola vez
    HABITO,   // Tarea que se repite con cierta frecuencia
    OBJETIVO, // Tarea padre que agrupa otras subtareas
    UNKNOWN
}

/**
 * Enum que define el nivel de prioridad de una tarea.
 */
enum class TaskPriority {
    LOW,    // Prioridad baja (0)
    MEDIUM, // Prioridad media (1)
    HIGH    // Prioridad alta (2)
}

/**
 * Modelo de dominio que representa una tarea.
 * Esta clase es utilizada por UI y la lógica de negocio.
 *
 * @property id Identificador único de la tarea.
 * @property title Nombre de la tarea.
 * @property description Detalles adicionales sobre lo que se debe hacer.
 * @property type El tipo de tarea.
 * @property state El estado actual de la tarea.
 * @property taskPriority La prioridad asignada a la tarea.
 * @property xpReward Cantidad de XP otorgada al completarla.
 * @property ludionReward Cantidad de Ludiones otorgada al completarla.
 * @property parentId ID de la tarea objetivo al que pertenece (null si es independiente).
 * @property isPendingSync Indica si la tarea tiene cambios locales que aún no se han enviado al servidor.
 * @property dueDate Fecha límite para completar la tarea (aplica en tareas de tipo UNICO).
 * @property habitFrequency Frecuencia de repetición (aplica en tareas de tipo HABITO).
 */
data class Task(
    val id: Int,
    val title: String,
    val description: String,
    val type: TaskType,
    val state: TaskState,
    val taskPriority: TaskPriority,
    val xpReward: Int,
    val ludionReward: Int,
    val parentId: Int?,
    val isPendingSync: Boolean,
    val dueDate: String?,
    val habitFrequency: String?
)
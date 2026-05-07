package com.mm.astraisandroid.data.models

/**
 * Enum que representa los posibles estados de una tarea.
 *
 * @property ACTIVE Tarea activa y pendiente de realizar.
 * @property COMPLETE Tarea ya finalizada.
 * @property DUE Tarea cuya fecha límite ha llegado o pasado.
 * @property UNKNOWN Estado no reconocido o no mapeado.
 */
enum class TaskState {
    ACTIVE,
    COMPLETE,
    DUE,
    UNKNOWN
}

/**
 * Enum que clasifica los diferentes tipos de tareas.
 *
 * @property UNICO Tarea de una sola vez con fecha límite opcional.
 * @property HABITO Tarea que se repite con cierta frecuencia (diaria, semanal, etc.).
 * @property OBJETIVO Tarea padre que agrupa otras subtareas.
 * @property UNKNOWN Tipo no reconocido o no mapeado.
 */
enum class TaskType {
    UNICO,
    HABITO,
    OBJETIVO,
    UNKNOWN
}

/**
 * Enum que define el nivel de prioridad de una tarea.
 *
 * @property LOW Prioridad baja (valor numérico 0).
 * @property MEDIUM Prioridad media (valor numérico 1).
 * @property HIGH Prioridad alta (valor numérico 2).
 */
enum class TaskPriority {
    LOW,
    MEDIUM,
    HIGH
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
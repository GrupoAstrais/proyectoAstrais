package com.mm.astraisandroid.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mm.astraisandroid.data.models.TaskPriority
import com.mm.astraisandroid.data.models.Task
import com.mm.astraisandroid.data.models.TaskState
import com.mm.astraisandroid.data.models.TaskType

/**
 * Entidad que representa una tarea almacenada en Room.
 * Actúa como caché offline y permite a la aplicación funcionar sin conexión a internet.
 *
 * @property id Identificador primario de la tarea.
 * @property titulo Nombre de la tarea.
 * @property descripcion Detalles de la tarea.
 * @property tipo Tipo de tarea.
 * @property estado Estado actual.
 * @property prioridad Nivel de prioridad.
 * @property recompensaXp Experiencia que otorga.
 * @property recompensaLudion Ludiones que otorga.
 * @property idObjetivo Identificador de la tarea objetivo padre, si existe.
 * @property isPendingSync Indica si hay cambios locales que el servidor aún desconoce.
 * @property extraUnicoFecha Fecha límite si la tarea es de tipo UNICO.
 * @property extraHabitoFrecuencia Frecuencia de repetición si la tarea es de tipo HABITO.
 */
@Entity(tableName = "tareas")
data class TareaEntity(
    @PrimaryKey val id: Int,
    val titulo: String,
    val descripcion: String,
    val tipo: String,
    val estado: String,
    val prioridad: Int,
    val recompensaXp: Int,
    val recompensaLudion: Int,
    val idObjetivo: Int? = null,
    val isPendingSync: Boolean = false,
    val extraUnicoFecha: String? = null,
    val extraHabitoFrecuencia: String? = null
)

/**
 * Función para transformar una entidad de la base de datos local ([TareaEntity])
 * en el modelo de dominio ([Task]) utilizado por la UI.
 *
 * @return El modelo de dominio para la UI.
 */
fun TareaEntity.toDomain(): Task {
    return Task(
        id = this.id,
        title = this.titulo,
        description = this.descripcion,
        type = runCatching { TaskType.valueOf(this.tipo) }.getOrDefault(TaskType.UNKNOWN),
        state = runCatching { TaskState.valueOf(this.estado) }.getOrDefault(TaskState.UNKNOWN),
        taskPriority = when (this.prioridad) {
            0 -> TaskPriority.LOW
            1 -> TaskPriority.MEDIUM
            2 -> TaskPriority.HIGH
            else -> TaskPriority.LOW
        },
        xpReward = this.recompensaXp,
        ludionReward = this.recompensaLudion,
        parentId = this.idObjetivo,
        isPendingSync = this.isPendingSync,
        dueDate = this.extraUnicoFecha,
        habitFrequency = this.extraHabitoFrecuencia
    )
}
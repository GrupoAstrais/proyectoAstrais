package com.mm.astraisandroid.data.api

import com.mm.astraisandroid.data.local.entities.TareaEntity
import kotlinx.serialization.Serializable

/**
 * DTO que representa la respuesta del servidor al consultar una tarea.
 *
 * @property id Identificador único de la tarea.
 * @property gid Identificador del grupo al que pertenece la tarea, o `null` si es personal.
 * @property uid Identificador del usuario creador de la tarea.
 * @property titulo Título descriptivo de la tarea.
 * @property descripcion Descripción detallada de la tarea.
 * @property tipo Tipo de tarea: `UNICO`, `HABITO`, etc.
 * @property estado Estado actual de la tarea: `active`, `completed`, etc.
 * @property prioridad Nivel de prioridad numérico de la tarea.
 * @property recompensaXp Cantidad de XP otorgada al completar la tarea.
 * @property recompensaLudion Cantidad de Ludiones otorgados al completar la tarea.
 * @property fechaValida Fecha límite o válida para tareas únicas (formato ISO-8601).
 * @property idObjetivo Identificador del objetivo asociado, si aplica.
 * @property extraUnico Datos adicionales específicos para tareas de tipo único.
 * @property extraHabito Datos adicionales específicos para tareas de tipo hábito.
 */
@Serializable
data class TaskResponse(
    val id: Int,
    val gid: Int?,
    val uid: Int?,
    val titulo: String,
    val descripcion: String,
    val tipo: String,
    val estado: String,
    val prioridad: Int,
    val recompensaXp: Int,
    val recompensaLudion: Int,
    val fechaValida: String? = null,
    val idObjetivo: Int? = null,
    val extraUnico: CreateTareaUniqueData? = null,
    val extraHabito: CreateTareaHabitData? = null
)

/**
 * Función para transformar la respuesta ([TaskResponse])
 * en una entidad de base de datos local ([TareaEntity]).
 *
 * @return La entidad configurada para ser guardada en local.
 */
fun TaskResponse.toEntity(): TareaEntity {
    return TareaEntity(
        id = this.id,
        titulo = this.titulo,
        descripcion = this.descripcion,
        tipo = this.tipo,
        estado = this.estado,
        prioridad = this.prioridad,
        recompensaXp = this.recompensaXp,
        recompensaLudion = this.recompensaLudion,
        idObjetivo = this.idObjetivo,
        extraUnicoFecha = this.extraUnico?.fechaLimite ?: this.fechaValida,
        extraHabitoFrecuencia = this.extraHabito?.frequency?.value,
        isPendingSync = false // Si viene del servidor, por definición ya está sincronizada
    )
}

/**
 * Enum que define las posibles frecuencias para las tareas de tipo Hábito.
 *
 * @property value El valor esperado/enviado al backend.
 */
enum class HabitFrequency(val value: String) {
    HOURLY("HOURLY"),
    DAILY("DAILY"),
    WEEKLY("WEEKLY"),
    MONTHLY("MONTHLY"),
    YEARLY("YEARLY");

    companion object {
        /**
         * Obtiene un [HabitFrequency] a partir de su valor en texto.
         * @return La frecuencia correspondiente o null si no hay coincidencia.
         */
        fun fromValue(value: String): HabitFrequency? =
            entries.find { it.value.equals(value, ignoreCase = true) }
    }
}

/**
 * DTO utilizado para enviar la petición de creación de una nueva tarea al servidor.
 *
 * @property gid Identificador del grupo donde se crea la tarea.
 * @property titulo Título de la nueva tarea.
 * @property descripcion Descripción de la tarea (vacío por defecto).
 * @property tipo Tipo de tarea: "UNICO", "HABITO" u "OBJETIVO".
 * @property prioridad Nivel de prioridad como entero (0=Baja, 1=Media, 2=Alta).
 * @property extraUnico Datos adicionales para tareas únicas (fecha límite), o `null`.
 * @property extraHabito Datos adicionales para hábitos (frecuencia), o `null`.
 * @property idObjetivo ID del objetivo padre si es subtarea, o `null`.
 */
@Serializable
data class CreateTareaRequest(
    val gid: Int?,
    val titulo: String,
    val descripcion: String = "",
    val tipo: String = "UNICO",
    val prioridad: Int = 0,
    val extraUnico : CreateTareaUniqueData? = null,
    val extraHabito : CreateTareaHabitData? = null,
    val idObjetivo: Int? = null
)

/**
 * DTO que contiene la configuración específica para crear una tarea de tipo Hábito.
 *
 * @property numeroFrecuencia Número de repeticiones por ciclo (por defecto 1).
 * @property frequency Enum de frecuencia: HOURLY, DAILY, WEEKLY, MONTHLY o YEARLY.
 */
@Serializable
data class CreateTareaHabitData(
    val numeroFrecuencia: Int = 1,
    val frequency: HabitFrequency = HabitFrequency.DAILY
)

/**
 * DTO que contiene la configuración específica para crear una tarea de tipo Única.
 *
 * @property fechaLimite Fecha límite en formato ISO-8601 (p. ej. "2026-12-31T23:59:59Z").
 */
@Serializable
data class CreateTareaUniqueData(
    val fechaLimite: String
)

/**
 * DTO para la edición parcial de una tarea existente.
 *
 * Los campos `null` no se modifican en el servidor, permitiendo actualizaciones selectivas.
 *
 * @property titulo Nuevo título, o `null` para no modificarlo.
 * @property descripcion Nueva descripción, o `null` para no modificarla.
 * @property prioridad Nueva prioridad, o `null` para no modificarla.
 * @property extraUnico Nuevos datos para tareas únicas, o `null` para no modificarlos.
 * @property extraHabito Nuevos datos para hábitos, o `null` para no modificarlos.
 * @property idObjetivo Nuevo identificador de objetivo, o `null` para no modificarlo.
 */
@Serializable
data class EditTareaRequest(
    val titulo: String? = null,
    val descripcion: String? = null,
    val prioridad: Int? = null,
    val extraUnico: CreateTareaUniqueData? = null,
    val extraHabito: CreateTareaHabitData? = null,
    val idObjetivo: Int? = null
)
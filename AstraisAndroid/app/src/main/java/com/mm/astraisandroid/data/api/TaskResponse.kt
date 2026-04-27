package com.mm.astraisandroid.data.api

import com.mm.astraisandroid.data.local.entities.TareaEntity
import kotlinx.serialization.Serializable

/**
 * DTO que representa la respuesta del servidor al consultar una tarea.
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
 */
@Serializable
data class CreateTareaHabitData(
    val numeroFrecuencia: Int = 1,
    val frequency: HabitFrequency = HabitFrequency.DAILY
)

/**
 * DTO que contiene la configuración específica para crear una tarea de tipo Única.
 */
@Serializable
data class CreateTareaUniqueData(
    val fechaLimite: String
)

@Serializable
data class EditTareaRequest(
    val titulo: String? = null,
    val descripcion: String? = null,
    val prioridad: Int? = null,
    val extraUnico: CreateTareaUniqueData? = null,
    val extraHabito: CreateTareaHabitData? = null,
    val idObjetivo: Int? = null
)
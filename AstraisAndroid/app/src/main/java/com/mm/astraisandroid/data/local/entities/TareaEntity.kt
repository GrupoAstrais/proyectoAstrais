package com.mm.astraisandroid.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Representa una tarea almacenada en la base de datos local
 */
@Entity(tableName = "tareas")
data class TareaEntity(
    @PrimaryKey val id: Int,
    val titulo: String,
    val descripcion: String,
    val tipo: String,
    val estado: String, // "ACTIVE" o "COMPLETE"
    val prioridad: Int, // 0: Baja, 1: Media, 2: Alta
    val recompensaXp: Int,
    val recompensaLudion: Int,
    val isPendingSync: Boolean = false // Si es true, significa que el usuario completó la tarea localmente pero el servidor aún no lo sabe D:
)
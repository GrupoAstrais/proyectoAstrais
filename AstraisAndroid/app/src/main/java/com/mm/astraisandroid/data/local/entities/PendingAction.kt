package com.mm.astraisandroid.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pending_actions")
data class PendingAction(
    @PrimaryKey(autoGenerate = true) val actionId: Int = 0,
    val type: String,           // "CREATE_TASK" o "COMPLETE_TASK"
    val data: String,           // El JSON de la petición
    val targetId: Int? = null,  // ID de la tarea si es para completar
    val createdAt: Long = System.currentTimeMillis()
)
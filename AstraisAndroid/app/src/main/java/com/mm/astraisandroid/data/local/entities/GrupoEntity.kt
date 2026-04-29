package com.mm.astraisandroid.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mm.astraisandroid.ui.features.groups.Grupo

@Entity(tableName = "grupos")
data class GrupoEntity(
    @PrimaryKey
    val id: Int,
    val name: String,
    val description: String,
    val role: Int
)

fun GrupoEntity.toDomain(): Grupo {
    return Grupo(
        id = id,
        name = name,
        subtitle = description,
        role = role
    )
}

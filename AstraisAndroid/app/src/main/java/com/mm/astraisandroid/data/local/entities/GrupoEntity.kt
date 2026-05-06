package com.mm.astraisandroid.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mm.astraisandroid.ui.features.groups.Grupo

/**
 * Entidad que representa un grupo almacenado en la base de datos local Room.
 *
 * Actúa como caché offline para los grupos del usuario, permitiendo acceso rápido
 * sin conexión a internet.
 *
 * @property id Identificador único del grupo (clave primaria).
 * @property name Nombre visible del grupo.
 * @property description Descripción del grupo.
 * @property role Rol del usuario en el grupo: `0` = Miembro, `1` = Moderador, `2` = Owner.
 */
@Entity(tableName = "grupos")
data class GrupoEntity(
    @PrimaryKey
    val id: Int,
    val name: String,
    val description: String,
    val role: Int
)

/**
 * Transforma una entidad de grupo local ([GrupoEntity]) al modelo de dominio ([Grupo])
 * utilizado por la interfaz de usuario.
 *
 * @return Instancia de [Grupo] mapeada desde la entidad local.
 */
fun GrupoEntity.toDomain(): Grupo {
    return Grupo(
        id = id,
        name = name,
        subtitle = description,
        role = role
    )
}

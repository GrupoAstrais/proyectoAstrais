package com.mm.astraisandroid.data.api

import com.mm.astraisandroid.data.models.Cosmetic
import com.mm.astraisandroid.data.models.CosmeticType
import kotlinx.serialization.Serializable

/**
 * DTO que representa la respuesta del servidor al consultar cosméticos.
 */
@Serializable
data class CosmeticResponse(
    val id: Int,
    val name: String,
    val desc: String,
    val type: String,
    val price: Int,
    val assetRef: String,
    val theme: String,
    val owned: Boolean,
    val coleccion: String,
    val equipped: Boolean
)


/**
 * Función para mapear un DTO de respuesta ([CosmeticResponse])
 * al modelo de dominio interno ([Cosmetic]).
 * * Si el servidor envía un tipo que no existe en el enum, asignará [CosmeticType.UNKNOWN]
 * por defecto.
 */
fun CosmeticResponse.toDomain(): Cosmetic {
    return Cosmetic(
        id = this.id,
        name = this.name,
        desc = this.desc,
        type = runCatching { CosmeticType.valueOf(this.type) }.getOrDefault(CosmeticType.UNKNOWN),
        price = this.price,
        assetRef = this.assetRef,
        theme = this.theme,
        coleccion = this.coleccion,
        owned = this.owned,
        equipped = this.equipped
    )
}
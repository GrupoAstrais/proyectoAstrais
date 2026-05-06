package com.mm.astraisandroid.data.api

import com.mm.astraisandroid.data.models.Cosmetic
import com.mm.astraisandroid.data.models.CosmeticType
import kotlinx.serialization.Serializable

/**
 * DTO que representa la respuesta del servidor al consultar cosméticos.
 *
 * @property id Identificador único del cosmético.
 * @property name Nombre visible del cosmético.
 * @property desc Descripción del cosmético.
 * @property type Tipo de cosmético como cadena (se mapea a [CosmeticType] en el dominio).
 * @property price Precio en Ludiones.
 * @property assetRef Referencia al asset gráfico del cosmético.
 * @property theme Tema visual asociado al cosmético.
 * @property rarity Rareza del cosmético (por defecto `COMUN`).
 * @property owned Indica si el usuario ya posee este cosmético.
 * @property coleccion Colección a la que pertenece el cosmético.
 * @property equipped Indica si el usuario tiene actualmente equipado este cosmético.
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
    val rarity: String = "COMUN",
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
        rarity = this.rarity,
        coleccion = this.coleccion,
        owned = this.owned,
        equipped = this.equipped
    )
}
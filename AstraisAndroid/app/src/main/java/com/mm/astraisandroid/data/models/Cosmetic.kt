package com.mm.astraisandroid.data.models

/**
 * Enum que define los diferentes tipos de cosméticos disponibles.
 */
enum class CosmeticType {
    PET,         // Mascota / compañero (compatible con backend)
    PET_SKIN,    // Aspecto para una mascota
    APP_THEME,   // Tema de colores para la interfaz de la aplicación
    PET_BASE,    // Aspecto base de una mascota / La mascota en sí
    AVATAR_PART, // Avatar de perfil del usuario
    UNKNOWN
}

/**
 * Modelo de dominio que representa un cosmético.
 * Esta clase es utilizada por UI y la lógica de negocio.
 *
 * @property id Identificador único del cosmético.
 * @property name Nombre visible del cosmético.
 * @property desc Descripción detallada del cosmético.
 * @property type Tipo de cosmético basado en [CosmeticType].
 * @property price Precio del cosmético en la moneda del juego.
 * @property assetRef Referencia al recurso visual.
 * @property theme JSON que define los colores (APP_THEME).
 * @property coleccion Nombre de la colección o set al que pertenece el cosmético.
 * @property owned Indica si el usuario ya posee este cosmético.
 * @property equipped Indica si el usuario tiene este cosmético equipado.
 */
data class Cosmetic(
    val id: Int,
    val name: String,
    val desc: String,
    val type: CosmeticType,
    val price: Int,
    val assetRef: String,
    val theme: String?,
    val rarity: String = "COMUN",
    val coleccion: String,
    val owned: Boolean,
    val equipped: Boolean
)


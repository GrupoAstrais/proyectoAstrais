package com.mm.astraisandroid.data.api

import com.mm.astraisandroid.data.models.Theme
import com.mm.astraisandroid.data.models.User
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/* Así es la estructura del JSON para los colores
{
  "primary": "#8B5CF6",
  "secondary": "#38BDF8 ",
  "tertiary": "#10B981",
  "background": "#0D1117",
  "backgroundAlt": "#11161D",
  "surface": "#1A1D2D",
  "text": "#F8FAFC",
  "error": "#F43F5E"
}
* */

/**
 * DTO utilizado para deserializar la configuración de colores del tema.
 * Representa la estructura exacta del objeto JSON que define la paleta visual de la aplicación.
 *
 * @property primary Color principal, usado para elementos clave como botones destacados.
 * @property secondary Color secundario, usado para elementos de soporte o acentos.
 * @property tertiary Color terciario, usado para detalles o variaciones adicionales.
 * @property background Color de fondo general de la aplicación.
 * @property backgroundAlt Color de fondo alternativo.
 * @property surface Color de las superficies superpuestas al fondo.
 * @property text Color principal del texto para asegurar buena legibilidad.
 * @property error Color utilizado para indicar estados de error o acciones destructivas.
 */
@Serializable
data class ThemeConfig(
    val primary: String,
    val secondary: String,
    val tertiary: String,
    val background: String,
    val backgroundAlt: String,
    val surface: String,
    val text: String,
    val error: String
)

/**
 * DTO que representa la respuesta del servidor al solicitar la información del perfil del usuario actual.
 *
 * @property id Identificador único del usuario.
 * @property name Nombre de usuario (nickname) visible en la app.
 * @property level Nivel actual alcanzado por el usuario.
 * @property currentXp Puntos de experiencia actuales en el nivel en curso.
 * @property totalXp Puntos de experiencia totales acumulados históricamente.
 * @property ludiones Moneda virtual del juego, utilizada para comprar en la tienda.
 * @property personalGid Identificador del grupo personal asociado al usuario.
 * @property equippedPetRef Referencia al asset de la mascota que el usuario tiene equipada.
 * @property equippedAvatarRef Referencia al asset del avatar que el usuario tiene equipado.
 * @property themeColors Tema de colores equipado actualmente.
 */
@Serializable
data class UserMeResponse(
    val id: Int,
    @SerialName("nombre") val name: String,
    @SerialName("nivel") val level: Int,
    @SerialName("xpActual") val currentXp: Int,
    @SerialName("xpTotal") val totalXp: Int,
    val ludiones: Int,
    val personalGid: Int?,
    val equippedPetRef: String?,
    val equippedAvatarRef: String? = null,
    val themeColors: String? = null
)

/**
 * Función para transformar el DTO ([ThemeConfig])
 * en el modelo de dominio ([Theme]) utilizado por la UI.
 *
 * @return Una instancia de [Theme] con los colores mapeados.
 */
fun ThemeConfig.toDomain(): Theme {
    return Theme(primary, secondary, tertiary, background, backgroundAlt, surface, text, error)
}

/**
 * Función para transformar el DTO completo de respuesta del usuario ([UserMeResponse])
 * al modelo de dominio ([User]).
 *
 * Si el string viene vacío, mal formado o le faltan propiedades críticas, la excepción será capturada y el usuario
 * cargará con un `theme` igual a `null`.
 *
 * @return Una instancia de [User].
 */
fun UserMeResponse.toDomain(): User {
    val parsedTheme = try {
        this.themeColors?.let {
            Json { ignoreUnknownKeys = true }.decodeFromString<ThemeConfig>(it).toDomain()
        }
    } catch (e: Exception) {
        null
    }

    return User(
        id = this.id,
        name = this.name,
        level = this.level,
        currentXp = this.currentXp,
        totalXp = this.totalXp,
        ludiones = this.ludiones,
        personalGid = this.personalGid,
        equippedPetRef = this.equippedPetRef,
        equippedAvatarRef = this.equippedAvatarRef,
        theme = parsedTheme
    )
}

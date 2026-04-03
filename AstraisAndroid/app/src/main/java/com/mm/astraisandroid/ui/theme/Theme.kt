package com.mm.astraisandroid.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.core.graphics.toColorInt
import com.mm.astraisandroid.data.api.ThemeConfig
import kotlinx.serialization.json.Json

private val DefaultDarkColorScheme = darkColorScheme(
    primary = IndigoGalaxia,
    secondary = MoradoNebulosa,
    tertiary = TurquesaCosmico,
    background = BgDark,
    surface = SurfaceDark,
    onPrimary = Color.White,
    onBackground = Gray50,
    onSurface = Gray50,
    error = StateError
)

private val LightColorScheme = lightColorScheme(
    primary = IndigoMedio,
    secondary = MoradoMedio,
    tertiary = VerdeMedio,
    background = Gray50,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Gray900,
    onBackground = Gray900,
    onSurface = Gray900,
    error = StateError
)

private fun getContrastColor(backgroundColor: Color): Color {
    return if (backgroundColor.luminance() > 0.45f) Color(0xFF12121A) else Color.White
}

private fun parseHexColor(hexString: String, fallback: Color): Color {
    return try {
        Color(hexString.toColorInt())
    } catch (e: Exception) {
        fallback
    }
}

@Composable
fun AstraisandroidTheme(
    themeJson: String? = null,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (!themeJson.isNullOrBlank()) {
        try {
            val config = Json { ignoreUnknownKeys = true }.decodeFromString<ThemeConfig>(themeJson)
            val textColor = parseHexColor(config.text, Gray50)

            val prim = parseHexColor(config.primary, IndigoGalaxia)
            val sec = parseHexColor(config.secondary, MoradoNebulosa)
            val tert = parseHexColor(config.tertiary, TurquesaCosmico)
            val err = parseHexColor(config.error, StateError)

            darkColorScheme(
                primary = prim,
                onPrimary = getContrastColor(prim),
                secondary = sec,
                onSecondary = getContrastColor(sec),
                tertiary = tert,
                onTertiary = getContrastColor(tert),
                background = parseHexColor(config.background, BgDark),
                surfaceVariant = parseHexColor(config.backgroundAlt, BgDark),
                surface = parseHexColor(config.surface, SurfaceDark),
                error = err,
                onError = getContrastColor(err),
                onBackground = textColor,
                onSurface = textColor
            )
        } catch (e: Exception) {
            DefaultDarkColorScheme
        }
    } else {
        DefaultDarkColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
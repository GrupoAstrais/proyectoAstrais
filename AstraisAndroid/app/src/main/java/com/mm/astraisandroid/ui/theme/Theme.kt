package com.mm.astraisandroid.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.core.graphics.toColorInt
import com.mm.astraisandroid.data.models.Theme

private val DefaultDarkColorScheme = darkColorScheme(
    primary = Primary,
    secondary = Secondary,
    tertiary = Tertiary,
    background = Background,
    surface = Surface,
    onPrimary = Color.White,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    surfaceVariant = BackgroundAlt,
    onSurfaceVariant = Gray300,
    error = ErrorCustom
)

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    secondary = Secondary,
    tertiary = Tertiary,
    background = Color.White,
    surface = Color(0xFFF1F5F9),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Gray900,
    onBackground = Gray900,
    onSurface = Gray900,
    surfaceVariant = Gray300,
    onSurfaceVariant = Gray700,
    error = ErrorCustom
)

private fun getContrastColor(backgroundColor: Color): Color {
    return if (backgroundColor.luminance() > 0.45f) Color(0xFF12121A) else Color.White
}

private fun parseHexColor(hexString: String, fallback: Color): Color {
    return try {
        Color(hexString.trim().toColorInt())
    } catch (e: Exception) {
        fallback
    }
}

@Composable
fun AstraisandroidTheme(
    userTheme: Theme? = null,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (userTheme != null) {
        val textColor = parseHexColor(userTheme.text, if (darkTheme) TextPrimary else Gray900)
        val prim = parseHexColor(userTheme.primary, Primary)
        val sec = parseHexColor(userTheme.secondary, Secondary)
        val tert = parseHexColor(userTheme.tertiary, Tertiary)
        val err = parseHexColor(userTheme.error, ErrorCustom)
        val bg = parseHexColor(userTheme.background, if (darkTheme) Background else Color.White)
        val bgAlt = parseHexColor(userTheme.backgroundAlt, if (darkTheme) BackgroundAlt else Gray300)
        val surf = parseHexColor(userTheme.surface, if (darkTheme) Surface else Color.White)

        if (darkTheme) {
            darkColorScheme(
                primary = prim,
                onPrimary = getContrastColor(prim),
                secondary = sec,
                onSecondary = getContrastColor(sec),
                tertiary = tert,
                onTertiary = getContrastColor(tert),
                background = bg,
                surfaceVariant = bgAlt,
                surface = surf,
                error = err,
                onError = getContrastColor(err),
                onBackground = textColor,
                onSurface = textColor
            )
        } else {
            lightColorScheme(
                primary = prim,
                onPrimary = getContrastColor(prim),
                secondary = sec,
                onSecondary = getContrastColor(sec),
                tertiary = tert,
                onTertiary = getContrastColor(tert),
                background = bg,
                surfaceVariant = bgAlt,
                surface = surf,
                error = err,
                onError = getContrastColor(err),
                onBackground = textColor,
                onSurface = textColor
            )
        }
    } else if (darkTheme) {
        DefaultDarkColorScheme
    } else {
        LightColorScheme
    }

    CompositionLocalProvider(LocalAstraisSpacing provides AstraisSpacing()) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = AstraisShapes,
            content = content
        )
    }
}
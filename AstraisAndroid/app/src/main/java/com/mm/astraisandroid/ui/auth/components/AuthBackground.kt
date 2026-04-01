package com.mm.astraisandroid.ui.auth.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

// boxscope es basicamente un contexto de box para poder usar los modificadores
//@Composable
//fun AuthBackground(content: @Composable BoxScope.() -> Unit) {
//    // crea un degradado para pruebas de arriba hacia abajo con los colores que le pases
//    val gradientBrush = Brush.verticalGradient(
//        colors = listOf(
//            Color(0xFF2D0A3F),
//            Color(0xFF8B1A6B),
//            Color(0xFFB5216A),
//            Color(0xFF6A0F5A),
//            Color(0xFF1A0A2E)
//        )
//    )
//    // ocupa toda la pantalla y pinta el degradado de fondo. Lo que este en content aparece encima
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(brush = gradientBrush),
//        content = content
//    )
//}

@Composable
fun AuthBackground(content: @Composable BoxScope.() -> Unit) {

    // transición infinita
    val infiniteTransition = rememberInfiniteTransition(label = "bg")

    // Anima un Float de 0f a 1f en bucle
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis =8000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse // ida y vuelta
        ),
        label = "progress"
    )

    // Dos sets de colores entre los que vas a interpolar
    val colorsA = listOf(
        Color(0xFF2D0A3F), Color(0xFF8B1A6B),
        Color(0xFFB5216A), Color(0xFF1A0A2E)
    )
    val colorsB = listOf(
        Color(0xFF0A1A3F), Color(0xFF1A3A8B),
        Color(0xFF213DB5), Color(0xFF0A0E2E)
    )

    // interpola cada color individualmente según progress
    val animatedColors = colorsA.zip(colorsB) { a, b -> lerp(a, b, progress) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind { // si podemos background peta
                drawRect(
                    brush = Brush.verticalGradient(animatedColors)
                )
            },
        content = content
    )
}

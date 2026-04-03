package com.mm.astraisandroid.ui.auth.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AuthBackground(content: @Composable BoxScope.() -> Unit) {
    val backgroundColor = MaterialTheme.colorScheme.background

    val color1 = MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)
    val color2 = MaterialTheme.colorScheme.secondary.copy(alpha = 0.18f)
    val color3 = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.18f)
    val color4 = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.12f)

    val infiniteTransition = rememberInfiniteTransition(label = "mesh")

    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(18000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "progress"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Convertimos el progreso a radianes (0 a 2pi)
            val angle = progress * 2f * PI.toFloat()

            // Fondo base sólido
            drawRect(color = backgroundColor)

            // Los multiplicadores dentro del sin/cos DEBEN SER ENTEROS por favor que peta
            // para que el inicio y el final coincidan perfectamente

            // Primario
            val p1 = Offset(
                x = w * 0.5f + cos(angle) * (w * 0.3f),
                y = h * 0.5f + sin(angle) * (h * 0.2f)
            )
            drawCircle(
                brush = Brush.radialGradient(listOf(color1, Color.Transparent), center = p1, radius = w * 0.9f),
                center = p1, radius = w * 0.9f
            )

            //  Secundario (Doble de velocidad, giro opuesto)
            val p2 = Offset(
                x = w * 0.5f + sin(-angle * 2f) * (w * 0.4f),
                y = h * 0.5f + cos(angle * 1f) * (h * 0.3f)
            )
            drawCircle(
                brush = Brush.radialGradient(listOf(color2, Color.Transparent), center = p2, radius = w * 0.8f),
                center = p2, radius = w * 0.8f
            )

            // Terciario (Movimiento lento en 8)
            val p3 = Offset(
                x = w * 0.7f + cos(angle) * (w * 0.2f),
                y = h * 0.8f + sin(angle * 2f) * (h * 0.1f)
            )
            drawCircle(
                brush = Brush.radialGradient(listOf(color3, Color.Transparent), center = p3, radius = w * 0.7f),
                center = p3, radius = w * 0.7f
            )

            // Contraste
            val p4 = Offset(
                x = w * 0.3f + sin(angle) * (w * 0.3f),
                y = h * 0.2f + cos(angle) * (h * 0.15f)
            )
            drawCircle(
                brush = Brush.radialGradient(listOf(color4, Color.Transparent), center = p4, radius = w * 0.6f),
                center = p4, radius = w * 0.6f
            )
        }

        content()
    }
}
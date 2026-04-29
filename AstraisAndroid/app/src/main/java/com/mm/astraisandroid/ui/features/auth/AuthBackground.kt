package com.mm.astraisandroid.ui.features.auth

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

    val color1 = MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)
    val color2 = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.38f)
    val color3 = MaterialTheme.colorScheme.secondary.copy(alpha = 0.32f)
    val color4 = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.28f)
    val color5 = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.22f)

    val infiniteTransition = rememberInfiniteTransition(label = "mesh")

    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(25000, easing = LinearEasing),
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

            val p1 = Offset(
                x = w * 0.3f + cos(angle * 0.7f) * (w * 0.4f),
                y = h * 0.4f + sin(angle * 0.5f) * (h * 0.35f)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(color1, color1.copy(alpha = 0.2f), Color.Transparent),
                    center = p1,
                    radius = w * 1.2f
                ),
                center = p1, radius = w * 1.2f
            )

            // Rich tertiary orb - counter movement
            val p2 = Offset(
                x = w * 0.7f + sin(-angle * 0.9f) * (w * 0.35f),
                y = h * 0.6f + cos(angle * 0.8f) * (h * 0.4f)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(color2, color2.copy(alpha = 0.15f), Color.Transparent),
                    center = p2,
                    radius = w * 1.0f
                ),
                center = p2, radius = w * 1.0f
            )

            // Secondary accent - figure-8 movement
            val p3 = Offset(
                x = w * 0.5f + cos(angle * 1.3f) * (w * 0.45f),
                y = h * 0.3f + sin(angle * 2f) * (h * 0.25f)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(color3, color3.copy(alpha = 0.1f), Color.Transparent),
                    center = p3,
                    radius = w * 0.85f
                ),
                center = p3, radius = w * 0.85f
            )

            // Primary container - bottom accent
            val p4 = Offset(
                x = w * 0.2f + sin(angle * 0.6f) * (w * 0.3f),
                y = h * 0.8f + cos(angle * 0.4f) * (h * 0.2f)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(color4, color4.copy(alpha = 0.15f), Color.Transparent),
                    center = p4,
                    radius = w * 0.9f
                ),
                center = p4, radius = w * 0.9f
            )

            // Tertiary container - top right accent
            val p5 = Offset(
                x = w * 0.8f + cos(angle * 1.1f) * (w * 0.25f),
                y = h * 0.2f + sin(angle * 0.7f) * (h * 0.3f)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(color5, color5.copy(alpha = 0.1f), Color.Transparent),
                    center = p5,
                    radius = w * 0.75f
                ),
                center = p5, radius = w * 0.75f
            )
        }

        content()
    }
}
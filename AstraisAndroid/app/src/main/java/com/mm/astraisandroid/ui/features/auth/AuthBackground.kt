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

@Composable
fun AuthBackground(content: @Composable BoxScope.() -> Unit) {
    val bgBase = MaterialTheme.colorScheme.background
    val bgAlt = MaterialTheme.colorScheme.surfaceVariant

    val primaryGlow = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)

    val infiniteTransition = rememberInfiniteTransition(label = "nebula_anim")

    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_movement"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(bgBase, bgAlt),
                    startY = 0f,
                    endY = h
                )
            )

            val glowX = w * 0.2f + (w * 0.6f * progress)
            val glowY = h * 0.3f + (h * 0.4f * progress)
            val glowRadius = w * 0.9f

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(primaryGlow, Color.Transparent),
                    center = Offset(glowX, glowY),
                    radius = glowRadius
                ),
                center = Offset(glowX, glowY),
                radius = glowRadius
            )
        }

        content()
    }
}
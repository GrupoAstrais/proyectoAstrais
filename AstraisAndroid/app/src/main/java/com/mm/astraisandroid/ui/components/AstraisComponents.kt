package com.mm.astraisandroid.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

object Glassmorphism {
    const val BG_PRIMARY = 0.04f
    const val BG_SECONDARY = 0.02f
    const val BG_TERTIARY = 0.08f
    const val BG_PREMIUM = 0.12f
    const val BORDER_PRIMARY = 0.15f
    const val BORDER_SECONDARY = 0.08f
    const val TEXT_PRIMARY = 0.95f
    const val TEXT_SECONDARY = 0.70f
    const val TEXT_TERTIARY = 0.45f
    const val ICON_ALPHA = 0.85f
}

enum class AstraisButtonStyle { Primary, Secondary, Destructive }

@Composable
fun AstraisButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    style: AstraisButtonStyle = AstraisButtonStyle.Primary
) {
    val colors = when (style) {
        AstraisButtonStyle.Primary -> ButtonDefaults.buttonColors()
        AstraisButtonStyle.Secondary -> ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
        AstraisButtonStyle.Destructive -> ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.error,
            contentColor = MaterialTheme.colorScheme.onError
        )
    }
    Button(onClick = onClick, modifier = modifier, enabled = enabled && !loading, colors = colors) {
        if (loading) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.padding(vertical = 2.dp)
            )
        } else {
            Text(text = text, style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
fun AstraisOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Text(text = text, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
fun AstraisCard(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        content()
    }
}

@Composable
fun AstraisTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier
    )
}

@Composable
fun AstraisStateView(
    isLoading: Boolean,
    error: String?,
    isEmpty: Boolean,
    emptyText: String,
    content: @Composable () -> Unit
) {
    when {
        isLoading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        error != null -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(error, color = MaterialTheme.colorScheme.error)
        }
        isEmpty -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(emptyText, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        else -> content()
    }
}

@Composable
fun AstraisConfirmDialog(
    title: String,
    body: String,
    confirmText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(body) },
        confirmButton = { TextButton(onClick = onConfirm) { Text(confirmText) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
fun AstraisSectionHeader(title: String, trailing: @Composable (() -> Unit)? = null) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground)
        trailing?.invoke()
    }
}

@Composable
fun AstraisScreenHeader(
    title: String,
    modifier: Modifier = Modifier,
    onBackClick: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (onBackClick != null) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = MaterialTheme.colorScheme.onBackground.copy(alpha = Glassmorphism.ICON_ALPHA)
                    )
                }
            }
            Text(
                text = title,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = Glassmorphism.TEXT_PRIMARY),
                fontFamily = FontFamily.Monospace,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }
        trailing?.invoke()
    }
}

@Composable
fun AstraisGlassSurface(
    modifier: Modifier = Modifier,
    shape: androidx.compose.ui.graphics.Shape = MaterialTheme.shapes.extraLarge,
    backgroundAlpha: Float = Glassmorphism.BG_PRIMARY,
    borderAlpha: Float = Glassmorphism.BORDER_PRIMARY,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val baseModifier = modifier
        .clip(shape)
        .background(
            MaterialTheme.colorScheme.onBackground.copy(alpha = backgroundAlpha)
        )
        .border(
            width = 0.5.dp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = borderAlpha),
            shape = shape
        )

    Box(
        modifier = if (onClick != null) {
            baseModifier.clickable { onClick() }
        } else baseModifier,
        content = content
    )
}

@Composable
fun AstraisGlassCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    AstraisGlassSurface(
        modifier = modifier,
        shape = MaterialTheme.shapes.extraLarge,
        onClick = onClick
    ) {
        Box(modifier = Modifier.padding(20.dp)) {
            content()
        }
    }
}

@Composable
fun AstraisGlassChip(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    AstraisGlassSurface(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        backgroundAlpha = Glassmorphism.BG_SECONDARY,
        borderAlpha = Glassmorphism.BORDER_SECONDARY,
        onClick = onClick,
        content = content
    )
}


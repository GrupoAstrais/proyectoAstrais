package com.mm.astraisandroid.ui.features.groups

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private const val ROLE_USER = 0
private const val ROLE_MOD = 1
private const val ROLE_OWNER = 2

@Composable
fun GroupStateView(
    isLoading: Boolean,
    isEmpty: Boolean,
    emptyText: String,
    errorText: String?,
    onRetry: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    when {
        isLoading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color.White)
        }
        errorText != null -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = errorText,
                color = Color(0xFFFF6B6B),
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp
            )
        }
        isEmpty -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = emptyText,
                color = Color.White.copy(alpha = 0.6f),
                fontFamily = FontFamily.Monospace
            )
        }
        else -> content()
    }
}

@Composable
fun RoleChip(role: Int) {
    val label = when (role) {
        ROLE_OWNER -> "Owner"
        ROLE_MOD -> "Mod"
        else -> "Miembro"
    }
    val bg = when (role) {
        ROLE_OWNER -> Color(0xFFE8B94A)
        ROLE_MOD -> Color(0xFF8CD3FF)
        else -> Color.White.copy(alpha = 0.2f)
    }
    val fg = if (role == ROLE_USER) Color.White else Color.Black

    Surface(
        shape = RoundedCornerShape(999.dp),
        color = bg,
        modifier = Modifier.semantics { contentDescription = "Rol: $label" }
    ) {
        Text(
            text = label,
            color = fg,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun ConfirmActionDialog(
    title: String,
    body: String,
    confirmText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontFamily = FontFamily.Monospace) },
        text = { Text(body, fontFamily = FontFamily.Monospace) },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text(confirmText, fontFamily = FontFamily.Monospace) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar", fontFamily = FontFamily.Monospace) }
        }
    )
}

@Composable
fun GroupSectionHeader(title: String, trailing: @Composable (() -> Unit)? = null) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, color = Color.White, fontFamily = FontFamily.Monospace)
        trailing?.invoke()
    }
}

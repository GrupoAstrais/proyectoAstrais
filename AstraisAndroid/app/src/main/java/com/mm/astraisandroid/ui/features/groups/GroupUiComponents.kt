package com.mm.astraisandroid.ui.features.groups


import com.mm.astraisandroid.R
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.res.stringResource
import com.mm.astraisandroid.ui.theme.Gray300
import com.mm.astraisandroid.ui.theme.Primary
import com.mm.astraisandroid.ui.theme.Surface as SurfaceColor

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
            CircularProgressIndicator(color = Primary)
        }
        errorText != null -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = errorText,
                color = Color(0xFFFF6B6B),
                fontSize = 12.sp
            )
        }
        isEmpty -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = emptyText,
                color = Color.White.copy(alpha = 0.6f),
            )
        }
        else -> content()
    }
}

@Composable
fun RoleChip(role: Int) {
    val label = when (role) {
        ROLE_OWNER -> stringResource(R.string.group_role_chip_owner)
        ROLE_MOD -> stringResource(R.string.group_role_chip_mod)
        else -> stringResource(R.string.group_role_chip_member)
    }

    val bgColor: Color
    val borderColor: Color
    val textColor: Color

    when (role) {
        ROLE_OWNER -> {
            bgColor = Color(0xFFE8B94A).copy(alpha = 0.15f)
            borderColor = Color(0xFFE8B94A).copy(alpha = 0.4f)
            textColor = Color(0xFFE8B94A)
        }
        ROLE_MOD -> {
            bgColor = Color(0xFF8CD3FF).copy(alpha = 0.15f)
            borderColor = Color(0xFF8CD3FF).copy(alpha = 0.4f)
            textColor = Color(0xFF8CD3FF)
        }
        else -> {
            bgColor = Color.White.copy(alpha = 0.08f)
            borderColor = Color.White.copy(alpha = 0.2f)
            textColor = Gray300
        }
    }

    val roleCd = stringResource(R.string.cd_role_label, label)
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = Color.Transparent,
        modifier = Modifier.semantics { contentDescription = roleCd }
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(999.dp))
                .background(bgColor)
                .border(0.5.dp, borderColor, RoundedCornerShape(999.dp))
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(
                text = label,
                color = textColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
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
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(28.dp))
                .shadow(
                    elevation = 16.dp,
                    shape = RoundedCornerShape(28.dp),
                    ambientColor = Color.Black.copy(alpha = 0.4f),
                    spotColor = Primary.copy(alpha = 0.1f)
                )
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            SurfaceColor.copy(alpha = 0.92f),
                            SurfaceColor.copy(alpha = 0.85f)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.18f),
                            Color.White.copy(alpha = 0.06f)
                        )
                    ),
                    shape = RoundedCornerShape(28.dp)
                )
                .padding(24.dp)
        ) {
            androidx.compose.foundation.layout.Column(
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = body,
                    color = Gray300,
                    fontSize = 14.sp
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.06f))
                            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                            .clickable { onDismiss() }
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(stringResource(R.string.dialog_cancel), color = Gray300, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Primary)
                            .shadow(
                                elevation = 8.dp,
                                shape = RoundedCornerShape(12.dp),
                                ambientColor = Primary.copy(alpha = 0.3f),
                                spotColor = Color.White.copy(alpha = 0.1f)
                            )
                            .clickable { onConfirm() }
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(confirmText, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun GroupSectionHeader(title: String, trailing: @Composable (() -> Unit)? = null) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, color = Color.White)
        trailing?.invoke()
    }
}

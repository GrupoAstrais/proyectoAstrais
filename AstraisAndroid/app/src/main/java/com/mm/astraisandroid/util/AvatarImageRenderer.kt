package com.mm.astraisandroid.util

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.mm.astraisandroid.data.api.BASE_URL

@Composable
fun AvatarImageRenderer(
    assetRef: String?,
    initial: String,
    size: Dp = 80.dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.45f),
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.2f)
                    )
                )
            )
            .border(2.dp, Color.White.copy(alpha = 0.25f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (!assetRef.isNullOrBlank()) {
            SubcomposeAsyncImage(
                model = "$BASE_URL/assets/avatar/$assetRef",
                contentDescription = "Avatar",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                loading = {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(size / 3)
                    )
                },
                error = {
                    AvatarInitial(initial = initial, size = size)
                }
            )
        } else {
            AvatarInitial(initial = initial, size = size)
        }
    }
}

@Composable
private fun AvatarInitial(initial: String, size: Dp) {
    Text(
        text = initial,
        fontSize = (size.value / 3).sp,
        fontWeight = FontWeight.Black,
        color = MaterialTheme.colorScheme.onBackground,
        fontFamily = FontFamily.Monospace
    )
}

package com.mm.astraisandroid.ui.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val ColorDinamico   = Color(0xFF7EB8F7)
private val ColorPersona    = Color(0xFF6EF77E)
private val ColorLogros     = Color(0xFFD07EF7)
private val ColorDark       = Color.White.copy(alpha = 0.06f)


@Composable
fun HomeTab(username: String = "Astrais",
            onNavigateToProfile: () -> Unit = {}) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header
        HomeHeader(username = username, onNavigateToProfile)

        // Banner card
        BannerCard()

        // Bento grid
        BentoGrid(modifier = Modifier.weight(1f))

        // Barra de notificaciones
        NotificationsBar(count = 8)
    }
}


@Composable
fun HomeHeader(username: String,  onProfileClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Hi, $username",
            color = Color.White,
            fontSize = 26.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp
        )
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.15f))
                .border(1.5.dp, Color.White.copy(alpha = 0.4f), CircleShape)
                .clickable { onProfileClick() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = username.first().uppercaseChar().toString(),
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
fun BannerCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(ColorDark)
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
            .clickable { },
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = "",
            color = Color.White.copy(alpha = 0.3f),
            fontSize = 13.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(horizontal = 20.dp)
        )
    }
}

@Composable
fun BentoGrid(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {

        BentoCell(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            color = ColorDark,
            borderAlpha = 0.1f,
            onClick = {}
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Tareas",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )

                repeat(3) { index ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.White.copy(alpha = 0.07f))
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Tarea ${index + 1}",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = "3 pendientes",
                    color = Color.White.copy(alpha = 0.3f),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            BentoCell(
                modifier = Modifier.fillMaxWidth().weight(1f),
                backgroundIcon = Icons.Default.Bolt,
                onClick = {}
            ) {
                Text(
                    text = "Dinamico",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )
            }

            BentoCell(
                modifier = Modifier.fillMaxWidth().weight(1f),
                backgroundIcon = Icons.Default.Person,
                onClick = {}
            ) {
                Text(
                    text = "Persona\nTienda",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    lineHeight = 22.sp
                )
            }

            BentoCell(
                modifier = Modifier.fillMaxWidth().weight(1f),
                backgroundIcon = Icons.Default.Star,
                onClick = {}
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "Logros",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Composable
fun BentoCell(
    modifier: Modifier = Modifier,
    color: Color = ColorDark,
    borderAlpha: Float = 0.1f,
    backgroundIcon: ImageVector? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit = {}
) {
    val shape = RoundedCornerShape(18.dp)
    Box(
        modifier = modifier
            .clip(shape)
            .background(color)
            .border(1.dp, Color.White.copy(alpha = borderAlpha), shape)
            .then(
                if (onClick != null) Modifier.clickable { onClick() } else Modifier
            )
    ) {

        if (backgroundIcon != null) {
            Icon(
                imageVector = backgroundIcon,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.07f),
                modifier = Modifier
                    .size(90.dp)
                    .align(Alignment.BottomEnd)
                    .offset(x = 16.dp, y = 16.dp)
            )
        }

        Box(
            modifier = Modifier
                .matchParentSize()
                .padding(16.dp),
            content = content
        )
    }
}

@Composable
fun NotificationsBar(count: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(50.dp))
            .background(Color.White.copy(alpha = 0.08f))
            .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(50.dp))
            .clickable { }
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Notificaciones ($count)",
            color = Color.White,
            fontSize = 13.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.SemiBold
        )
        Icon(
            imageVector = Icons.Default.ArrowForward,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(18.dp)
        )
    }
}
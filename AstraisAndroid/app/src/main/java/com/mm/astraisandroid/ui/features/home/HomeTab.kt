package com.mm.astraisandroid.ui.features.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mm.astraisandroid.util.LottiePetRenderer
import com.mm.astraisandroid.data.api.UserMeResponse
import com.mm.astraisandroid.data.models.User
import com.mm.astraisandroid.ui.features.tasks.TaskViewModel

private val ColorDinamico   = Color(0xFF7EB8F7)
private val ColorPersona    = Color(0xFF6EF77E)
private val ColorLogros     = Color(0xFFD07EF7)
private val ColorDark       = Color.White.copy(alpha = 0.06f)


@Composable
fun HomeTab(
    user: User?,
    taskViewModel: TaskViewModel = hiltViewModel(),
    onNavigateToProfile: () -> Unit = {},
    onNavigateToTasks: () -> Unit = {},
    onNavigateToStore: () -> Unit = {},
    onNavigateToInventory: () -> Unit = {},
    onNavigateToGroups: () -> Unit = {}
) {
    val taskState by taskViewModel.state.collectAsStateWithLifecycle()
    val pendingTasks = taskState.tasks.filter { !it.isCompleted }
    val topTasks = pendingTasks.take(3)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val isGuest = com.mm.astraisandroid.data.preferences.SessionManager.isGuest()

        HomeHeader(username = if (isGuest) "Invitado" else (user?.name ?: "Viajero"), onNavigateToProfile)

        if (isGuest) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(ColorDark)
                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.size(64.dp)
                    )
                    Text(
                        text = "Modo Invitado",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "Tus tareas se guardan de forma local. Ve al perfil para registrarte y sincronizar tu progreso.",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 14.sp,
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            BannerCard(user = user)

            BentoGrid(
                user = user,
                topTasks = topTasks,
                totalPending = pendingTasks.size,
                modifier = Modifier.weight(1f),
                onTasksClick = onNavigateToTasks,
                onInventoryClick = onNavigateToInventory,
                onStoreClick = onNavigateToStore,
                onGroupsClick = onNavigateToGroups
            )

            NotificationsBar(count = 8)
        }
    }
}


@Composable
fun HomeHeader(username: String, onProfileClick: () -> Unit) {
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
                text = username.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
fun BannerCard(user: User?) {
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
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Text(
                text = "Nivel ${user?.level ?: 0}",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "${user?.currentXp ?: 0} / ${((user?.level ?: 0) + 1) * 100} XP",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(80.dp)
                .align(Alignment.CenterEnd)
                .padding(end = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            if (user?.equippedPetRef != null) {
                LottiePetRenderer(assetRef = user.equippedPetRef)
            } else {
                Text(
                    text = "SIN MASCOTA",
                    color = Color.White.copy(alpha = 0.2f),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun BentoGrid(
    user: User?,
    topTasks: List<com.mm.astraisandroid.ui.features.tasks.TaskUIModel>,
    totalPending: Int,
    modifier: Modifier = Modifier,
    onTasksClick: () -> Unit,
    onInventoryClick: () -> Unit,
    onStoreClick: () -> Unit,
    onGroupsClick: () -> Unit,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        BentoCell(
            modifier = Modifier.weight(1f).fillMaxHeight(),
            onClick = onTasksClick
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

                topTasks.forEach { tarea ->
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
                            text = tarea.title,
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 1
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
                    text = if (totalPending > 0) "$totalPending pendientes" else "¡Todo al día!",
                    color = Color.White.copy(alpha = 0.3f),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Column(
            modifier = Modifier.weight(1f).fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            BentoCell(
                modifier = Modifier.fillMaxWidth().weight(1.4f),
                onClick = onInventoryClick
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = "Inventario",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )

                    Box(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        if (user?.equippedPetRef != null) {
                            LottiePetRenderer(
                                assetRef = user.equippedPetRef,
                                modifier = Modifier.size(90.dp)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Inventory,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.07f),
                                modifier = Modifier.size(60.dp)
                            )
                        }
                    }
                }
            }

            BentoCell(
                modifier = Modifier.fillMaxWidth().weight(1f),
                backgroundIcon = Icons.Default.ShoppingCart,
                onClick = onStoreClick
            ) {
                Text(
                    text = "Tienda",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )
            }

            BentoCell(
                modifier = Modifier.fillMaxWidth().weight(1f),
                backgroundIcon = Icons.Default.Star,
                onClick = onGroupsClick
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
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(18.dp)
        )
    }
}
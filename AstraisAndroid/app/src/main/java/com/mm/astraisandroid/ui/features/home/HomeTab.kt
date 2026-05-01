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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mm.astraisandroid.util.LottiePetRenderer
import com.mm.astraisandroid.data.api.UserMeResponse
import com.mm.astraisandroid.data.models.User
import com.mm.astraisandroid.ui.features.tasks.TaskViewModel
import com.mm.astraisandroid.ui.components.AstraisGlassSurface
import com.mm.astraisandroid.ui.components.AstraisGlassChip
import com.mm.astraisandroid.ui.components.Glassmorphism

@Composable
fun HomeTab(
    user: User?,
    taskViewModel: TaskViewModel = hiltViewModel(),
    isGuest: Boolean = false,
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

        WelcomeHeader(
            username = if (isGuest) "Invitado" else (user?.name ?: "Viajero"),
            onProfileClick = onNavigateToProfile
        )

        if (isGuest) {
            GuestWelcomeCard(onRegisterClick = onNavigateToProfile)
        } else {
            BannerCard(user = user)
        }

        BentoGrid(
            user = user,
            topTasks = topTasks,
            totalPending = pendingTasks.size,
            modifier = Modifier.weight(1f),
            onTasksClick = onNavigateToTasks,
            onInventoryClick = { if (!isGuest) onNavigateToInventory() },
            onStoreClick = { if (!isGuest) onNavigateToStore() },
            onGroupsClick = { if (!isGuest) onNavigateToGroups() },
            isGuest = isGuest
        )

        if (!isGuest) {
            NotificationsBar(count = 8)
        }
    }
}

@Composable
fun WelcomeHeader(username: String, onProfileClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = "Hi,",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = Glassmorphism.TEXT_SECONDARY),
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = username,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = Glassmorphism.TEXT_PRIMARY),
                fontFamily = FontFamily.Monospace,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        AstraisGlassSurface(
            modifier = Modifier.size(52.dp),
            shape = CircleShape,
            backgroundAlpha = Glassmorphism.BG_PREMIUM,
            borderAlpha = Glassmorphism.BORDER_PRIMARY,
            onClick = onProfileClick
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = username.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = Glassmorphism.TEXT_PRIMARY),
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
fun BannerCard(user: User?) {
    AstraisGlassSurface(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        shape = MaterialTheme.shapes.extraLarge,
        backgroundAlpha = Glassmorphism.BG_TERTIARY,
        onClick = {}
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Text(
                    text = "Nivel ${user?.level ?: 0}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = Glassmorphism.TEXT_PRIMARY),
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "${user?.currentXp ?: 0} / ${((user?.level ?: 0) + 1) * 100} XP",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = Glassmorphism.TEXT_SECONDARY),
                    fontFamily = FontFamily.Monospace
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(90.dp)
                    .padding(end = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                if (user?.equippedPetRef != null) {
                    LottiePetRenderer(assetRef = user.equippedPetRef, modifier = Modifier.size(70.dp))
                } else {
                    Text(
                        text = "SIN MASCOTA",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = Glassmorphism.TEXT_TERTIARY),
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun GuestWelcomeCard(onRegisterClick: () -> Unit) {
    AstraisGlassSurface(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp),
        shape = MaterialTheme.shapes.extraLarge,
        backgroundAlpha = Glassmorphism.BG_PREMIUM,
        borderAlpha = Glassmorphism.BORDER_PRIMARY,
        onClick = onRegisterClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AstraisGlassSurface(
                modifier = Modifier.size(56.dp),
                shape = CircleShape,
                backgroundAlpha = Glassmorphism.BG_PREMIUM
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            Column {
                Text(
                    text = "Bienvenido a Astrais",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = Glassmorphism.TEXT_PRIMARY),
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Toca para registrarte y sincronizar",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = Glassmorphism.TEXT_SECONDARY),
                    fontFamily = FontFamily.Monospace
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
    isGuest: Boolean = false
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AstraisGlassSurface(
            modifier = Modifier.weight(1f).fillMaxHeight(),
            shape = MaterialTheme.shapes.large,
            onClick = onTasksClick
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Tareas",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontFamily = FontFamily.Monospace
                )

                topTasks.forEach { tarea ->
                    AstraisGlassChip(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = null
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = tarea.title,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = Glassmorphism.TEXT_PRIMARY),
                                fontFamily = FontFamily.Monospace,
                                maxLines = 1
                            )
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .clip(MaterialTheme.shapes.extraSmall)
                                    .border(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = Glassmorphism.ICON_ALPHA), MaterialTheme.shapes.extraSmall)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = if (totalPending > 0) "$totalPending pendientes" else "¡Todo al día!",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = Glassmorphism.TEXT_TERTIARY),
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Column(
            modifier = Modifier.weight(1f).fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AstraisGlassSurface(
                modifier = Modifier.fillMaxWidth().weight(1.4f),
                shape = MaterialTheme.shapes.large,
                onClick = if (!isGuest) onInventoryClick else null
            ) {
                Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    Text(
                        text = "Inventario",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground,
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
                                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = Glassmorphism.BG_SECONDARY),
                                modifier = Modifier.size(60.dp)
                            )
                        }
                    }
                }
            }

            BentoMiniCell(
                modifier = Modifier.fillMaxWidth().weight(1f),
                backgroundIcon = Icons.Default.ShoppingCart,
                onClick = onStoreClick,
                isLocked = isGuest,
                text = "Tienda"
            )

            BentoMiniCell(
                modifier = Modifier.fillMaxWidth().weight(1f),
                backgroundIcon = Icons.Default.Star,
                onClick = onGroupsClick,
                isLocked = isGuest,
                text = "Logros"
            )
        }
    }
}

@Composable
fun BentoMiniCell(
    modifier: Modifier = Modifier,
    backgroundIcon: ImageVector? = null,
    onClick: (() -> Unit)? = null,
    isLocked: Boolean = false,
    text: String = ""
) {
    AstraisGlassSurface(
        modifier = modifier,
        shape = MaterialTheme.shapes.extraLarge,
        backgroundAlpha = if (isLocked) Glassmorphism.BG_SECONDARY else Glassmorphism.BG_PRIMARY,
        onClick = if (!isLocked) onClick else null
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(20.dp)) {
            if (backgroundIcon != null) {
                Icon(
                    imageVector = backgroundIcon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f),
                    modifier = Modifier
                        .size(100.dp)
                        .align(Alignment.BottomEnd)
                        .offset(x = 20.dp, y = 20.dp)
                )
            }

            if (isLocked) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onBackground.copy(alpha = Glassmorphism.TEXT_TERTIARY),
                    modifier = Modifier
                        .size(22.dp)
                        .align(Alignment.TopEnd)
                )
            }

            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isLocked) MaterialTheme.colorScheme.onBackground.copy(alpha = Glassmorphism.TEXT_TERTIARY) else MaterialTheme.colorScheme.onBackground.copy(alpha = Glassmorphism.TEXT_PRIMARY),
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
fun NotificationsBar(count: Int) {
    AstraisGlassSurface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        backgroundAlpha = Glassmorphism.BG_TERTIARY,
        onClick = {}
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Notificaciones ($count)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = Glassmorphism.TEXT_PRIMARY),
                fontFamily = FontFamily.Monospace
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = Glassmorphism.ICON_ALPHA),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
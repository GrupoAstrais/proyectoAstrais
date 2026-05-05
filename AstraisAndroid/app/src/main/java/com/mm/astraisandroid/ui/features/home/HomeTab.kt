package com.mm.astraisandroid.ui.features.home


import com.mm.astraisandroid.R
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mm.astraisandroid.util.LottiePetRenderer
import com.mm.astraisandroid.util.AvatarImageRenderer
import com.mm.astraisandroid.data.api.UserMeResponse
import com.mm.astraisandroid.data.models.User
import com.mm.astraisandroid.ui.features.tasks.TaskViewModel
import com.mm.astraisandroid.ui.components.AstraisGlassSurface
import com.mm.astraisandroid.ui.components.AstraisGlassChip
import com.mm.astraisandroid.ui.components.Glassmorphism
import com.mm.astraisandroid.ui.theme.Gray300

@Composable
fun HomeTab(
    user: User?,
    taskViewModel: TaskViewModel = hiltViewModel(),
    isGuest: Boolean = false,
    onNavigateToProfile: () -> Unit = {},
    onNavigateToTasks: () -> Unit = {},
    onNavigateToStore: () -> Unit = {},
    onNavigateToInventory: () -> Unit = {},
    onNavigateToGroups: () -> Unit = {},
    onNavigateToLogros: () -> Unit = {}
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
            username = if (isGuest) stringResource(R.string.profile_guest_name) else (user?.name ?: stringResource(R.string.profile_guest_name)),
            equippedAvatarRef = user?.equippedAvatarRef,
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
            onLogrosClick = { if (!isGuest) onNavigateToLogros() },
            isGuest = isGuest
        )

        if (!isGuest) {
            NotificationsBar(count = 8)
        }
    }
}

@Composable
fun WelcomeHeader(username: String, equippedAvatarRef: String?, onProfileClick: () -> Unit) {
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
                text = stringResource(R.string.home_welcome_hi),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = Glassmorphism.TEXT_SECONDARY),
            )
            Text(
                text = username,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = Glassmorphism.TEXT_PRIMARY),
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
                if (equippedAvatarRef != null) {
                    AvatarImageRenderer(
                        assetRef = equippedAvatarRef,
                        initial = username.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                        size = 52.dp
                    )
                } else {
                    Text(
                        text = username.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = Color.White.copy(alpha = Glassmorphism.TEXT_PRIMARY),
                    )
                }
            }
        }
    }
}

@Composable
fun BannerCard(user: User?) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .shadow(
                elevation = 6.dp,
                shape = MaterialTheme.shapes.extraLarge,
                ambientColor = Color.Black.copy(alpha = 0.2f),
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            )
            .clip(MaterialTheme.shapes.extraLarge)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.45f),
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.2f)
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.2f),
                        Color.White.copy(alpha = 0.05f)
                    )
                ),
                shape = MaterialTheme.shapes.extraLarge
            )
            .clickable { }
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Text(
                    text = stringResource(R.string.home_level, user?.level ?: 0),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = Color.White.copy(alpha = Glassmorphism.TEXT_PRIMARY),
                )
                Text(
                    text = stringResource(R.string.home_xp_progress, user?.currentXp ?: 0, ((user?.level ?: 0) + 1) * 100),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = Glassmorphism.TEXT_SECONDARY),
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
                        text = stringResource(R.string.home_no_pet),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = Glassmorphism.TEXT_TERTIARY),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun GuestWelcomeCard(onRegisterClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .shadow(
                elevation = 6.dp,
                shape = MaterialTheme.shapes.extraLarge,
                ambientColor = Color.Black.copy(alpha = 0.2f),
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            )
            .clip(MaterialTheme.shapes.extraLarge)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.25f)
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.2f),
                        Color.White.copy(alpha = 0.06f)
                    )
                ),
                shape = MaterialTheme.shapes.extraLarge
            )
            .clickable { onRegisterClick() }
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
                    text = stringResource(R.string.guest_welcome_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = Glassmorphism.TEXT_PRIMARY),
                )
                Text(
                    text = stringResource(R.string.guest_welcome_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = Glassmorphism.TEXT_SECONDARY),
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
    onLogrosClick: () -> Unit,
    isGuest: Boolean = false
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        GlassBentoTaskCard(
            modifier = Modifier.weight(1f).fillMaxHeight(),
            topTasks = topTasks,
            totalPending = totalPending,
            onClick = onTasksClick
        )

        Column(
            modifier = Modifier.weight(1f).fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            GlassBentoInventoryCard(
                modifier = Modifier.fillMaxWidth().weight(1.4f),
                user = user,
                onClick = if (!isGuest) onInventoryClick else null,
                isLocked = isGuest
            )

            BentoMiniCell(
                modifier = Modifier.fillMaxWidth().weight(1f),
                backgroundIcon = Icons.Default.ShoppingCart,
                onClick = onStoreClick,
                isLocked = isGuest,
                text = stringResource(R.string.home_store_cell)
            )

            BentoMiniCell(
                modifier = Modifier.fillMaxWidth().weight(1f),
                backgroundIcon = Icons.Default.Star,
                onClick = onLogrosClick,
                isLocked = isGuest,
                text = stringResource(R.string.home_achievements_cell)
            )
        }
    }
}

@Composable
private fun GlassBentoTaskCard(
    modifier: Modifier = Modifier,
    topTasks: List<com.mm.astraisandroid.ui.features.tasks.TaskUIModel>,
    totalPending: Int,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(MaterialTheme.shapes.large)
            .shadow(
                elevation = 4.dp,
                shape = MaterialTheme.shapes.large,
                ambientColor = Color.Black.copy(alpha = 0.15f),
                spotColor = Color.Transparent
            )
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.35f),
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.15f)
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.15f),
                        Color.White.copy(alpha = 0.04f)
                    )
                ),
                shape = MaterialTheme.shapes.large
            )
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = stringResource(R.string.home_tasks_card_title),
                style = MaterialTheme.typography.titleMedium,
                color = Color.White.copy(alpha = Glassmorphism.TEXT_PRIMARY),
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
                            color = Color.White.copy(alpha = Glassmorphism.TEXT_PRIMARY),
                            maxLines = 1
                        )
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(MaterialTheme.shapes.extraSmall)
                                .border(1.dp, Color.White.copy(alpha = Glassmorphism.ICON_ALPHA), MaterialTheme.shapes.extraSmall)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = if (totalPending > 0) stringResource(R.string.home_pending_tasks_count, totalPending) else stringResource(R.string.home_all_caught_up),
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = Glassmorphism.TEXT_TERTIARY),
            )
        }
    }
}

@Composable
private fun GlassBentoInventoryCard(
    modifier: Modifier = Modifier,
    user: User?,
    onClick: (() -> Unit)?,
    isLocked: Boolean
) {
    Box(
        modifier = modifier
            .clip(MaterialTheme.shapes.large)
            .then(
                if (onClick != null) {
                    Modifier.shadow(
                        elevation = 4.dp,
                        shape = MaterialTheme.shapes.large,
                        ambientColor = Color.Black.copy(alpha = 0.15f),
                        spotColor = Color.Transparent
                    )
                } else Modifier
            )
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.35f),
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.15f)
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.15f),
                        Color.White.copy(alpha = 0.04f)
                    )
                ),
                shape = MaterialTheme.shapes.large
            )
            .then(
                if (onClick != null) Modifier.clickable { onClick() } else Modifier
            )
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Text(
                text = stringResource(R.string.home_inventory_card_title),
                style = MaterialTheme.typography.titleMedium,
                color = Color.White.copy(alpha = Glassmorphism.TEXT_PRIMARY),
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
                        tint = Color.White.copy(alpha = 0.12f),
                        modifier = Modifier.size(60.dp)
                    )
                }
            }
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
    Box(
        modifier = modifier
            .clip(MaterialTheme.shapes.extraLarge)
            .shadow(
                elevation = 4.dp,
                shape = MaterialTheme.shapes.extraLarge,
                ambientColor = Color.Black.copy(alpha = 0.15f),
                spotColor = Color.Transparent
            )
            .background(
                if (isLocked) {
                    Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.15f),
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.05f)
                        )
                    )
                } else {
                    Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.3f),
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.1f)
                        )
                    )
                }
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.12f),
                        Color.White.copy(alpha = 0.03f)
                    )
                ),
                shape = MaterialTheme.shapes.extraLarge
            )
            .then(
                if (!isLocked && onClick != null) Modifier.clickable { onClick() } else Modifier
            )
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(20.dp)) {
            if (backgroundIcon != null) {
                Icon(
                    imageVector = backgroundIcon,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.06f),
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
                    tint = Color.White.copy(alpha = Glassmorphism.TEXT_TERTIARY),
                    modifier = Modifier
                        .size(22.dp)
                        .align(Alignment.TopEnd)
                )
            }

            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isLocked) Color.White.copy(alpha = Glassmorphism.TEXT_TERTIARY) else Color.White.copy(alpha = Glassmorphism.TEXT_PRIMARY),
            )
        }
    }
}

@Composable
fun NotificationsBar(count: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(28.dp),
                ambientColor = Color.Black.copy(alpha = 0.15f),
                spotColor = Color.Transparent
            )
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.35f),
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.15f)
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.15f),
                        Color.White.copy(alpha = 0.04f)
                    )
                ),
                shape = RoundedCornerShape(28.dp)
            )
            .clickable { }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.home_notifications, count),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = Glassmorphism.TEXT_PRIMARY),
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = Color.White.copy(alpha = Glassmorphism.ICON_ALPHA),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

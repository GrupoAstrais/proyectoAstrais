package com.mm.astraisandroid.ui.tabs

import android.content.Intent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mm.astraisandroid.api.UserMeResponse
import com.mm.astraisandroid.ui.auth.components.AuthBackground

private val CardBg         = Color.White.copy(alpha = 0.07f)
private val CardBorder     = Color.White.copy(alpha = 0.12f)
private val XpColor        = Color(0xFFC172FF)
private val ActiveDot      = Color(0xFFC172FF)
private val UnlockedBg     = Color(0xFFFFD166).copy(alpha = 0.12f)
private val UnlockedBorder = Color(0xFFFFD166).copy(alpha = 0.35f)

data class Achievement(val label: String, val unlocked: Boolean)
data class Friend(val initial: String, val name: String, val activeToday: Boolean)
data class ActivityItem(val text: String, val time: String)

@Composable
fun PerfilTab(user: UserMeResponse?, onBack: () -> Unit = {}, onLogout: () -> Unit = {}) {
    val context = LocalContext.current
    val achievements = listOf(
        Achievement("Primer logro",     true),
        Achievement("7 días seguidos",  true),
        Achievement("100 tareas",       true),
        Achievement("Nivel 10",         true),
        Achievement("Nivel 20",         false),
        Achievement("Leyenda",          false),
    )

    val friends = listOf(
        Friend("M", "Manuel",  activeToday = true),
        Friend("S", "Samuel",  activeToday = false),
        Friend("O", "Olga", activeToday = false),
        Friend("E", "Elías", activeToday = true),
    )

    val activity = listOf(
        ActivityItem("Completó \"TFG Backend\"",    "hace 2h"),
        ActivityItem("Racha de 7 días conseguida",  "hace 5h"),
        ActivityItem("Subió al nivel 14",           "ayer"),
        ActivityItem("Desbloqueó \"100 tareas\"",   "hace 3 días"),
    )
    fun shareProfileText() {
        val shareText = buildString {
            appendLine("¡Mira mi progreso en Astrais!")
            appendLine()
            appendLine("Usuario: @${user?.nombre?.lowercase() ?: "usuario"}")
            appendLine("Nivel: ${user?.nivel ?: 0}")
            appendLine("XP Total: ${user?.xpTotal ?: 0}")
            appendLine()
            appendLine("¡Únete en astrais.app!")
        }

        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }

        context.startActivity(
            Intent.createChooser(shareIntent, "Compartir perfil")
        )
    }


    AuthBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Top bar
            ProfileTopBar(onBack = onBack)

            // Hero
            ProfileHeroCard(
                name         = user?.nombre ?: "Cargando...",
                username     = "@${user?.nombre?.lowercase() ?: "..."}",
                level        = user?.nivel ?: 0,
                currentXp    = user?.xpActual ?: 0,
                maxXp        = if (user != null) (user.nivel + 1) * 100 else 100,
                registerDate = "Miembro activo",
                onShare      = { shareProfileText() }
            )

            // Stats
            StatsRow(xpTotal = user?.xpTotal ?: 0)

            // Amigos
            FriendsCard(friends = friends)

            // Logros
            AchievementsCard(achievements = achievements)

            // Actividad
            //ActivityCard(items = activity)

            Button(
                onClick = onLogout,
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF4C4C)
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(
                    text = "CERRAR SESIÓN",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun ProfileTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.clickable { onBack() }
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Volver",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = "Perfil",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace
            )
        }

        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(CardBg)
                .border(1.dp, CardBorder, CircleShape)
                .clickable { },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Ajustes",
                tint = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun ProfileHeroCard(
    name: String,
    username: String,
    level: Int,
    currentXp: Int,
    maxXp: Int,
    registerDate: String,
    onShare: () -> Unit = {}
) {
    var editing by remember { mutableStateOf(false) }
    var description by remember { mutableStateOf("") }
    val shape = RoundedCornerShape(20.dp)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(CardBg)
            .border(1.dp, CardBorder, shape)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.1f))
                    .border(2.5.dp, XpColor.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = name.first().uppercaseChar().toString(),
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = name,
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = username,
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Registro: $registerDate",
                    color = Color.White.copy(alpha = 0.25f),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        XpBar(level = level, currentXp = currentXp, maxXp = maxXp)

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "DESCRIPCIÓN",
                    color = Color.White.copy(alpha = 0.35f),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.5.sp
                )
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(RoundedCornerShape(7.dp))
                        .background(Color.White.copy(alpha = 0.07f))
                        .clickable { editing = !editing },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (editing) Icons.Default.Check else Icons.Default.Edit,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.size(13.dp)
                    )
                }
            }

            if (editing) {
                TextField(
                    value = description,
                    onValueChange = { description = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 64.dp),
                    placeholder = {
                        Text(
                            "Escribe algo sobre ti...",
                            color = Color.White.copy(alpha = 0.2f),
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor   = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedTextColor        = Color.White,
                        unfocusedTextColor      = Color.White,
                        focusedIndicatorColor   = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor             = Color.White
                    ),
                    textStyle = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp
                    )
                )
            } else {
                Text(
                    text = if (description.isBlank()) "Toca para añadir..." else description,
                    color = if (description.isBlank())
                        Color.White.copy(alpha = 0.2f)
                    else
                        Color.White.copy(alpha = 0.75f),
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 40.dp)
                        .clickable { editing = true }
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.White.copy(alpha = 0.1f))
                    .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                    .clickable { }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Añadir amigo",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.White.copy(alpha = 0.1f))
                    .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                    .clickable { onShare() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Compartir",
                    tint = Color.White,

                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun XpBar(level: Int, currentXp: Int, maxXp: Int) {
    val progress by animateFloatAsState(
        targetValue = currentXp.toFloat() / maxXp.toFloat(),
        animationSpec = tween(1000),
        label = "xp_bar"
    )

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Nivel $level",
                color = XpColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "$currentXp / $maxXp XP",
                    color = Color.White.copy(alpha = 0.3f),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(50))
                .background(Color.White.copy(alpha = 0.1f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(50))
                    .background(XpColor)
            )
        }
    }
}

@Composable
fun StatsRow(xpTotal: Int) {
    val shape = RoundedCornerShape(20.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(CardBg)
            .border(1.dp, CardBorder, shape)
            .padding(vertical = 20.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        StatItem(label = "Tareas", value = "24")
        Box(modifier = Modifier.width(1.dp).height(36.dp).background(Color.White.copy(alpha = 0.1f)))
        StatItem(label = "Racha", value = "7d")
        Box(modifier = Modifier.width(1.dp).height(36.dp).background(Color.White.copy(alpha = 0.1f)))
        StatItem(label = "XP Total", value = xpTotal.toString())
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = value,
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.35f),
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
fun FriendsCard(friends: List<Friend>) {
    val shape = RoundedCornerShape(20.dp)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(CardBg)
            .border(1.dp, CardBorder, shape)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Amigos (${friends.size})",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.clickable { }
            ) {
                Text(
                    text = "ver todos",
                    color = Color.White.copy(alpha = 0.35f),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.35f),
                    modifier = Modifier.size(12.dp)
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            friends.forEach { friend ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Box {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.1f))
                                .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = friend.initial,
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        if (friend.activeToday) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(ActiveDot)
                                    .border(2.dp, Color.Black.copy(alpha = 0.4f), CircleShape)
                                    .align(Alignment.BottomEnd)
                            )
                        }
                    }
                    Text(
                        text = friend.name,
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Composable
fun AchievementsCard(achievements: List<Achievement>) {
    val unlocked = achievements.count { it.unlocked }
    val progress by animateFloatAsState(
        targetValue = unlocked.toFloat() / achievements.size.toFloat(),
        animationSpec = tween(1000),
        label = "logros_bar"
    )
    val shape = RoundedCornerShape(20.dp)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(CardBg)
            .border(1.dp, CardBorder, shape)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Logros",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "$unlocked / ${achievements.size}",
                color = Color.White.copy(alpha = 0.35f),
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
                .clip(RoundedCornerShape(50))
                .background(Color.White.copy(alpha = 0.1f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(50))
                    .background(XpColor)
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
            achievements.forEach { achievement ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (achievement.unlocked) UnlockedBg
                            else Color.White.copy(alpha = 0.04f)
                        )
                        .border(
                            1.dp,
                            if (achievement.unlocked) UnlockedBorder
                            else Color.White.copy(alpha = 0.07f),
                            RoundedCornerShape(10.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = if (achievement.unlocked) XpColor
                        else Color.White.copy(alpha = 0.15f),
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = achievement.label,
                        color = if (achievement.unlocked) Color.White
                        else Color.White.copy(alpha = 0.25f),
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = if (achievement.unlocked) FontWeight.SemiBold
                        else FontWeight.Normal
                    )
                }
            }
        }
    }
}


@Composable
fun ActivityCard(items: List<ActivityItem>) {
    val shape = RoundedCornerShape(20.dp)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(CardBg)
            .border(1.dp, CardBorder, shape)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Actividad reciente",
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace
        )

        Column {
            items.forEachIndexed { index, item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.25f))
                        )
                        Text(
                            text = item.text,
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Text(
                        text = item.time,
                        color = Color.White.copy(alpha = 0.25f),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                if (index < items.lastIndex) {
                    Box(
                        modifier = Modifier
                            .padding(start = 3.dp)
                            .width(1.dp)
                            .height(6.dp)
                            .background(Color.White.copy(alpha = 0.08f))
                    )
                }
            }
        }
    }
}
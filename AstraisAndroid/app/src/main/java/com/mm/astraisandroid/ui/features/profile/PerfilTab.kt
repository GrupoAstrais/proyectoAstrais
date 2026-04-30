package com.mm.astraisandroid.ui.features.profile

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CurrencyPound
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mm.astraisandroid.data.models.User
import com.mm.astraisandroid.data.preferences.SessionManager
import com.mm.astraisandroid.ui.components.AstraisGlassCard
import com.mm.astraisandroid.ui.components.AstraisGlassSurface
import com.mm.astraisandroid.ui.components.AstraisScreenHeader
import com.mm.astraisandroid.ui.components.Glassmorphism
import com.mm.astraisandroid.ui.features.auth.AuthBackground
import androidx.compose.foundation.text.BasicTextField
import com.mm.astraisandroid.util.AvatarImageRenderer

@Composable
fun PerfilTab(
    user: User?,
    onBack: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val context = LocalContext.current
    val userViewModel: UserViewModel = hiltViewModel()
    val viewModelState by userViewModel.state.collectAsStateWithLifecycle()
    val isGuest = SessionManager.isGuest()

    fun shareProfileText() {
        val shareText = buildString {
            appendLine("¡Mira mi progreso en Astrais!")
            appendLine()
            appendLine("Usuario: @${user?.name?.lowercase() ?: "usuario"}")
            appendLine("Nivel: ${user?.level ?: 0}")
            appendLine("XP Total: ${user?.totalXp ?: 0}")
            appendLine("Ludiones: ${user?.ludiones ?: 0}")
            appendLine()
            appendLine("¡Únete en astrais.app!")
        }
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }
        context.startActivity(Intent.createChooser(shareIntent, "Compartir perfil"))
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
        AstraisScreenHeader(title = "Perfil", onBackClick = onBack)

        ProfileHeroCard(
            user = user,
            isGuest = isGuest,
            onNameChange = { newName -> userViewModel.updateUsername(newName) }
        )

        // 2. STATS ROW
        //  StatsRow(
        //      level = user?.level ?: 0,
        //      totalXp = user?.totalXp ?: 0,
        //      ludiones = user?.ludiones ?: 0
        //  )

        if (!isGuest && user != null) {
            PreferencesCard(
                isLoading = viewModelState.isLoading,
                onSaveLanguage = { lang ->
                    userViewModel.updateProfile(
                        newName = user.name,
                        language = lang,
                        onSuccess = {}
                    )
                }
            )
        }

        if (user != null) {
            EquippedCosmeticCard(equippedPetRef = user.equippedPetRef)
        }

        ShareButton(onShare = ::shareProfileText)

        Button(
            onClick = onLogout,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isGuest)
                    MaterialTheme.colorScheme.tertiary
                else
                    MaterialTheme.colorScheme.error
            ),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Logout,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onError,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = if (isGuest) "REGISTRARSE / INICIAR SESIÓN" else "CERRAR SESIÓN",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onError,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
    }
}


@Composable
private fun ProfileHeroCard(
    user: User?,
    isGuest: Boolean,
    onNameChange: (String) -> Unit
) {
    val name = user?.name ?: "Invitado"
    val level = user?.level ?: 0
    val currentXp = user?.currentXp ?: 0
    val maxXp = (level + 1) * 100

    var editingName by remember(name) { mutableStateOf(false) }
    var nameDraft by remember(name) { mutableStateOf(name) }

    AstraisGlassCard(modifier = Modifier.fillMaxWidth(), onClick = null) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AvatarImageRenderer(
                    assetRef = user?.equippedAvatarRef,
                    initial = name.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (editingName && !isGuest) {
                        InlineNameEditor(
                            value = nameDraft,
                            onChange = { nameDraft = it },
                            onConfirm = {
                                editingName = false
                                if (nameDraft.isNotBlank() && nameDraft != name) {
                                    onNameChange(nameDraft.trim())
                                }
                            },
                            onCancel = {
                                nameDraft = name
                                editingName = false
                            }
                        )
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.clickable(enabled = !isGuest) {
                                nameDraft = name
                                editingName = true
                            }
                        ) {
                            Text(
                                text = name,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onBackground,
                                fontFamily = FontFamily.Monospace,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            if (!isGuest) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Editar nombre",
                                    tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }

                    Text(
                        text = "@${name.lowercase().replace(" ", "")}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = Glassmorphism.TEXT_SECONDARY),
                        fontFamily = FontFamily.Monospace
                    )

                    if (isGuest) {
                        Text(
                            text = "Modo invitado — registra para sincronizar",
                            color = MaterialTheme.colorScheme.tertiary,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            XpBar(level = level, currentXp = currentXp, maxXp = maxXp)
        }
    }
}


@Composable
private fun InlineNameEditor(
    value: String,
    onChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White.copy(alpha = 0.08f))
            .border(1.dp, Color.White.copy(alpha = 0.18f), RoundedCornerShape(10.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        BasicTextField(
            value = value,
            onValueChange = onChange,
            singleLine = true,
            modifier = Modifier.weight(1f),
            textStyle = TextStyle(
                color = MaterialTheme.colorScheme.onBackground,
                fontFamily = FontFamily.Monospace,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            ),
            cursorBrush = Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary))
        )
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = "Guardar",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .size(20.dp)
                .clickable { onConfirm() }
        )
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Cancelar",
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier
                .size(20.dp)
                .clickable { onCancel() }
        )
    }
}


@Composable
private fun XpBar(level: Int, currentXp: Int, maxXp: Int) {
    val safeMax = maxXp.coerceAtLeast(1)
    val progress by animateFloatAsState(
        targetValue = (currentXp.toFloat() / safeMax.toFloat()).coerceIn(0f, 1f),
        animationSpec = tween(900),
        label = "xp_bar"
    )
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "NIVEL $level",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.tertiary,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )
            Text(
                text = "$currentXp / $maxXp XP",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = Glassmorphism.TEXT_SECONDARY),
                fontFamily = FontFamily.Monospace
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color.White.copy(alpha = 0.08f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                MaterialTheme.colorScheme.tertiary,
                                MaterialTheme.colorScheme.primary
                            )
                        )
                    )
            )
        }
    }
}


@Composable
private fun StatsRow(level: Int, totalXp: Int, ludiones: Int) {
    AstraisGlassSurface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 18.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatItem(value = level.toString(), label = "NIVEL", icon = Icons.Default.Star, tint = MaterialTheme.colorScheme.tertiary)
            StatDivider()
            StatItem(value = totalXp.toString(), label = "XP TOTAL", icon = null, tint = MaterialTheme.colorScheme.secondary)
            StatDivider()
            StatItem(value = ludiones.toString(), label = "LUDIONES", icon = Icons.Default.CurrencyPound, tint = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun StatDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(36.dp)
            .background(Color.White.copy(alpha = 0.12f))
    )
}

@Composable
private fun StatItem(value: String, label: String, icon: androidx.compose.ui.graphics.vector.ImageVector?, tint: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (icon != null) {
            Icon(icon, null, tint = tint, modifier = Modifier.size(16.dp))
        }
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onBackground,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f),
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp
        )
    }
}

private val SUPPORTED_LANGUAGES = listOf("ESP" to "Español", "ENG" to "English")

@Composable
private fun PreferencesCard(
    isLoading: Boolean,
    onSaveLanguage: (String) -> Unit
) {
    var selected by remember { mutableStateOf("ESP") }

    AstraisGlassCard(modifier = Modifier.fillMaxWidth(), onClick = null) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SectionHeader(icon = Icons.Default.Language, title = "Idioma")

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SUPPORTED_LANGUAGES.forEach { (code, label) ->
                    LanguagePill(
                        label = label,
                        code = code,
                        selected = selected == code,
                        onClick = { selected = code },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Button(
                onClick = { onSaveLanguage(selected) },
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(16.dp)
                    )
                } else {
                    Text(
                        "Guardar preferencias",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun LanguagePill(
    label: String,
    code: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accent = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) accent.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.05f))
            .border(1.dp, accent.copy(alpha = if (selected) 0.55f else 0.2f), RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(label, color = accent, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        Text(code, color = accent.copy(alpha = 0.7f), fontFamily = FontFamily.Monospace, fontSize = 9.sp, letterSpacing = 1.sp)
    }
}

@Composable
private fun EquippedCosmeticCard(equippedPetRef: String?) {
    AstraisGlassCard(modifier = Modifier.fillMaxWidth(), onClick = null) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SectionHeader(icon = Icons.Default.Pets, title = "Mascota equipada")

            if (equippedPetRef.isNullOrBlank()) {
                Text(
                    text = "Aún no tienes mascota equipada. Visita la tienda o el inventario para elegir una.",
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp
                )
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Pets,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = equippedPetRef,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Cámbiala desde el Inventario",
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ShareButton(onShare: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = 0.06f))
            .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(14.dp))
            .clickable { onShare() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Share,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = "Compartir mi perfil",
            color = MaterialTheme.colorScheme.onBackground,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SectionHeader(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.85f),
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp
        )
    }
}

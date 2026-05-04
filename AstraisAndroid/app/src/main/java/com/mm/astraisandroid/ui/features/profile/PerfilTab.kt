package com.mm.astraisandroid.ui.features.profile


import com.mm.astraisandroid.R
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
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mm.astraisandroid.data.models.User
import com.mm.astraisandroid.ui.components.AstraisGlassCard
import com.mm.astraisandroid.ui.components.AstraisGlassSurface
import com.mm.astraisandroid.ui.components.AstraisScreenHeader
import com.mm.astraisandroid.ui.components.Glassmorphism
import com.mm.astraisandroid.ui.features.auth.AuthBackground
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.res.stringResource
import com.mm.astraisandroid.util.AvatarImageRenderer
import com.mm.astraisandroid.util.LocaleHelper
import com.mm.astraisandroid.ui.theme.Gray300

@Composable
fun PerfilTab(
    user: User?,
    isGuest: Boolean = false,
    onBack: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val context = LocalContext.current
    val userViewModel: UserViewModel = hiltViewModel()
    val viewModelState by userViewModel.state.collectAsStateWithLifecycle()

    fun shareProfileText() {
        val shareText = buildString {
            appendLine(context.getString(R.string.profile_share_title))
            appendLine()
            appendLine(context.getString(R.string.profile_share_username, user?.name?.lowercase() ?: "usuario"))
            appendLine(context.getString(R.string.profile_share_level, user?.level ?: 0))
            appendLine(context.getString(R.string.profile_share_xp, user?.totalXp ?: 0))
            appendLine(context.getString(R.string.profile_share_ludiones, user?.ludiones ?: 0))
            appendLine()
            appendLine(context.getString(R.string.profile_share_join))
        }
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }
        context.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.profile_share_chooser_title)))
    }

    AuthBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AstraisScreenHeader(title = stringResource(R.string.profile_title), onBackClick = onBack)

            GlassProfileHeroCard(
                user = user,
                isGuest = isGuest,
                onNameChange = { newName -> userViewModel.updateUsername(newName) }
            )

            if (!isGuest && user != null) {
                GlassPreferencesCard(
                    isLoading = viewModelState.isLoading,
                    initialLanguage = user.language ?: LocaleHelper.getLanguage(context),
                    onSaveLanguage = { lang ->
                        userViewModel.updateProfile(
                            newName = user.name,
                            language = lang,
                            onSuccess = {
                                val activity = context as? android.app.Activity
                                activity?.recreate()
                            }
                        )
                    }
                )
            }

            if (user != null) {
                GlassEquippedCosmeticCard(equippedPetRef = user.equippedPetRef)
            }

            GlassShareButton(onShare = ::shareProfileText)

            GlassActionButton(
                text = if (isGuest) stringResource(R.string.profile_register_login) else stringResource(R.string.profile_logout),
                isDanger = !isGuest,
                onClick = onLogout
            )

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun GlassProfileHeroCard(
    user: User?,
    isGuest: Boolean,
    onNameChange: (String) -> Unit
) {
    val name = user?.name ?: stringResource(R.string.profile_guest_name)
    val level = user?.level ?: 0
    val currentXp = user?.currentXp ?: 0
    val maxXp = (level + 1) * 100

    var editingName by remember(name) { mutableStateOf(false) }
    var nameDraft by remember(name) { mutableStateOf(name) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = Color.Black.copy(alpha = 0.25f),
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            )
            .clip(RoundedCornerShape(24.dp))
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
                shape = RoundedCornerShape(24.dp)
            )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
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
                        GlassInlineNameEditor(
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
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            if (!isGuest) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = stringResource(R.string.cd_edit_name),
                                    tint = Gray300.copy(alpha = 0.55f),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }

                    Text(
                        text = "@${name.lowercase().replace(" ", "")}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Gray300.copy(alpha = Glassmorphism.TEXT_SECONDARY),
                    )

                    if (isGuest) {
                        Text(
                            text = stringResource(R.string.profile_guest_mode_hint),
                            color = MaterialTheme.colorScheme.tertiary,
                            fontSize = 10.sp,
                        )
                    }
                }
            }

            GlassXpBar(level = level, currentXp = currentXp, maxXp = maxXp)
        }
    }
}

@Composable
private fun GlassInlineNameEditor(
    value: String,
    onChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White.copy(alpha = 0.06f))
            .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
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
                color = Color.White,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            ),
            cursorBrush = Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary))
        )
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = stringResource(R.string.cd_save_name),
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .size(20.dp)
                .clickable { onConfirm() }
        )
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = stringResource(R.string.cd_cancel_edit),
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier
                .size(20.dp)
                .clickable { onCancel() }
        )
    }
}

@Composable
private fun GlassXpBar(level: Int, currentXp: Int, maxXp: Int) {
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
                text = stringResource(R.string.profile_level_label, level),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.tertiary,
                letterSpacing = 1.sp
            )
            Text(
                text = stringResource(R.string.profile_xp_progress, currentXp, maxXp),
                style = MaterialTheme.typography.bodyMedium,
                color = Gray300.copy(alpha = Glassmorphism.TEXT_SECONDARY),
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

private val SUPPORTED_LANGUAGES = listOf("ESP" to "Español", "ENG" to "English")

@Composable
private fun GlassPreferencesCard(
    isLoading: Boolean,
    initialLanguage: String,
    onSaveLanguage: (String) -> Unit
) {
    var selected by remember { mutableStateOf(initialLanguage) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = Color.Black.copy(alpha = 0.2f),
                spotColor = Color.Transparent
            )
            .clip(RoundedCornerShape(20.dp))
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
                shape = RoundedCornerShape(20.dp)
            )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            GlassSectionHeader(icon = Icons.Default.Language, title = stringResource(R.string.profile_language_title))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SUPPORTED_LANGUAGES.forEach { (code, label) ->
                    GlassLanguagePill(
                        label = label,
                        code = code,
                        selected = selected == code,
                        onClick = { selected = code },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (!isLoading) MaterialTheme.colorScheme.primary else Gray300.copy(alpha = 0.3f))
                    .shadow(
                        elevation = if (!isLoading) 6.dp else 0.dp,
                        shape = RoundedCornerShape(12.dp),
                        ambientColor = if (!isLoading) MaterialTheme.colorScheme.primary.copy(alpha = 0.25f) else Color.Transparent,
                        spotColor = if (!isLoading) Color.White.copy(alpha = 0.1f) else Color.Transparent
                    )
                    .clickable(enabled = !isLoading) { onSaveLanguage(selected) }
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(16.dp)
                    )
                } else {
                    Text(
                        stringResource(R.string.profile_save_preferences),
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun GlassLanguagePill(
    label: String,
    code: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accent = if (selected) MaterialTheme.colorScheme.primary else Gray300.copy(alpha = 0.5f)

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.05f))
            .border(1.dp, if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(label, color = accent, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        Text(code, color = accent.copy(alpha = 0.7f), fontSize = 9.sp, letterSpacing = 1.sp)
    }
}

@Composable
private fun GlassEquippedCosmeticCard(equippedPetRef: String?) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = Color.Black.copy(alpha = 0.2f),
                spotColor = Color.Transparent
            )
            .clip(RoundedCornerShape(20.dp))
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
                shape = RoundedCornerShape(20.dp)
            )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            GlassSectionHeader(icon = Icons.Default.Pets, title = stringResource(R.string.profile_equipped_pet_title))

            if (equippedPetRef.isNullOrBlank()) {
                Text(
                    text = stringResource(R.string.profile_no_pet_equipped),
                    color = Gray300.copy(alpha = 0.6f),
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
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
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
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = stringResource(R.string.profile_change_pet_hint),
                            color = Gray300.copy(alpha = 0.55f),
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GlassShareButton(onShare: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = 0.06f))
            .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(14.dp))
            .clickable { onShare() }
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = stringResource(R.string.profile_share_button),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun GlassActionButton(
    text: String,
    isDanger: Boolean,
    onClick: () -> Unit
) {
    val bgColor = if (isDanger) {
        MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
    } else {
        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)
    }
    val borderColor = if (isDanger) {
        MaterialTheme.colorScheme.error.copy(alpha = 0.4f)
    } else {
        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.4f)
    }
    val textColor = if (isDanger) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.tertiary
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Logout,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.titleSmall,
                color = textColor,
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
private fun GlassSectionHeader(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String) {
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
            color = Color.White.copy(alpha = 0.85f),
            letterSpacing = 1.sp
        )
    }
}

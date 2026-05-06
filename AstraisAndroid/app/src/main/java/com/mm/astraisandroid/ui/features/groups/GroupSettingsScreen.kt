package com.mm.astraisandroid.ui.features.groups


import com.mm.astraisandroid.R
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mm.astraisandroid.ui.features.auth.AuthBackground
import com.mm.astraisandroid.ui.theme.Gray300
import com.mm.astraisandroid.ui.theme.Gray700
import com.mm.astraisandroid.ui.theme.Primary
import com.mm.astraisandroid.ui.theme.Secondary
import com.mm.astraisandroid.ui.theme.Surface

private const val ROLE_USER_S = 0
private const val ROLE_MOD_S = 1
private const val ROLE_OWNER_S = 2

/**
 * Pantalla de configuración de un grupo con secciones para editar metadatos, gestionar
 * invitaciones, ver historial de auditoría, transferir propiedad y abandonar el grupo.
 *
 * Las acciones administrativas (editar, crear invitaciones, revocar, transferir propiedad)
 * están restringidas según el rol del usuario (Owner o Moderador).
 *
 * @param gid Identificador del grupo a configurar.
 * @param groupName Nombre actual del grupo usado para prellenar el campo de edición.
 * @param groupDescription Descripción actual del grupo usada para prellenar el campo de edición.
 * @param groupRole Rol del usuario actual en este grupo (0=Miembro, 1=Moderador, 2=Owner).
 * @param onBack Acción ejecutada al pulsar el botón de volver.
 * @param viewModel ViewModel de detalle de grupo inyectado por Hilt.
 */
@Composable
fun GroupSettingsScreen(
    gid: Int,
    groupName: String,
    groupDescription: String,
    groupRole: Int,
    onBack: () -> Unit,
    viewModel: GroupDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val clipboard = LocalClipboardManager.current

    var settingsName by remember(groupName) { mutableStateOf(groupName) }
    var settingsDesc by remember(groupDescription) { mutableStateOf(groupDescription) }
    var confirmLeave by remember { mutableStateOf(false) }
    var confirmRevoke by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(gid) {
        viewModel.loadMembers(gid)
        viewModel.loadInvites(gid)
        viewModel.loadAudit(gid)
    }

    AuthBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.cd_back),
                        tint = Color.White
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.group_settings_title),
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = groupName,
                        color = Gray300.copy(alpha = 0.6f),
                        fontSize = 11.sp
                    )
                }
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = Gray300.copy(alpha = 0.4f),
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                GlassSettingsSection(title = stringResource(R.string.group_section_info), icon = Icons.Default.Settings) {
                    if (groupRole == ROLE_OWNER_S || groupRole == ROLE_MOD_S) {
                        GlassOutlinedTextField(
                            value = settingsName,
                            onValueChange = { settingsName = it },
                            label = stringResource(R.string.group_name_label),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(Modifier.size(8.dp))
                        GlassOutlinedTextField(
                            value = settingsDesc,
                            onValueChange = { settingsDesc = it },
                            label = stringResource(R.string.group_description_label),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.size(8.dp))
                        GlassButton(
                            text = stringResource(R.string.group_save_button),
                            onClick = { viewModel.editGroup(gid, settingsName, settingsDesc) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Text(
                            stringResource(R.string.group_admin_only_edit),
                            color = Gray300.copy(alpha = 0.7f),
                            fontSize = 12.sp
                        )
                    }
                }

                GlassSettingsSection(title = stringResource(R.string.group_section_invites), icon = Icons.Default.MailOutline) {
                    if (groupRole == ROLE_OWNER_S || groupRole == ROLE_MOD_S) {
                        GlassButton(
                            text = stringResource(R.string.group_create_invite_button),
                            onClick = { viewModel.createInvite(gid) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.size(8.dp))
                    }

                    when {
                        state.isLoadingInvites -> Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) { CircularProgressIndicator(color = Primary) }
                        state.invites.isEmpty() -> Text(
                            text = stringResource(R.string.group_no_invites),
                            color = Gray300.copy(alpha = 0.6f),
                            fontSize = 12.sp
                        )
                        else -> Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            state.invites.forEach { inv ->
                                GlassInviteRow(
                                    inviteUrl = inv.inviteUrl,
                                    usesCount = inv.usesCount,
                                    maxUses = inv.maxUses,
                                    expiresAt = inv.expiresAt,
                                    revokedAt = inv.revokedAt,
                                    onCopy = { clipboard.setText(AnnotatedString(inv.inviteUrl)) },
                                    onRevoke = { if (groupRole == ROLE_OWNER_S || groupRole == ROLE_MOD_S) confirmRevoke = inv.code },
                                    canRevoke = (groupRole == ROLE_OWNER_S || groupRole == ROLE_MOD_S) && inv.revokedAt == null
                                )
                            }
                        }
                    }
                }

                GlassSettingsSection(title = stringResource(R.string.group_section_history), icon = Icons.Default.History) {
                    when {
                        state.isLoadingAudit -> Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) { CircularProgressIndicator(color = Primary) }
                        state.auditEvents.isEmpty() -> Text(
                            text = stringResource(R.string.group_no_audit_events),
                            color = Gray300.copy(alpha = 0.6f),
                            fontSize = 12.sp
                        )
                        else -> Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            state.auditEvents.forEach { ev ->
                                GlassAuditRow(eventType = ev.eventType, createdAt = ev.createdAt.toString())
                            }
                        }
                    }
                }

                GlassSettingsSection(title = stringResource(R.string.group_section_members), icon = Icons.Default.PersonRemove) {
                    if (groupRole == ROLE_OWNER_S) {
                        val candidates = state.members.filter { it.role != ROLE_OWNER_S }
                        if (candidates.isEmpty()) {
                            Text(
                                text = "No hay otros miembros",
                                color = Gray300.copy(alpha = 0.6f),
                                fontSize = 12.sp
                            )
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                candidates.forEach { member ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = member.name,
                                            color = Color.White,
                                            fontSize = 14.sp,
                                            modifier = Modifier.weight(1f)
                                        )
                                        TextButton(onClick = { viewModel.passOwnership(gid, member.uid) }) {
                                            Text(
                                                stringResource(R.string.group_transfer_ownership_button),
                                                color = MaterialTheme.colorScheme.primary,
                                                fontSize = 12.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        Text(
                            stringResource(R.string.group_admin_only_edit),
                            color = Gray300.copy(alpha = 0.7f),
                            fontSize = 12.sp
                        )
                    }
                }

                GlassSettingsSection(title = stringResource(R.string.group_section_danger_zone), icon = Icons.Default.Logout) {
                    if (groupRole != ROLE_OWNER_S) {
                        GlassDangerButton(
                            text = stringResource(R.string.group_leave_button),
                            onClick = { confirmLeave = true },
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Text(
                            stringResource(R.string.group_owner_leave_warning),
                            color = Gray300.copy(alpha = 0.7f),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }

    if (confirmLeave) {
        ConfirmActionDialog(
            title = stringResource(R.string.dialog_leave_group_title),
            body = stringResource(R.string.dialog_leave_group_body),
            confirmText = stringResource(R.string.dialog_leave_confirm),
            onConfirm = {
                confirmLeave = false
                viewModel.leaveGroup(gid)
                onBack()
            },
            onDismiss = { confirmLeave = false }
        )
    }

    if (confirmRevoke != null) {
        ConfirmActionDialog(
            title = stringResource(R.string.dialog_revoke_invite_title),
            body = stringResource(R.string.dialog_revoke_invite_body),
            confirmText = stringResource(R.string.dialog_revoke_confirm),
            onConfirm = {
                val code = confirmRevoke
                confirmRevoke = null
                if (code != null) viewModel.revokeInvite(gid, code)
            },
            onDismiss = { confirmRevoke = null }
        )
    }
}

@Composable
private fun GlassSettingsSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = Color.Black.copy(alpha = 0.25f),
                spotColor = Color.White.copy(alpha = 0.05f)
            )
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Surface.copy(alpha = 0.45f),
                        Surface.copy(alpha = 0.2f)
                    )
                )
            )
            .border(
                1.dp,
                Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.18f),
                        Color.White.copy(alpha = 0.04f)
                    )
                ),
                RoundedCornerShape(16.dp)
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Primary,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = title.uppercase(),
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }
        content()
    }
}

@Composable
private fun GlassOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontSize = 13.sp) },
        modifier = modifier,
        singleLine = singleLine,
        keyboardOptions = KeyboardOptions(imeAction = if (singleLine) ImeAction.Next else ImeAction.Default),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Primary,
            unfocusedBorderColor = Color.White.copy(alpha = 0.12f),
            focusedLabelColor = Primary,
            unfocusedLabelColor = Gray300.copy(alpha = 0.5f),
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            cursorColor = Primary,
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent
        )
    )
}

@Composable
private fun GlassButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animatedBg by animateColorAsState(
        targetValue = Primary,
        label = "btnBg"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(animatedBg)
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(12.dp),
                ambientColor = Primary.copy(alpha = 0.25f),
                spotColor = Color.White.copy(alpha = 0.1f)
            )
            .clickable { onClick() }
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun GlassDangerButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dangerColor = MaterialTheme.colorScheme.error

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(dangerColor.copy(alpha = 0.2f))
            .border(1.dp, dangerColor.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = dangerColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun GlassInviteRow(
    inviteUrl: String,
    usesCount: Int,
    maxUses: Int?,
    expiresAt: String?,
    revokedAt: String?,
    onCopy: () -> Unit,
    onRevoke: () -> Unit,
    canRevoke: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.04f))
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                inviteUrl,
                color = Color.White,
                fontSize = 11.sp
            )
            val meta = buildString {
                append(stringResource(R.string.group_invite_uses, usesCount))
                if (maxUses != null) append(stringResource(R.string.group_invite_max_uses_suffix, maxUses))
                if (expiresAt != null) append(stringResource(R.string.group_invite_expires, expiresAt))
                if (revokedAt != null) append(stringResource(R.string.group_invite_revoked))
            }
            Text(
                meta,
                color = Gray300.copy(alpha = 0.6f),
                fontSize = 10.sp
            )
        }
        IconButton(onClick = onCopy) {
            Icon(
                Icons.Default.ContentCopy,
                contentDescription = stringResource(R.string.cd_copy),
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        }
        if (canRevoke) {
            IconButton(onClick = onRevoke) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = stringResource(R.string.cd_revoke_invite),
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun GlassAuditRow(eventType: String, createdAt: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.04f))
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Text(
            eventType,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )
        Text(
            createdAt,
            color = Gray300.copy(alpha = 0.55f),
            fontSize = 10.sp
        )
    }
}

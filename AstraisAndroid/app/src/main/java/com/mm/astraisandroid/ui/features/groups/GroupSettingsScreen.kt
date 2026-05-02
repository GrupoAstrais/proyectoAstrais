package com.mm.astraisandroid.ui.features.groups

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mm.astraisandroid.ui.features.auth.AuthBackground

private const val ROLE_USER_S = 0
private const val ROLE_MOD_S = 1
private const val ROLE_OWNER_S = 2

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
                        contentDescription = "Volver",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Ajustes",
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = groupName,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                    modifier = Modifier.size(20.dp)
                )
            }

            if (state.error != null) {
                Text(
                    text = state.error ?: "",
                    color = MaterialTheme.colorScheme.error,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                SettingsSection(title = "Información del grupo", icon = Icons.Default.Settings) {
                    if (groupRole == ROLE_OWNER_S || groupRole == ROLE_MOD_S) {
                        OutlinedTextField(
                            value = settingsName,
                            onValueChange = { settingsName = it },
                            label = { Text("Nombre", fontFamily = FontFamily.Monospace) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(Modifier.size(8.dp))
                        OutlinedTextField(
                            value = settingsDesc,
                            onValueChange = { settingsDesc = it },
                            label = { Text("Descripción", fontFamily = FontFamily.Monospace) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.size(8.dp))
                        Button(
                            onClick = { viewModel.editGroup(gid, settingsName, settingsDesc) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Guardar", fontFamily = FontFamily.Monospace)
                        }
                    } else {
                        Text(
                            "Solo administradores pueden editar.",
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp
                        )
                    }
                }

                SettingsSection(title = "Invitaciones", icon = Icons.Default.MailOutline) {
                    if (groupRole == ROLE_OWNER_S || groupRole == ROLE_MOD_S) {
                        Button(
                            onClick = { viewModel.createInvite(gid) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Crear invitación", fontFamily = FontFamily.Monospace)
                        }
                        Spacer(Modifier.size(8.dp))
                    }

                    when {
                        state.isLoadingInvites -> Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) { CircularProgressIndicator(color = MaterialTheme.colorScheme.onBackground) }
                        state.invites.isEmpty() -> Text(
                            text = "No hay invitaciones.",
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp
                        )
                        else -> Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            state.invites.forEach { inv ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color.White.copy(alpha = 0.05f))
                                        .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            inv.inviteUrl,
                                            color = MaterialTheme.colorScheme.onBackground,
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 11.sp
                                        )
                                        val meta = buildString {
                                            append("Usos: ${inv.usesCount}")
                                            if (inv.maxUses != null) append("/${inv.maxUses}")
                                            if (inv.expiresAt != null) append(" · Expira: ${inv.expiresAt}")
                                            if (inv.revokedAt != null) append(" · Revocada")
                                        }
                                        Text(
                                            meta,
                                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 10.sp
                                        )
                                    }
                                    IconButton(onClick = {
                                        clipboard.setText(AnnotatedString(inv.inviteUrl))
                                    }) {
                                        Icon(
                                            Icons.Default.ContentCopy,
                                            contentDescription = "Copiar",
                                            tint = MaterialTheme.colorScheme.onBackground,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    if ((groupRole == ROLE_OWNER_S || groupRole == ROLE_MOD_S) && inv.revokedAt == null) {
                                        IconButton(onClick = { confirmRevoke = inv.code }) {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = "Revocar",
                                                tint = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                SettingsSection(title = "Historial", icon = Icons.Default.History) {
                    when {
                        state.isLoadingAudit -> Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) { CircularProgressIndicator(color = MaterialTheme.colorScheme.onBackground) }
                        state.auditEvents.isEmpty() -> Text(
                            text = "Sin eventos registrados.",
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp
                        )
                        else -> Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            state.auditEvents.forEach { ev ->
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color.White.copy(alpha = 0.05f))
                                        .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                                        .padding(12.dp)
                                ) {
                                    Text(
                                        ev.eventType,
                                        color = MaterialTheme.colorScheme.onBackground,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        ev.createdAt.toString(),
                                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f),
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }
                }

                SettingsSection(title = "Zona de peligro", icon = Icons.Default.Logout) {
                    if (groupRole != ROLE_OWNER_S) {
                        Button(
                            onClick = { confirmLeave = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.85f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Salir del grupo", fontFamily = FontFamily.Monospace)
                        }
                    } else {
                        Text(
                            "Para salir siendo owner, transfiere la propiedad primero.",
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }

    if (confirmLeave) {
        ConfirmActionDialog(
            title = "Salir del grupo",
            body = "¿Seguro que quieres salir?",
            confirmText = "Salir",
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
            title = "Revocar invitación",
            body = "¿Confirmas revocar esta invitación?",
            confirmText = "Revocar",
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
private fun SettingsSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.15f)
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
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
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
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.85f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )
        }
        content()
    }
}

package com.mm.astraisandroid.ui.features.groups

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.ui.graphics.Brush
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mm.astraisandroid.ui.features.auth.AuthBackground
import com.mm.astraisandroid.ui.components.AstraisScreenHeader
import com.mm.astraisandroid.ui.components.AstraisGlassSurface
import com.mm.astraisandroid.ui.components.Glassmorphism
import android.content.Intent
import android.app.Activity
import androidx.compose.material3.MaterialTheme

private val GreenAccent = Color(0xFFD6DCEE)
private val CardBg = Color.White.copy(alpha = 0.07f)
private val CardBorder = Color.White.copy(alpha = 0.12f)

private const val ROLE_USER = 0

data class Grupo(
    val id: Int,
    val name: String,
    val subtitle: String,
    val role: Int,
    val hasNewActivity: Boolean = false
)

@Composable
fun GrupoTab(
    onBack: () -> Unit = {},
    onOpenGroup: (Grupo) -> Unit = {},
    viewModel: GroupViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var query by remember { mutableStateOf("") }
    var createDialogOpen by remember { mutableStateOf(false) }
    var selectedGroup by remember { mutableStateOf<Grupo?>(null) }
    var editDialogOpen by remember { mutableStateOf(false) }
    var addUserDialogOpen by remember { mutableStateOf(false) }
    var joinByUrlDialogOpen by remember { mutableStateOf(false) }
    var deleteDialogOpen by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    LaunchedEffect(state.generatedInviteUrl) {
        val url = state.generatedInviteUrl
        if (!url.isNullOrBlank()) {
            clipboardManager.setText(AnnotatedString(url))
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, url)
            }
            val chooser = Intent.createChooser(sendIntent, "Compartir invitacion").apply {
                if (context !is Activity) {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            }
            runCatching { context.startActivity(chooser) }
            viewModel.clearInfoMessage()
        }
    }

    val filtered = state.groups.filter {
        query.isBlank() || it.name.contains(query, ignoreCase = true)
    }

    AuthBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AstraisScreenHeader(
                title = "Grupos",
                trailing = {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AstraisGlassSurface(
                            modifier = Modifier.size(36.dp),
                            shape = RoundedCornerShape(10.dp),
                            backgroundAlpha = Glassmorphism.BG_TERTIARY,
                            onClick = { joinByUrlDialogOpen = true }
                        ) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Unirse por URL",
                                    tint = MaterialTheme.colorScheme.onBackground.copy(alpha = Glassmorphism.ICON_ALPHA)
                                )
                            }
                        }
                        AstraisGlassSurface(
                            modifier = Modifier.size(36.dp),
                            shape = RoundedCornerShape(10.dp),
                            backgroundAlpha = Glassmorphism.BG_TERTIARY,
                            onClick = { createDialogOpen = true }
                        ) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Crear grupo",
                                    tint = MaterialTheme.colorScheme.onBackground.copy(alpha = Glassmorphism.ICON_ALPHA)
                                )
                            }
                        }
                    }
                }
            )
            SearchBar(query = query, onQueryChange = { query = it })

            if (state.error != null) {
                Text(
                    text = state.error ?: "",
                    color = Color(0xFFFF6B6B),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    modifier = Modifier.clickable { viewModel.clearError() }
                )
            } else if (state.infoMessage != null) {
                Text(
                    text = state.infoMessage ?: "",
                    color = GreenAccent,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    modifier = Modifier.clickable { viewModel.clearInfoMessage() }
                )
            }

            GroupStateView(
                isLoading = state.isLoading && state.groups.isEmpty(),
                isEmpty = state.groups.isEmpty() && !state.isLoading,
                emptyText = "No estas en ningun grupo. Crea uno o únete por enlace.",
                errorText = null
            ) {
                if (filtered.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No hay resultados para \"$query\".", color = Color.White.copy(alpha = 0.6f), fontFamily = FontFamily.Monospace)
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(filtered, key = { it.id }) { grupo ->
                            GrupoCard(
                                grupo = grupo,
                                onOpen = { onOpenGroup(grupo) },
                                onEdit = {
                                    selectedGroup = grupo
                                    editDialogOpen = true
                                },
                                onDelete = {
                                    selectedGroup = grupo
                                    deleteDialogOpen = true
                                },
                                onAddUser = {
                                    selectedGroup = grupo
                                    addUserDialogOpen = true
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (createDialogOpen) {
        GroupEditorDialog(
            title = "Crear grupo",
            initialName = "",
            initialDescription = "",
            onDismiss = { createDialogOpen = false },
            onConfirm = { name, desc ->
                viewModel.createGroup(name, desc)
                createDialogOpen = false
            }
        )
    }

    val groupForAction = selectedGroup
    if (editDialogOpen && groupForAction != null) {
        GroupEditorDialog(
            title = "Editar grupo",
            initialName = groupForAction.name,
            initialDescription = groupForAction.subtitle,
            onDismiss = { editDialogOpen = false },
            onConfirm = { name, desc ->
                viewModel.editGroup(gid = groupForAction.id, name = name, desc = desc)
                editDialogOpen = false
            }
        )
    }

    if (addUserDialogOpen && groupForAction != null) {
        AlertDialog(
            onDismissRequest = { addUserDialogOpen = false },
            title = { Text("Generar URL de invitacion") },
            text = { Text("Se generara una URL para compartir y que otros usuarios se unan al grupo.") },
            confirmButton = {
                Button(onClick = {
                    viewModel.createInviteUrl(groupForAction.id)
                    addUserDialogOpen = false
                }) { Text("Generar URL") }
            },
            dismissButton = { TextButton(onClick = { addUserDialogOpen = false }) { Text("Cancelar") } }
        )
    }

    if (joinByUrlDialogOpen) {
        InviteUrlDialog(
            title = "Unirse por URL",
            confirmText = "Unirme",
            onDismiss = { joinByUrlDialogOpen = false },
            onConfirm = { inviteUrl ->
                viewModel.joinByUrl(inviteUrl)
                joinByUrlDialogOpen = false
            }
        )
    }

    if (deleteDialogOpen && groupForAction != null) {
        ConfirmActionDialog(
            title = "Eliminar grupo",
            body = "Seguro que quieres eliminar \"${groupForAction.name}\"?",
            confirmText = "Eliminar",
            onConfirm = {
                viewModel.deleteGroup(groupForAction.id)
                deleteDialogOpen = false
            },
            onDismiss = { deleteDialogOpen = false }
        )
    }
}

@Composable
private fun SearchBar(query: String, onQueryChange: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = 0.07f))
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(Icons.Default.Search, contentDescription = null, tint = Color.White.copy(alpha = 0.3f), modifier = Modifier.size(18.dp))
        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            textStyle = TextStyle(color = Color.White, fontSize = 13.sp, fontFamily = FontFamily.Monospace),
            decorationBox = { inner ->
                if (query.isEmpty()) {
                    Text(text = "Buscar grupo...", color = Color.White.copy(alpha = 0.25f), fontSize = 13.sp, fontFamily = FontFamily.Monospace)
                }
                inner()
            }
        )
        if (query.isNotBlank()) {
            IconButton(onClick = { onQueryChange("") }, modifier = Modifier.size(20.dp)) {
                Icon(Icons.Default.Close, contentDescription = "Limpiar búsqueda", tint = Color.White.copy(alpha = 0.5f))
            }
        }
    }
}

private fun canManageUsers(role: Int): Boolean = role == 2 || role == 1

private const val ROLE_OWNER = 2
private const val ROLE_MOD = 1

private data class RoleVisuals(val icon: ImageVector, val label: String, val color: Color)

@Composable
private fun roleVisuals(role: Int): RoleVisuals = when (role) {
    ROLE_OWNER -> RoleVisuals(Icons.Filled.WorkspacePremium, "OWNER", Color(0xFFE8B94A))
    ROLE_MOD -> RoleVisuals(Icons.Filled.Shield, "MOD", Color(0xFF8CD3FF))
    else -> RoleVisuals(Icons.Filled.Person, "MIEMBRO", MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))
}

@Composable
private fun RoleBadge(role: Int) {
    val v = roleVisuals(role)
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(v.color.copy(alpha = 0.18f))
            .border(1.dp, v.color.copy(alpha = 0.45f), RoundedCornerShape(999.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
            .semantics { contentDescription = "Rol: ${v.label}" },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = v.icon,
            contentDescription = null,
            tint = v.color,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = v.label,
            color = v.color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
private fun ActivityDot(modifier: Modifier = Modifier) {
    val errorColor = MaterialTheme.colorScheme.error
    Box(
        modifier = modifier
            .size(10.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(errorColor)
            .border(2.dp, errorColor.copy(alpha = 0.25f), RoundedCornerShape(999.dp))
            .semantics { contentDescription = "Hay actividad nueva" }
    )
}

@Composable
private fun GrupoCard(
    grupo: Grupo,
    onOpen: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onAddUser: () -> Unit
) {
    val canManage = canManageUsers(grupo.role)
    val canDelete = grupo.role != ROLE_USER

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.15f)
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
                shape = RoundedCornerShape(20.dp)
            )
            .clickable { onOpen() }
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = grupo.name,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (grupo.hasNewActivity) {
                        ActivityDot()
                    }
                }

                if (grupo.subtitle.isNotBlank()) {
                    Text(
                        text = grupo.subtitle,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f),
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 2,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
            }

            RoleBadge(role = grupo.role)
        }

        if (grupo.hasNewActivity) {
            Text(
                text = "Nuevas tareas",
                color = MaterialTheme.colorScheme.error,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 0.5.sp
            )
        }

        if (canManage || canDelete) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (canManage) {
                    GroupActionChip(
                        icon = Icons.Default.GroupAdd,
                        label = "Invitar",
                        onClick = onAddUser
                    )
                    GroupActionChip(
                        icon = Icons.Default.Edit,
                        label = "Editar",
                        onClick = onEdit
                    )
                }
                if (canDelete) {
                    GroupActionChip(
                        icon = Icons.Default.Delete,
                        label = "Eliminar",
                        onClick = onDelete,
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun GroupActionChip(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    tint: Color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.85f)
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(tint.copy(alpha = 0.1f))
            .border(1.dp, tint.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = label,
            color = tint,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
private fun GroupEditorDialog(
    title: String,
    initialName: String,
    initialDescription: String,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var desc by remember { mutableStateOf(initialDescription) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nombre") }, singleLine = true)
                OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Descripcion") }, minLines = 2)
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(name.trim(), desc.trim()) }, enabled = name.trim().length >= 3) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
private fun InviteUrlDialog(
    title: String,
    confirmText: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var urlText by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = urlText,
                onValueChange = { urlText = it },
                label = { Text("URL de invitacion") },
                singleLine = true
            )
        },
        confirmButton = {
            Button(onClick = { onConfirm(urlText.trim()) }, enabled = urlText.trim().isNotBlank()) {
                Text(confirmText)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

package com.mm.astraisandroid.ui.features.groups


import com.mm.astraisandroid.R
import android.app.Activity
import android.content.Intent
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mm.astraisandroid.ui.features.auth.AuthBackground
import com.mm.astraisandroid.ui.components.AstraisScreenHeader
import com.mm.astraisandroid.ui.components.AstraisGlassSurface
import com.mm.astraisandroid.ui.components.Glassmorphism
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.res.stringResource

private val GreenAccent = Color(0xFFD6DCEE)

private const val ROLE_USER = 0
private const val ROLE_OWNER = 2
private const val ROLE_MOD = 1

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
            val chooser = Intent.createChooser(sendIntent, context.getString(R.string.group_share_invite_chooser_title)).apply {
                if (context !is Activity) {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            }
            runCatching { context.startActivity(chooser) }
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
                title = stringResource(R.string.group_title),
                trailing = {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        GlassActionButton(
                            icon = Icons.Default.Share,
                            contentDescription = stringResource(R.string.cd_join_by_url),
                            onClick = { joinByUrlDialogOpen = true }
                        )
                        GlassActionButton(
                            icon = Icons.Default.Add,
                            contentDescription = stringResource(R.string.cd_create_group),
                            onClick = { createDialogOpen = true }
                        )
                    }
                }
            )
            GlassSearchBar(query = query, onQueryChange = { query = it })

            GroupStateView(
                isLoading = state.isLoading && state.groups.isEmpty(),
                isEmpty = state.groups.isEmpty() && !state.isLoading,
                emptyText = stringResource(R.string.group_empty_message),
                errorText = null
            ) {
                if (filtered.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White.copy(alpha = 0.03f))
                            .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            stringResource(R.string.group_no_search_results, query),
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 14.sp
                        )
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(filtered, key = { it.id }) { grupo ->
                            GlassGrupoCard(
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
        GlassGroupEditorDialog(
            title = stringResource(R.string.dialog_create_group_title),
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
        GlassGroupEditorDialog(
            title = stringResource(R.string.dialog_edit_group_title),
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
        GlassInviteGenerateDialog(
            onDismiss = { addUserDialogOpen = false },
            onConfirm = {
                viewModel.createInviteUrl(groupForAction.id)
                addUserDialogOpen = false
            }
        )
    }

    if (joinByUrlDialogOpen) {
        GlassInviteUrlDialog(
            title = stringResource(R.string.dialog_join_by_url_title),
            confirmText = stringResource(R.string.dialog_join_by_url_confirm),
            onDismiss = { joinByUrlDialogOpen = false },
            onConfirm = { inviteUrl ->
                viewModel.joinByUrl(inviteUrl)
                joinByUrlDialogOpen = false
            }
        )
    }

    if (deleteDialogOpen && groupForAction != null) {
        ConfirmActionDialog(
            title = stringResource(R.string.dialog_delete_group_title),
            body = stringResource(R.string.dialog_delete_group_body, groupForAction.name),
            confirmText = stringResource(R.string.dialog_delete_confirm),
            onConfirm = {
                viewModel.deleteGroup(groupForAction.id)
                deleteDialogOpen = false
            },
            onDismiss = { deleteDialogOpen = false }
        )
    }
}

@Composable
private fun GlassActionButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }

    val bgAlpha by animateColorAsState(
        targetValue = if (isPressed) Color.White.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.1f),
        label = "btnBg"
    )

    AstraisGlassSurface(
        modifier = Modifier.size(36.dp),
        shape = RoundedCornerShape(10.dp),
        backgroundAlpha = Glassmorphism.BG_TERTIARY,
        onClick = {
            isPressed = true
            onClick()
            isPressed = false
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(bgAlpha),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = Color.White.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun GlassSearchBar(query: String, onQueryChange: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = 0.06f))
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
            textStyle = TextStyle(color = Color.White, fontSize = 13.sp),
            decorationBox = { inner ->
                if (query.isEmpty()) {
                    Text(text = stringResource(R.string.group_search_placeholder), color = Color.White.copy(alpha = 0.25f), fontSize = 13.sp)
                }
                inner()
            }
        )
        if (query.isNotBlank()) {
            IconButton(onClick = { onQueryChange("") }, modifier = Modifier.size(20.dp)) {
                Icon(Icons.Default.Close, contentDescription = stringResource(R.string.cd_clear_search), tint = Color.White.copy(alpha = 0.5f))
            }
        }
    }
}

private fun canManageUsers(role: Int): Boolean = role == ROLE_OWNER || role == ROLE_MOD

private data class RoleVisuals(val icon: ImageVector, val label: String, val color: Color)

@Composable
private fun roleVisuals(role: Int): RoleVisuals = when (role) {
    ROLE_OWNER -> RoleVisuals(Icons.Filled.WorkspacePremium, stringResource(R.string.group_role_badge_owner), Color(0xFFE8B94A))
    ROLE_MOD -> RoleVisuals(Icons.Filled.Shield, stringResource(R.string.group_role_badge_mod), Color(0xFF8CD3FF))
    else -> RoleVisuals(Icons.Filled.Person, stringResource(R.string.group_role_badge_member), Color.White.copy(alpha = 0.7f))
}

@Composable
private fun GlassRoleBadge(role: Int) {
    val v = roleVisuals(role)
    val roleCd = stringResource(R.string.cd_role_label, v.label)
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(v.color.copy(alpha = 0.15f))
            .border(1.dp, v.color.copy(alpha = 0.4f), RoundedCornerShape(999.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
            .semantics { contentDescription = roleCd },
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
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
private fun GlassActivityDot(modifier: Modifier = Modifier) {
    val errorColor = MaterialTheme.colorScheme.error
    val newActivityCd = stringResource(R.string.cd_new_activity)
    Box(
        modifier = modifier
            .size(10.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(errorColor)
            .border(2.dp, errorColor.copy(alpha = 0.25f), RoundedCornerShape(999.dp))
            .semantics { contentDescription = newActivityCd }
    )
}

@Composable
private fun GlassGrupoCard(
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
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = Color.Black.copy(alpha = 0.2f),
                spotColor = Color.White.copy(alpha = 0.05f)
            )
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.45f),
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.18f)
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
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (grupo.hasNewActivity) {
                        GlassActivityDot()
                    }
                }

                if (grupo.subtitle.isNotBlank()) {
                    Text(
                        text = grupo.subtitle,
                        color = Color.White.copy(alpha = 0.65f),
                        fontSize = 12.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            GlassRoleBadge(role = grupo.role)
        }

        if (grupo.hasNewActivity) {
            Text(
                text = stringResource(R.string.group_new_tasks_badge),
                color = MaterialTheme.colorScheme.error,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }

        if (canManage || canDelete) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (canManage) {
                    GlassGroupActionChip(
                        icon = Icons.Default.GroupAdd,
                        label = stringResource(R.string.group_invite_action),
                        onClick = onAddUser
                    )
                    GlassGroupActionChip(
                        icon = Icons.Default.Edit,
                        label = stringResource(R.string.group_edit_action),
                        onClick = onEdit
                    )
                }
                if (canDelete) {
                    GlassGroupActionChip(
                        icon = Icons.Default.Delete,
                        label = stringResource(R.string.group_delete_action),
                        onClick = onDelete,
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun GlassGroupActionChip(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    tint: Color = Color.White.copy(alpha = 0.85f)
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(tint.copy(alpha = 0.1f))
            .border(1.dp, tint.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
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
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun GlassGroupEditorDialog(
    title: String,
    initialName: String,
    initialDescription: String,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var desc by remember { mutableStateOf(initialDescription) }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(28.dp))
                .shadow(
                    elevation = 16.dp,
                    shape = RoundedCornerShape(28.dp),
                    ambientColor = Color.Black.copy(alpha = 0.4f),
                    spotColor = com.mm.astraisandroid.ui.theme.Primary.copy(alpha = 0.1f)
                )
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            com.mm.astraisandroid.ui.theme.Surface.copy(alpha = 0.92f),
                            com.mm.astraisandroid.ui.theme.Surface.copy(alpha = 0.85f)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.18f),
                            Color.White.copy(alpha = 0.06f)
                        )
                    ),
                    shape = RoundedCornerShape(28.dp)
                )
                .padding(24.dp)
        ) {
            Column(
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                GlassDialogTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = stringResource(R.string.dialog_label_name),
                    singleLine = true
                )

                GlassDialogTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = stringResource(R.string.dialog_label_description),
                    singleLine = false
                )

                Row(
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.06f))
                            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                            .clickable { onDismiss() }
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(stringResource(R.string.dialog_cancel), color = com.mm.astraisandroid.ui.theme.Gray300, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (name.trim().length >= 3) com.mm.astraisandroid.ui.theme.Primary else com.mm.astraisandroid.ui.theme.Gray700)
                            .shadow(
                                elevation = if (name.trim().length >= 3) 8.dp else 0.dp,
                                shape = RoundedCornerShape(12.dp),
                                ambientColor = if (name.trim().length >= 3) com.mm.astraisandroid.ui.theme.Primary.copy(alpha = 0.3f) else Color.Transparent,
                                spotColor = if (name.trim().length >= 3) Color.White.copy(alpha = 0.1f) else Color.Transparent
                            )
                            .clickable(enabled = name.trim().length >= 3) { onConfirm(name.trim(), desc.trim()) }
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(stringResource(R.string.dialog_save), color = if (name.trim().length >= 3) Color.White else com.mm.astraisandroid.ui.theme.Gray300.copy(alpha = 0.4f), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun GlassInviteGenerateDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(28.dp))
                .shadow(
                    elevation = 16.dp,
                    shape = RoundedCornerShape(28.dp),
                    ambientColor = Color.Black.copy(alpha = 0.4f),
                    spotColor = com.mm.astraisandroid.ui.theme.Primary.copy(alpha = 0.1f)
                )
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            com.mm.astraisandroid.ui.theme.Surface.copy(alpha = 0.92f),
                            com.mm.astraisandroid.ui.theme.Surface.copy(alpha = 0.85f)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.18f),
                            Color.White.copy(alpha = 0.06f)
                        )
                    ),
                    shape = RoundedCornerShape(28.dp)
                )
                .padding(24.dp)
        ) {
            Column(
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.dialog_generate_invite_url_title),
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.dialog_generate_invite_url_body),
                    color = com.mm.astraisandroid.ui.theme.Gray300,
                    fontSize = 14.sp
                )
                Row(
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.06f))
                            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                            .clickable { onDismiss() }
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(stringResource(R.string.dialog_cancel), color = com.mm.astraisandroid.ui.theme.Gray300, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(com.mm.astraisandroid.ui.theme.Primary)
                            .shadow(
                                elevation = 8.dp,
                                shape = RoundedCornerShape(12.dp),
                                ambientColor = com.mm.astraisandroid.ui.theme.Primary.copy(alpha = 0.3f),
                                spotColor = Color.White.copy(alpha = 0.1f)
                            )
                            .clickable { onConfirm() }
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(stringResource(R.string.dialog_generate_url_button), color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun GlassInviteUrlDialog(
    title: String,
    confirmText: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var urlText by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(28.dp))
                .shadow(
                    elevation = 16.dp,
                    shape = RoundedCornerShape(28.dp),
                    ambientColor = Color.Black.copy(alpha = 0.4f),
                    spotColor = com.mm.astraisandroid.ui.theme.Primary.copy(alpha = 0.1f)
                )
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            com.mm.astraisandroid.ui.theme.Surface.copy(alpha = 0.92f),
                            com.mm.astraisandroid.ui.theme.Surface.copy(alpha = 0.85f)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.18f),
                            Color.White.copy(alpha = 0.06f)
                        )
                    ),
                    shape = RoundedCornerShape(28.dp)
                )
                .padding(24.dp)
        ) {
            Column(
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                GlassDialogTextField(
                    value = urlText,
                    onValueChange = { urlText = it },
                    label = stringResource(R.string.dialog_label_invite_url),
                    singleLine = true
                )

                Row(
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.06f))
                            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                            .clickable { onDismiss() }
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(stringResource(R.string.dialog_cancel), color = com.mm.astraisandroid.ui.theme.Gray300, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (urlText.trim().isNotBlank()) com.mm.astraisandroid.ui.theme.Primary else com.mm.astraisandroid.ui.theme.Gray700)
                            .shadow(
                                elevation = if (urlText.trim().isNotBlank()) 8.dp else 0.dp,
                                shape = RoundedCornerShape(12.dp),
                                ambientColor = if (urlText.trim().isNotBlank()) com.mm.astraisandroid.ui.theme.Primary.copy(alpha = 0.3f) else Color.Transparent,
                                spotColor = if (urlText.trim().isNotBlank()) Color.White.copy(alpha = 0.1f) else Color.Transparent
                            )
                            .clickable(enabled = urlText.trim().isNotBlank()) { onConfirm(urlText.trim()) }
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(confirmText, color = if (urlText.trim().isNotBlank()) Color.White else com.mm.astraisandroid.ui.theme.Gray300.copy(alpha = 0.4f), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun GlassDialogTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    singleLine: Boolean
) {
    Column(
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
    ) {
        Text(label, color = com.mm.astraisandroid.ui.theme.Gray300.copy(alpha = 0.6f), fontSize = 10.sp, letterSpacing = 1.5.sp, fontWeight = FontWeight.SemiBold)
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(alpha = 0.04f))
                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                .padding(horizontal = 14.dp, vertical = 14.dp),
            textStyle = TextStyle(color = Color.White, fontSize = 14.sp),
            singleLine = singleLine,
            decorationBox = { inner ->
                if (value.isEmpty()) {
                    Text(label, color = com.mm.astraisandroid.ui.theme.Gray300.copy(alpha = 0.3f), fontSize = 14.sp)
                }
                inner()
            }
        )
    }
}

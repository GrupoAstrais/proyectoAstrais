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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mm.astraisandroid.data.api.GroupMemberOut
import com.mm.astraisandroid.ui.features.auth.AuthBackground
import com.mm.astraisandroid.ui.features.tasks.CreateTareaDialog
import com.mm.astraisandroid.ui.features.tasks.EditTaskDialog
import com.mm.astraisandroid.ui.features.tasks.TaskCard
import com.mm.astraisandroid.ui.features.tasks.TaskUIModel
import com.mm.astraisandroid.ui.theme.Gray300
import com.mm.astraisandroid.ui.theme.Gray700
import com.mm.astraisandroid.ui.theme.Primary
import com.mm.astraisandroid.ui.theme.Secondary
import com.mm.astraisandroid.ui.theme.Surface
import kotlinx.coroutines.launch

private const val ROLE_USER = 0
private const val ROLE_MOD = 1
private const val ROLE_OWNER = 2

private const val MAX_AVATARS_VISIBLE = 5

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupDetailScreen(
    gid: Int,
    groupName: String,
    groupDescription: String,
    groupRole: Int,
    onBack: () -> Unit,
    onOpenSettings: () -> Unit = {},
    onUserStateChanged: () -> Unit = {},
    viewModel: GroupDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var createDialogOpen by remember { mutableStateOf(false) }
    var createDialogParentId by remember { mutableStateOf<Int?>(null) }
    var membersSheetOpen by remember { mutableStateOf(false) }
    var confirmKick by remember { mutableStateOf<Int?>(null) }
    var expandedTaskId by remember { mutableStateOf<Int?>(null) }
    var taskToEdit by remember { mutableStateOf<TaskUIModel?>(null) }
    var taskToDelete by remember { mutableStateOf<TaskUIModel?>(null) }
    var showCompleted by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    val canManage = groupRole == ROLE_OWNER || groupRole == ROLE_MOD

    LaunchedEffect(gid) {
        viewModel.loadTasks(gid)
        viewModel.loadMembers(gid)
    }

    AuthBackground {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                GlassGroupDetailHeader(
                    groupName = groupName,
                    groupRole = groupRole,
                    onBack = onBack,
                    onOpenSettings = onOpenSettings
                )

                if (groupDescription.isNotBlank()) {
                    Text(
                        text = groupDescription,
                        color = Gray300.copy(alpha = 0.6f),
                        fontSize = 12.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                MembersCarousel(
                    members = state.members,
                    isLoading = state.isLoadingMembers,
                    onTap = { membersSheetOpen = true }
                )

                val pendingCount = state.tasks.count { it.parentId == null && !it.isCompleted }
                val completedCount = state.tasks.count { it.parentId == null && it.isCompleted }

                GlassTaskFilterToggle(
                    showCompleted = showCompleted,
                    pendingCount = pendingCount,
                    completedCount = completedCount,
                    onToggle = { showCompleted = !showCompleted }
                )

                Box(modifier = Modifier.fillMaxSize()) {
                    val topLevel = state.tasks.filter {
                        it.parentId == null && it.isCompleted == showCompleted
                    }
                    val subtasksByParent = state.tasks.filter { it.parentId != null }.groupBy { it.parentId!! }

                    when {
                        state.isLoadingTasks && state.tasks.isEmpty() -> {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = Primary)
                            }
                        }
                        topLevel.isEmpty() -> {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(
                                    text = if (showCompleted)
                                        stringResource(R.string.task_no_completed_yet)
                                    else
                                        stringResource(R.string.task_no_pending),
                                    color = Gray300.copy(alpha = 0.6f),
                                )
                            }
                        }
                        else -> {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 96.dp)
                            ) {
                                items(topLevel, key = { it.id }) { task ->
                                    TaskCard(
                                        task = task,
                                        subtasks = subtasksByParent[task.id].orEmpty(),
                                        isExpanded = expandedTaskId == task.id,
                                        onToggleExpand = {
                                            expandedTaskId = if (expandedTaskId == task.id) null else task.id
                                        },
                                        onToggleComplete = { t ->
                                            viewModel.toggleTaskCompletion(gid, t)
                                            onUserStateChanged()
                                        },
                                        onAddSubtask = { if (canManage) { createDialogParentId = task.id; createDialogOpen = true } },
                                        onEditSubtask = { sub -> if (canManage) taskToEdit = sub },
                                        onDeleteSubtask = { sub -> if (canManage) taskToDelete = sub },
                                        onEdit = { if (canManage) taskToEdit = task },
                                        onDelete = { if (canManage) taskToDelete = task }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (canManage) {
                FloatingActionButton(
                    onClick = { createDialogParentId = null; createDialogOpen = true },
                    containerColor = Primary,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(20.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(R.string.cd_create_task),
                        tint = Color.White
                    )
                }
            }
        }
    }

    if (createDialogOpen) {
        val parentId = createDialogParentId
        CreateTareaDialog(
            parentId = parentId,
            onDismiss = { createDialogOpen = false; createDialogParentId = null },
            onCreate = { titulo, desc, tipoStr, prioridadInt, frecuencia, fechaLimite ->
                viewModel.createTaskFromDialog(
                    gid = gid,
                    title = titulo,
                    description = desc,
                    tipo = tipoStr,
                    prioridad = prioridadInt,
                    frecuencia = frecuencia,
                    fechaLimite = fechaLimite,
                    parentId = parentId
                )
                createDialogOpen = false
                createDialogParentId = null
            }
        )
    }

    if (membersSheetOpen) {
        ModalBottomSheet(
            onDismissRequest = { membersSheetOpen = false },
            sheetState = sheetState,
            containerColor = Surface
        ) {
            MembersSheetContent(
                members = state.members,
                viewerRole = groupRole,
                onClose = {
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        if (!sheetState.isVisible) membersSheetOpen = false
                    }
                },
                onPromote = { uid -> viewModel.setMemberRole(gid, uid, ROLE_MOD) },
                onDemote = { uid -> viewModel.setMemberRole(gid, uid, ROLE_USER) },
                onKick = { uid -> confirmKick = uid }
            )
        }
    }

    taskToEdit?.let { task ->
        EditTaskDialog(
            task = task,
            onDismiss = { taskToEdit = null },
            onEdit = { titulo, desc, prio, fechaLimite, frecuencia ->
                viewModel.editTask(
                    gid = gid,
                    tid = task.id,
                    titulo = titulo,
                    descripcion = desc,
                    prioridad = prio,
                    fechaLimite = fechaLimite,
                    frecuencia = frecuencia
                )
                taskToEdit = null
            }
        )
    }

    taskToDelete?.let { task ->
        ConfirmActionDialog(
            title = stringResource(R.string.dialog_delete_task_title),
            body = stringResource(R.string.dialog_delete_task_body, task.title),
            confirmText = stringResource(R.string.dialog_delete_confirm),
            onConfirm = {
                viewModel.deleteTask(gid, task.id)
                taskToDelete = null
            },
            onDismiss = { taskToDelete = null }
        )
    }

    if (confirmKick != null) {
        ConfirmActionDialog(
            title = stringResource(R.string.dialog_kick_member_title),
            body = stringResource(R.string.dialog_kick_member_body),
            confirmText = stringResource(R.string.dialog_kick_confirm),
            onConfirm = {
                val uid = confirmKick
                confirmKick = null
                if (uid != null) viewModel.kickMember(gid, uid)
            },
            onDismiss = { confirmKick = null }
        )
    }
}

@Composable
private fun GlassGroupDetailHeader(
    groupName: String,
    groupRole: Int,
    onBack: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val roleLabel = when (groupRole) {
        ROLE_OWNER -> stringResource(R.string.group_role_owner)
        ROLE_MOD -> stringResource(R.string.group_role_mod)
        else -> stringResource(R.string.group_role_member)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Surface.copy(alpha = 0.3f))
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.12f),
                        Color.White.copy(alpha = 0.04f)
                    )
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 12.dp, vertical = 10.dp),
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
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = groupName,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = roleLabel,
                color = Gray300.copy(alpha = 0.6f),
                fontSize = 10.sp,
                letterSpacing = 0.5.sp
            )
        }
        IconButton(onClick = onOpenSettings) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = stringResource(R.string.cd_group_settings),
                tint = Color.White
            )
        }
    }
}

@Composable
private fun MembersCarousel(
    members: List<GroupMemberOut>,
    isLoading: Boolean,
    onTap: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = stringResource(R.string.group_members_header, members.size),
            color = Gray300.copy(alpha = 0.5f),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        if (isLoading && members.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                CircularProgressIndicator(
                    color = Primary,
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            }
        } else {
            val visible = members.take(MAX_AVATARS_VISIBLE)
            val remainder = members.size - visible.size
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 4.dp)
            ) {
                itemsIndexed(visible, key = { _, m -> m.uid }) { _, m ->
                    MemberAvatar(member = m, onClick = onTap)
                }
                if (remainder > 0) {
                    item(key = "remainder") {
                        GlassOverflowAvatar(count = remainder, onClick = onTap)
                    }
                }
            }
        }
    }
}

@Composable
private fun MemberAvatar(member: GroupMemberOut, onClick: () -> Unit) {
    val initials = remember(member.name) { initialsOf(member.name) }
    val bg = remember(member.uid) { colorForId(member.uid) }

    val borderColor = when (member.role) {
        ROLE_OWNER -> Color(0xFFE8B94A)
        ROLE_MOD -> Color(0xFF8CD3FF)
        else -> Color.White.copy(alpha = 0.2f)
    }

    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(bg)
            .border(2.dp, borderColor, CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun GlassOverflowAvatar(count: Int, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .shadow(
                elevation = 4.dp,
                shape = CircleShape,
                ambientColor = Color.Black.copy(alpha = 0.2f),
                spotColor = Color.White.copy(alpha = 0.05f)
            )
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Surface.copy(alpha = 0.4f),
                        Surface.copy(alpha = 0.15f)
                    )
                )
            )
            .border(2.dp, Color.White.copy(alpha = 0.2f), CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "+$count",
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun MembersSheetContent(
    members: List<GroupMemberOut>,
    viewerRole: Int,
    onClose: () -> Unit,
    onPromote: (Int) -> Unit,
    onDemote: (Int) -> Unit,
    onKick: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = stringResource(R.string.group_members_sheet_title),
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Spacer(Modifier.size(12.dp))
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(members, key = { it.uid }) { m ->
                GlassMemberRow(
                    member = m,
                    viewerRole = viewerRole,
                    onPromote = { onPromote(m.uid) },
                    onDemote = { onDemote(m.uid) },
                    onKick = { onKick(m.uid) }
                )
            }
        }
        Spacer(Modifier.size(12.dp))
        TextButton(onClick = onClose, modifier = Modifier.align(Alignment.End)) {
            Text(stringResource(R.string.dialog_close))
        }
    }
}

@Composable
private fun GlassMemberRow(
    member: GroupMemberOut,
    viewerRole: Int,
    onPromote: () -> Unit,
    onDemote: () -> Unit,
    onKick: () -> Unit
) {
    val canManageThis = (viewerRole == ROLE_OWNER || viewerRole == ROLE_MOD) && member.role != ROLE_OWNER
    val canPromoteDemote = viewerRole == ROLE_OWNER && member.role != ROLE_OWNER

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = 0.04f))
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(14.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(colorForId(member.uid)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initialsOf(member.name),
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = member.name,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
            RoleChip(role = member.role)
        }

        if (canPromoteDemote) {
            if (member.role == ROLE_MOD) {
                TextButton(onClick = onDemote) {
                    Text(
                        stringResource(R.string.group_demote_mod),
                        color = Color.White,
                        fontSize = 11.sp
                    )
                }
            } else {
                TextButton(onClick = onPromote) {
                    Text(
                        stringResource(R.string.group_promote_mod),
                        color = Color.White,
                        fontSize = 11.sp
                    )
                }
            }
        }

        if (canManageThis) {
            IconButton(onClick = onKick) {
                Icon(
                    Icons.Default.PersonRemove,
                    contentDescription = stringResource(R.string.cd_kick_member),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun GlassTaskFilterToggle(
    showCompleted: Boolean,
    pendingCount: Int,
    completedCount: Int,
    onToggle: () -> Unit
) {
    val activeLabel = if (showCompleted) stringResource(R.string.task_status_completed) else stringResource(R.string.task_status_pending)
    val activeCount = if (showCompleted) completedCount else pendingCount
    val inactiveLabel = if (showCompleted) stringResource(R.string.task_view_pending) else stringResource(R.string.task_view_completed)
    val inactiveCount = if (showCompleted) pendingCount else completedCount

    val animatedBg by animateColorAsState(
        targetValue = if (showCompleted) Primary.copy(alpha = 0.2f) else Surface.copy(alpha = 0.3f),
        label = "filterBg"
    )
    val animatedBorder by animateColorAsState(
        targetValue = if (showCompleted) Primary.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.12f),
        label = "filterBorder"
    )
    val accent = if (showCompleted) Primary else Color.White

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(animatedBg)
            .border(1.dp, animatedBorder, RoundedCornerShape(12.dp))
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(12.dp),
                ambientColor = Color.Black.copy(alpha = 0.15f),
                spotColor = if (showCompleted) Primary.copy(alpha = 0.1f) else Color.Transparent
            )
            .clickable { onToggle() }
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            imageVector = if (showCompleted) Icons.Default.VisibilityOff else Icons.Default.Visibility,
            contentDescription = null,
            tint = accent,
            modifier = Modifier.size(18.dp)
        )
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = stringResource(R.string.task_active_status_count, activeLabel, activeCount),
                color = accent,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp
            )
            Text(
                text = stringResource(R.string.task_inactive_status_count, inactiveLabel, inactiveCount),
                color = Gray300.copy(alpha = 0.5f),
                fontSize = 10.sp
            )
        }
    }
}

private fun initialsOf(name: String): String {
    val parts = name.trim().split("\\s+".toRegex()).filter { it.isNotBlank() }
    return when {
        parts.isEmpty() -> "?"
        parts.size == 1 -> parts[0].take(2).uppercase()
        else -> (parts[0].take(1) + parts[1].take(1)).uppercase()
    }
}

private val avatarPalette = listOf(
    Color(0xFF7C5CFF),
    Color(0xFF06D6A0),
    Color(0xFFE8B94A),
    Color(0xFF118AB2),
    Color(0xFFEF476F),
    Color(0xFF8CD3FF),
    Color(0xFFFF8FAB),
    Color(0xFF26A69A)
)

private fun colorForId(id: Int): Color =
    avatarPalette[((id % avatarPalette.size) + avatarPalette.size) % avatarPalette.size]

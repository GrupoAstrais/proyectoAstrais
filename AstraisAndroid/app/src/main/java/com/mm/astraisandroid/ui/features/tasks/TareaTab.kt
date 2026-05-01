package com.mm.astraisandroid.ui.features.tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mm.astraisandroid.ui.components.AstraisScreenHeader

@Composable
fun TasksTab(viewModel: TaskViewModel = hiltViewModel(), onTaskCompleted: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val rootTasks = remember(state.tasks) {
        state.tasks.filter { it.parentId == null }
    }

    val subtasksMap = remember(state.tasks) {
        state.tasks.filter { it.parentId != null }.groupBy { it.parentId }
    }

    var taskToEdit by remember { mutableStateOf<TaskUIModel?>(null) }

    LaunchedEffect(Unit) {
        val gid = state.personalGid
        if (gid != null) viewModel.loadTareas(gid)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            AstraisScreenHeader("Mis Tareas")

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.08f))
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TabButton("Pendientes", !state.isShowingCompleted, Modifier.weight(1f)) { viewModel.toggleShowingCompleted(false) }
                TabButton("Completadas", state.isShowingCompleted, Modifier.weight(1f)) { viewModel.toggleShowingCompleted(true) }
            }

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val categorias = listOf(
                    "ALL" to "Todas",
                    "UNICO" to "Únicas",
                    "HABITO" to "Hábitos",
                    "OBJETIVO" to "Objetivos"
                )
                items(categorias) { (key, label) ->
                    CategoryFilterChip(
                        text = label,
                        isSelected = state.selectedCategory == key,
                        onClick = { viewModel.setCategory(key) }
                    )
                }
            }
        }

        if (state.isLoading && rootTasks.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else if (rootTasks.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                    modifier = Modifier.size(80.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = if (!state.isShowingCompleted) "Todo al día" else "Aún no hay tareas",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = if (!state.isShowingCompleted) "No tienes tareas pendientes en esta categoría." else "No has completado tareas en esta categoría.",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Monospace,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp, vertical = 8.dp)
                )
            }
        } else {
            var expandedTaskId by remember { mutableStateOf<Int?>(null) }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 90.dp)
            ) {
                items(rootTasks, key = { it.id }) { task ->
                    val subtasks = subtasksMap[task.id] ?: emptyList()

                    TaskCard(
                        task = task,
                        subtasks = subtasks,
                        isExpanded = expandedTaskId == task.id,
                        onToggleExpand = {
                            expandedTaskId = if (expandedTaskId == task.id) null else task.id
                        },
                        onToggleComplete = { clickedTask ->
                            val gid = state.personalGid ?: -1
                            viewModel.toggleTaskCompletion(
                                tid = clickedTask.id,
                                gid = gid,
                                isCurrentlyCompleted = clickedTask.isCompleted
                            ) { onTaskCompleted() }
                        },
                        onAddSubtask = { viewModel.openCreateDialog(parentId = task.id) },
                        onEdit = { taskToEdit = task },
                        onDelete = {
                            val gid = state.personalGid ?: -1
                            viewModel.eliminarTarea(task.id, gid)
                        }
                    )
                }
            }
        }
    }

    taskToEdit?.let { task ->
        EditTaskDialog(
            task = task,
            onDismiss = { taskToEdit = null },
            onEdit = { titulo, desc, prio, fechaLimite, frecuencia ->
                val gid = state.personalGid ?: -1
                viewModel.editarTarea(task.id, gid, titulo, desc, prio, fechaLimite, frecuencia)
                taskToEdit = null
            }
        )
    }
}

@Composable
fun TabButton(text: String, isSelected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) Color.White.copy(alpha = 0.15f) else Color.Transparent)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isSelected) Color.White else Color.White.copy(alpha = 0.5f),
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
fun CategoryFilterChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(
                if (isSelected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimary
            else MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}
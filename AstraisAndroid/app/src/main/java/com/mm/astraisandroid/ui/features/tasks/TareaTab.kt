package com.mm.astraisandroid.ui.features.tasks

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CurrencyPound
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mm.astraisandroid.data.models.TaskPriority

@Composable
fun TasksTab(viewModel: TaskViewModel = hiltViewModel(), onTaskCompleted: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val rootTasks = state.tasks.filter { it.parentId == null }

    LaunchedEffect(Unit) {
        val gid = com.mm.astraisandroid.data.preferences.SessionManager.getPersonalGid()
        if (gid != null) viewModel.loadTareas(gid)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(text = "Mis Tareas", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)

            Row(
                modifier = Modifier.fillMaxWidth().height(44.dp).clip(RoundedCornerShape(12.dp)).background(Color.White.copy(alpha = 0.08f)).padding(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TabButton("Pendientes", !state.isShowingCompleted, Modifier.weight(1f)) { viewModel.toggleShowingCompleted(false) }
                TabButton("Completadas", state.isShowingCompleted, Modifier.weight(1f)) { viewModel.toggleShowingCompleted(true) }
            }

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val categorias = listOf("ALL" to "Todas", "UNICO" to "Únicas", "HABITO" to "Hábitos", "OBJETIVO" to "Objetivos")
                items(categorias) { (key, label) ->
                    CategoryChip(label, state.selectedCategory == key) { viewModel.setCategory(key) }
                }
            }
        }

        if (state.isLoading && state.tasks.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color.White)
            }
        } else if (state.tasks.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No hay tareas aquí", color = Color.White.copy(alpha = 0.3f), fontFamily = FontFamily.Monospace)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 90.dp)) {
                items(rootTasks, key = { it.id }) { task ->
                    val subtasks = state.tasks.filter { it.parentId == task.id }
                    TaskCard(
                        task = task,
                        subtasks = subtasks,
                        onComplete = {
                            val gid = com.mm.astraisandroid.data.preferences.SessionManager.getPersonalGid() ?: return@TaskCard
                            viewModel.completarTarea(task.id, gid) { onTaskCompleted() }
                        },
                        onAddSubtask = { viewModel.openCreateDialog(parentId = task.id) }
                    )
                }
            }
        }
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
fun CategoryChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (isSelected) Color.White else Color.White.copy(alpha = 0.05f))
            .border(1.dp, if (isSelected) Color.Transparent else Color.White.copy(alpha = 0.2f), RoundedCornerShape(50))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            color = if (isSelected) Color.Black else Color.White.copy(alpha = 0.7f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
fun TaskCard(task: TaskUIModel, subtasks: List<TaskUIModel> = emptyList(), onComplete: () -> Unit, onAddSubtask: () -> Unit = {}) {
    var expanded by remember { mutableStateOf(false) }

    val diffColor = when (task.priority) {
        TaskPriority.LOW -> Color(0xFF06D6A0)
        TaskPriority.MEDIUM -> Color(0xFFFFD166)
        TaskPriority.HIGH -> Color(0xFFEF476F)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.06f))
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
            .clickable { expanded = !expanded }
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = task.title,
                    color = if (task.isCompleted) Color.White.copy(alpha = 0.4f) else Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(diffColor.copy(alpha = 0.15f)).padding(horizontal = 6.dp, vertical = 4.dp)) {
                        Text(task.priority.name, color = diffColor, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }
                    Box(modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(Color(0xFFC172FF).copy(alpha = 0.15f)).padding(horizontal = 6.dp, vertical = 4.dp)) {
                        Text("+${task.xp} XP", color = Color(0xFFE2B3FF), fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }
                    if (task.ludiones > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(Color(0xFFFF9800).copy(alpha = 0.15f)).padding(horizontal = 6.dp, vertical = 4.dp)) {
                            Icon(Icons.Filled.CurrencyPound, contentDescription = null, tint = Color(0xFFFFB74D), modifier = Modifier.size(10.dp))
                            Spacer(Modifier.width(2.dp))
                            Text("${task.ludiones}", color = Color(0xFFFFB74D), fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            if (task.tipo != "OBJETIVO") {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (task.isCompleted) Color.White else Color.Transparent)
                        .border(2.dp, if (task.isCompleted) Color.Transparent else Color.White.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .clickable(enabled = !(task.tipo == "HABITO" && task.isCompleted)) { onComplete() },
                    contentAlignment = Alignment.Center
                ) {
                    if (task.isCompleted) {
                        Icon(Icons.Default.Check, contentDescription = "Completado", tint = Color.Black, modifier = Modifier.size(18.dp))
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF7EB8F7).copy(alpha = 0.2f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("OBJETIVO", color = Color(0xFF7EB8F7), fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
            }
        }

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(tween(300)),
            exit = shrinkVertically(tween(300))
        ) {
            Column(modifier = Modifier.padding(top = 12.dp)) {
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.1f)))
                Spacer(modifier = Modifier.height(10.dp))

                if (task.description.isNotBlank()) {
                    Text(text = task.description, color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                    Spacer(modifier = Modifier.height(12.dp))
                }
                if (task.tipo == "OBJETIVO") {
                    Text("SUBTAREAS (${subtasks.count { it.isCompleted }} / ${subtasks.size})", color = Color.White.copy(alpha=0.5f), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    Spacer(modifier = Modifier.height(8.dp))

                    subtasks.forEach { sub ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clip(RoundedCornerShape(8.dp)).background(Color.Black.copy(alpha=0.2f)).padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(sub.title, color = if(sub.isCompleted) Color.White.copy(alpha=0.3f) else Color.White, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                            if (sub.isCompleted) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color.Green.copy(alpha=0.6f), modifier = Modifier.size(14.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(Color.White.copy(alpha=0.1f)).clickable { onAddSubtask() }.padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("+ Añadir subtarea", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }
    }
}
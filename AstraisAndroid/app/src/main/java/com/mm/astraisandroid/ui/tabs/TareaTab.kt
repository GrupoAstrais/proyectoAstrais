package com.mm.astraisandroid.ui.tabs

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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mm.astraisandroid.TaskUIState
import com.mm.astraisandroid.TaskViewModel
import com.mm.astraisandroid.TokenHolder

enum class Difficulty { EASY, MEDIUM, HARD }

data class TaskUIModel(
    val id: Int,
    val title: String,
    val difficulty: Difficulty,
    val xp: Int,
    val tipo: String,
    var isCompleted: Boolean = false
)

@Composable
fun TasksTab(viewModel: TaskViewModel = viewModel(), onTaskCompleted: () -> Unit) {
    val isShowingCompleted by viewModel.isShowingCompleted.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val tareasFiltradas by viewModel.uiTasks.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        val gid = TokenHolder.getPersonalGid()
        if (gid != null) viewModel.loadTareas(gid)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                text = "Mis Tareas",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.08f))
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TabButton(
                    text = "Pendientes",
                    isSelected = !isShowingCompleted,
                    modifier = Modifier.weight(1f)
                ) { viewModel.toggleShowingCompleted(false) }

                TabButton(
                    text = "Completadas",
                    isSelected = isShowingCompleted,
                    modifier = Modifier.weight(1f)
                ) { viewModel.toggleShowingCompleted(true) }
            }

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                val categorias = listOf("ALL" to "Todas", "UNICO" to "Únicas", "HABITO" to "Hábitos", "OBJETIVO" to "Objetivos")
                items(categorias) { (key, label) ->
                    CategoryChip(
                        label = label,
                        isSelected = selectedCategory == key,
                        onClick = { viewModel.setCategory(key) }
                    )
                }
            }
        }

        when {
            uiState is TaskUIState.Loading && tareasFiltradas.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp)
                }
            }
            tareasFiltradas.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = if (isShowingCompleted) "No hay tareas completadas aún." else "No hay tareas.",
                        color = Color.White.copy(alpha = 0.3f),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 14.sp
                    )
                }
            }
            else -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 90.dp)
                ) {
                    items(tareasFiltradas, key = { it.id }) { task ->
                        TaskCard(
                            task = task,
                            onComplete = {
                                val gid = TokenHolder.getPersonalGid() ?: return@TaskCard
                                viewModel.completarTarea(task.id, gid) { onTaskCompleted() }
                            }
                        )
                    }
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
fun TaskCard(task: TaskUIModel, onComplete: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.06f))
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
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


                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.White.copy(alpha = 0.05f))
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = task.difficulty.name,
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFC172FF).copy(alpha = 0.15f))
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "+${task.xp} XP",
                        color = Color(0xFFE2B3FF),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(if (task.isCompleted) Color.White else Color.Transparent)
                .border(2.dp, if (task.isCompleted) Color.Transparent else Color.White.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                .clickable { onComplete() },
            contentAlignment = Alignment.Center
        ) {
            if (task.isCompleted) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Completado",
                    tint = Color.Black,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
package com.mm.astraisandroid.ui.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
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

data class SubTask(
    val id: Int,
    val title: String,
    var isCompleted: Boolean = false
)

sealed class TaskItem {
    data class Simple(
        val id: Int,
        val title: String,
        val difficulty: Difficulty,
        val xp: Int,
        var isCompleted: Boolean = false
    ) : TaskItem()

    data class Group(
        val id: Int,
        val title: String,
        val difficulty: Difficulty,
        val xp: Int,
        val subtasks: List<SubTask>
    ) : TaskItem()
}


private val sampleTasks = listOf(
    TaskItem.Simple(1, "TFG proyecto", Difficulty.HARD, 100),
    TaskItem.Simple(2, "Olimpiada", Difficulty.EASY, 20),
    TaskItem.Simple(3, "TFG proyecto", Difficulty.MEDIUM, 80),
    TaskItem.Group(
        id = 4,
        title = "TFG proyecto",
        difficulty = Difficulty.HARD,
        xp = 100,
        subtasks = listOf(
            SubTask(1, "Backend"),
            SubTask(2, "Frontend"),
            SubTask(3, "Backend")
        )
    ),
    TaskItem.Simple(5, "Otro", Difficulty.MEDIUM, 80)
)

private val categories = listOf("TAR", "OBJ", "HAB")
private val dateFilters = listOf("Today", "Tomorrow", "All")
private val taskTabs = listOf("Tareas", "Completadas", "Pendientes")


@Composable
fun TasksTab(viewModel: TaskViewModel = viewModel()) {
    var selectedDateFilter by remember { mutableIntStateOf(0) }
    var selectedCategory   by remember { mutableIntStateOf(0) }
    var selectedTab        by remember { mutableIntStateOf(0) }

    val tareas by viewModel.tareas.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Carga las tareas al entrar en la pantalla
    LaunchedEffect(Unit) {
        val gid = TokenHolder.getPersonalGid()
        if (gid != null) viewModel.loadTareas(gid)
    }

    // Mapea TareaResponse -> TaskItem.Simple filtrando por categoría y tab
    val tareasFiltradas = remember(tareas, selectedCategory, selectedTab) {
        tareas
            .filter { tarea ->
                val tipoFiltro = when (selectedCategory) {
                    0 -> "UNICO"
                    1 -> "OBJETIVO"
                    2 -> "HABITO"
                    else -> null
                }
                tipoFiltro == null || tarea.tipo == tipoFiltro
            }
            .filter { tarea ->
                when (selectedTab) {
                    0 -> tarea.estado == "ACTIVE"       // Tareas
                    1 -> tarea.estado == "COMPLETE"     // Completadas
                    2 -> tarea.estado == "DUE"          // Pendientes
                    else -> true
                }
            }
            .map { tarea ->
                TaskItem.Simple(
                    id          = tarea.id,
                    title       = tarea.titulo,
                    difficulty  = when (tarea.prioridad) {
                        0    -> Difficulty.EASY
                        1    -> Difficulty.MEDIUM
                        else -> Difficulty.HARD
                    },
                    xp          = tarea.recompensaXp,
                    isCompleted = tarea.estado == "COMPLETE"
                )
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        DateFilterRow(
            filters  = dateFilters,
            selected = selectedDateFilter,
            onSelect = { selectedDateFilter = it }
        )

        CategoryNavigator(
            categories = categories,
            selected   = selectedCategory,
            onPrevious = { if (selectedCategory > 0) selectedCategory-- },
            onNext     = { if (selectedCategory < categories.lastIndex) selectedCategory++ }
        )

        TaskTabRow(
            tabs     = taskTabs,
            selected = selectedTab,
            onSelect = { selectedTab = it }
        )

        when {
            uiState is TaskUIState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp)
                }
            }
            uiState is TaskUIState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = (uiState as TaskUIState.Error).message,
                        color = Color.White.copy(alpha = 0.4f),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp
                    )
                }
            }
            tareasFiltradas.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Sin tareas aquí",
                        color = Color.White.copy(alpha = 0.3f),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp
                    )
                }
            }
            else -> {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(tareasFiltradas, key = { it.id }) { task ->
                        SimpleTaskCard(
                            task = task,
                            onComplete = {
                                val gid = TokenHolder.getPersonalGid() ?: return@SimpleTaskCard
                                viewModel.completarTarea(task.id, gid)
                            }
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun DateFilterRow(
    filters: List<String>,
    selected: Int,
    onSelect: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        filters.forEachIndexed { index, label ->
            val isSelected = selected == index
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(
                        if (isSelected) Color.White else Color.Transparent
                    )
                    .border(1.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(50))
                    .clickable { onSelect(index) }
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = label,
                    color = if (isSelected) Color.Black else Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .border(1.dp, Color.White.copy(alpha = 0.4f), CircleShape)
                .clickable { },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add task",
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}


@Composable
fun CategoryNavigator(
    categories: List<String>,
    selected: Int,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (selected > 0) {
            Text(
                text = categories[selected - 1],
                color = Color.White.copy(alpha = 0.3f),
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.width(4.dp))
        }

        IconButton(onClick = onPrevious, enabled = selected > 0) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowLeft,
                contentDescription = "Previous",
                tint = if (selected > 0) Color.White else Color.White.copy(alpha = 0.2f)
            )
        }

        Text(
            text = categories[selected],
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 2.sp
        )

        IconButton(onClick = onNext, enabled = selected < categories.lastIndex) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "Next",
                tint = if (selected < categories.lastIndex) Color.White else Color.White.copy(alpha = 0.2f)
            )
        }

        if (selected < categories.lastIndex) {
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = categories[selected + 1],
                color = Color.White.copy(alpha = 0.3f),
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
fun TaskTabRow(
    tabs: List<String>,
    selected: Int,
    onSelect: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        tabs.forEachIndexed { index, label ->
            val isSelected = selected == index
            Text(
                text = label,
                color = if (isSelected) Color.White else Color.White.copy(alpha = 0.4f),
                fontSize = if (isSelected) 15.sp else 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.clickable { onSelect(index) }
            )
        }
    }
}


@Composable
fun SimpleTaskCard(
    task: TaskItem.Simple,
    onComplete: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = 0.07f))
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(14.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = task.title,
                color = if (task.isCompleted) Color.White.copy(alpha = 0.4f) else Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DifficultyChip(difficulty = task.difficulty)
                XpChip(xp = task.xp)
            }
        }

        TaskCheckbox(
            checked = task.isCompleted,
            onCheckedChange = { if (it) onComplete() }
        )
    }
}

@Composable
fun GroupTaskCard(task: TaskItem.Group) {
    val subtaskStates = remember { task.subtasks.map { mutableStateOf(it.isCompleted) } }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = 0.07f))
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(14.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = task.title,
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                DifficultyChip(difficulty = task.difficulty)
                XpChip(xp = task.xp)
            }
        }

        Divider(color = Color.White.copy(alpha = 0.08f), thickness = 1.dp)

        task.subtasks.forEachIndexed { index, subtask ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = subtask.title,
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Monospace
                )
                TaskCheckbox(
                    checked = subtaskStates[index].value,
                    onCheckedChange = { subtaskStates[index].value = it }
                )
            }

            if (index < task.subtasks.lastIndex) {
                Divider(color = Color.White.copy(alpha = 0.05f), thickness = 1.dp)
            }
        }
    }
}

@Composable
fun DifficultyChip(difficulty: Difficulty) {
    val label = difficulty.name
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color.White.copy(alpha = 0.12f))
            .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = label,
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp
        )
    }
}

@Composable
fun XpChip(xp: Int) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color.White.copy(alpha = 0.08f))
            .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = "+$xp XP",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
fun TaskCheckbox(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(if (checked) Color.White.copy(alpha = 0.9f) else Color.Transparent)
            .border(1.5.dp, Color.White.copy(alpha = if (checked) 0f else 0.4f), RoundedCornerShape(6.dp))
            .clickable { onCheckedChange(!checked) },
        contentAlignment = Alignment.Center
    ) {
        if (checked) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = Color.Black,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
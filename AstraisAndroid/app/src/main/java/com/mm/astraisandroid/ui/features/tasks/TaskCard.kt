@file:OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)


package com.mm.astraisandroid.ui.features.tasks

import com.mm.astraisandroid.R

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.mm.astraisandroid.ui.theme.Gray300
import com.mm.astraisandroid.ui.theme.Gray700

@Composable
fun TaskCard(
    task: TaskUIModel,
    subtasks: List<TaskUIModel> = emptyList(),
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onToggleComplete: (TaskUIModel) -> Unit,
    onAddSubtask: () -> Unit = {},
    onEditSubtask: (TaskUIModel) -> Unit = {},
    onDeleteSubtask: (TaskUIModel) -> Unit = {},
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val isObjective = task.tipo == "OBJETIVO"
    val isHabit = task.tipo == "HABITO"
    val contentAlpha = if (task.isCompleted) 0.5f else 1f

    val completedCount = subtasks.count { it.isCompleted }
    val totalCount = subtasks.size
    val progressFraction = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f
    val animatedProgress by animateFloatAsState(targetValue = progressFraction, label = "prog")

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) { onDelete(); false } else false
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            val errorColor = MaterialTheme.colorScheme.error

            val isSwipingToDismiss = dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart
            val progress = if (isSwipingToDismiss) dismissState.progress else 0f

            val actionAlpha = when {
                progress < 0.15f -> 0f
                progress > 0.5f -> 1f
                else -> (progress - 0.15f) / (0.5f - 0.15f)
            }

            val animatedActionAlpha by animateFloatAsState(
                targetValue = actionAlpha,
                animationSpec = tween(durationMillis = 100),
                label = "actionAlpha"
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                errorColor.copy(alpha = 0.25f * animatedActionAlpha)
                            )
                        )
                    )
                    .border(
                        width = 1.dp,
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                errorColor.copy(alpha = 0.4f * animatedActionAlpha)
                            )
                        ),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.cd_delete_task),
                    tint = errorColor.copy(alpha = animatedActionAlpha),
                    modifier = Modifier.alpha(animatedActionAlpha)
                )
            }
        },
        content = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(24.dp),
                        ambientColor = Color.Black.copy(alpha = 0.3f),
                        spotColor = Color.White.copy(alpha = 0.05f)
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
                    .clickable { onToggleExpand() }
                    .animateContentSize()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    GlassTaskTypeIcon(task.tipo, contentAlpha)

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = task.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = contentAlpha),
                                textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            if (isObjective) {
                                Icon(
                                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = Gray300.copy(alpha = 0.5f)
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(26.dp)
                                        .clip(CircleShape)
                                        .background(if (task.isCompleted) MaterialTheme.colorScheme.primary else Color.Transparent)
                                        .border(
                                            1.5.dp,
                                            if (task.isCompleted) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.3f),
                                            CircleShape
                                        )
                                        .clickable { onToggleComplete(task) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (task.isCompleted) {
                                        Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }

                        if (task.description.isNotBlank()) {
                            Text(
                                text = task.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.6f * contentAlpha),
                                maxLines = if (isExpanded) 5 else 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val priorityLabel = when(task.priority) {
                        com.mm.astraisandroid.data.models.TaskPriority.LOW -> "Baja"
                        com.mm.astraisandroid.data.models.TaskPriority.MEDIUM -> "Media"
                        com.mm.astraisandroid.data.models.TaskPriority.HIGH -> "Alta"
                    }
                    GlassTaskMetadataChip(priorityLabel, Icons.Default.Flag, MaterialTheme.colorScheme.tertiary)

                    GlassTaskMetadataChip(stringResource(R.string.task_xp_reward, task.xp), Icons.Default.Star, MaterialTheme.colorScheme.primary)

                    if (task.ludiones > 0) {
                        GlassTaskMetadataChip("${task.ludiones}", Icons.Default.CurrencyPound, MaterialTheme.colorScheme.tertiary)
                    }

                    if (isHabit && !task.habitFrequency.isNullOrBlank()) {
                        val freqLabel = when(task.habitFrequency) {
                            "HOURLY" -> "Cada hora"
                            "DAILY" -> "Diario"
                            "WEEKLY" -> "Semanal"
                            "MONTHLY" -> "Mensual"
                            "YEARLY" -> "Anual"
                            else -> task.habitFrequency
                        }
                        GlassTaskMetadataChip(freqLabel, Icons.Default.Repeat, MaterialTheme.colorScheme.secondary)
                    }

                    if (!isHabit && !task.dueDate.isNullOrBlank()) {
                        val dateLabel = task.dueDate.split("T").firstOrNull() ?: ""
                        GlassTaskMetadataChip(dateLabel, Icons.Default.Event, MaterialTheme.colorScheme.error)
                    }
                }

                if (isObjective && totalCount > 0) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(
                                stringResource(R.string.task_subtasks_progress, completedCount, totalCount),
                                style = MaterialTheme.typography.labelSmall,
                                color = Gray300.copy(alpha = 0.5f)
                            )
                            Text(
                                stringResource(R.string.task_progress_percent, (progressFraction * 100).toInt()),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        LinearProgressIndicator(
                            progress = { animatedProgress },
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = Color.White.copy(alpha = 0.08f)
                        )
                    }
                }

                AnimatedVisibility(visible = isExpanded) {
                    Column(modifier = Modifier.padding(top = 20.dp)) {
                        if (isObjective) {
                            GlassSubtaskHeader(onAddSubtask)
                            Spacer(Modifier.height(10.dp))
                            subtasks.forEach { sub ->
                                GlassSubtaskRow(sub, onToggleComplete, onEditSubtask, onDeleteSubtask)
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(onClick = onEdit) {
                                Icon(Icons.Default.Edit, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.secondary)
                                Spacer(Modifier.width(6.dp))
                                Text(stringResource(R.string.task_edit_button), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.secondary)
                            }
                        }
                    }
                }
            }
        }
    )
}

@Composable
private fun GlassTaskTypeIcon(tipo: String, alpha: Float) {
    val (icon, color) = when(tipo) {
        "OBJETIVO" -> Icons.Default.Flag to MaterialTheme.colorScheme.tertiary
        "HABITO" -> Icons.Default.Repeat to MaterialTheme.colorScheme.secondary
        else -> Icons.Default.Task to MaterialTheme.colorScheme.primary
    }
    Box(
        modifier = Modifier
            .size(46.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(color.copy(alpha = 0.12f * alpha))
            .border(1.dp, color.copy(alpha = 0.35f * alpha), RoundedCornerShape(14.dp)),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, null, tint = color.copy(alpha = alpha), modifier = Modifier.size(24.dp))
    }
}

@Composable
private fun GlassTaskMetadataChip(text: String, icon: ImageVector, color: Color) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(color.copy(alpha = 0.1f))
            .border(1.dp, color.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(icon, null, tint = color.copy(alpha = 0.9f), modifier = Modifier.size(14.dp))
        Text(text, fontSize = 12.sp, color = color.copy(alpha = 0.9f), fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun GlassSubtaskHeader(onAdd: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            stringResource(R.string.task_subtasks_header),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = Gray300.copy(alpha = 0.5f)
        )
        IconButton(onClick = onAdd, modifier = Modifier.size(28.dp)) {
            Icon(Icons.Default.AddCircle, null, tint = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun GlassSubtaskRow(
    sub: TaskUIModel,
    onToggle: (TaskUIModel) -> Unit,
    onEdit: (TaskUIModel) -> Unit,
    onDelete: (TaskUIModel) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.04f))
            .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(12.dp))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = sub.isCompleted,
            onCheckedChange = { onToggle(sub) },
            modifier = Modifier.size(24.dp),
            colors = CheckboxDefaults.colors(
                checkedColor = MaterialTheme.colorScheme.primary,
                uncheckedColor = Color.White.copy(alpha = 0.3f)
            )
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = sub.title,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = if (sub.isCompleted) 0.5f else 0.9f),
                textDecoration = if (sub.isCompleted) TextDecoration.LineThrough else null
            )
            if (!sub.dueDate.isNullOrBlank()) {
                val dateLabel = sub.dueDate.split("T").firstOrNull() ?: ""
                Text(
                    text = dateLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.4f)
                )
            }
        }
        IconButton(onClick = { onEdit(sub) }, modifier = Modifier.size(28.dp)) {
            Icon(Icons.Default.Edit, null, modifier = Modifier.size(16.dp), tint = Gray300.copy(alpha = 0.5f))
        }
        IconButton(onClick = { onDelete(sub) }, modifier = Modifier.size(28.dp)) {
            Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f))
        }
    }
}

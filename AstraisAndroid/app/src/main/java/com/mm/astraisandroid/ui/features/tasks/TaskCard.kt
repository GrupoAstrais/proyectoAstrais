@file:OptIn(ExperimentalLayoutApi::class)

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mm.astraisandroid.data.models.TaskPriority
import com.mm.astraisandroid.ui.features.tasks.TaskUIModel

@Composable
fun TaskCard(
    task: TaskUIModel,
    subtasks: List<TaskUIModel> = emptyList(),
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onToggleComplete: (TaskUIModel) -> Unit,
    onAddSubtask: () -> Unit = {},
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val isObjective = task.tipo == "OBJETIVO"
    val contentAlpha = if (task.isCompleted) 0.4f else 1f

    val cardBg = Color(0xFF1E1E2E).copy(alpha = if (task.isCompleted) 0.3f else 0.6f)
    val cardBorder = Color.White.copy(alpha = 0.08f)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(cardBg)
            .border(1.dp, cardBorder, RoundedCornerShape(20.dp))
            .clickable { onToggleExpand() }
            .animateContentSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = task.title,
                    color = Color.White.copy(alpha = contentAlpha),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                )

                if (task.description.isNotBlank()) {
                    Text(
                        text = task.description,
                        color = Color.White.copy(alpha = contentAlpha * 0.6f),
                        fontSize = 14.sp,
                        maxLines = if (isExpanded) Int.MAX_VALUE else 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 20.sp
                    )
                }

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    if (isObjective) {
                        ModernChip(text = "Objetivo", icon = Icons.Default.Flag, color = MaterialTheme.colorScheme.primary, alpha = contentAlpha)
                    } else {
                        val priorityColor = when (task.priority) {
                            TaskPriority.LOW -> Color(0xFF81C784)
                            TaskPriority.MEDIUM -> Color(0xFFFFD166)
                            TaskPriority.HIGH -> Color(0xFFE57373)
                        }
                        val priorityIcon = when (task.priority) {
                            TaskPriority.LOW -> Icons.Default.KeyboardArrowDown
                            TaskPriority.MEDIUM -> Icons.Default.Remove
                            TaskPriority.HIGH -> Icons.Default.KeyboardArrowUp
                        }
                        ModernChip(
                            text = task.priority.name.lowercase().replaceFirstChar { it.uppercase() },
                            icon = priorityIcon,
                            color = priorityColor,
                            alpha = contentAlpha
                        )
                    }

                    ModernChip(text = "${task.xp} XP", icon = Icons.Default.Star, color = Color(0xFF64B5F6), alpha = contentAlpha)

                    if (task.ludiones > 0) {
                        ModernChip(text = "${task.ludiones}", icon = Icons.Default.CurrencyPound, color = Color(0xFFFFB74D), alpha = contentAlpha)
                    }
                }
            }

            if (!isObjective) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(if (task.isCompleted) MaterialTheme.colorScheme.primary else Color.Transparent)
                        .border(
                            2.dp,
                            if (task.isCompleted) Color.Transparent else Color.White.copy(alpha = 0.3f),
                            CircleShape
                        )
                        .clickable { onToggleComplete(task) },
                    contentAlignment = Alignment.Center
                ) {
                    if (task.isCompleted) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }
            } else {
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = "Expandir",
                    tint = Color.White.copy(alpha = 0.4f),
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(tween(300)),
            exit = shrinkVertically(tween(300))
        ) {
            Column(modifier = Modifier.padding(top = 16.dp)) {
                HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                Spacer(modifier = Modifier.height(16.dp))

                if (isObjective) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Subtareas (${subtasks.count { it.isCompleted }}/${subtasks.size})",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        TextButton(
                            onClick = onAddSubtask,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Añadir", color = MaterialTheme.colorScheme.primary, fontSize = 13.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    subtasks.forEach { sub ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onToggleComplete(sub) }
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(if (sub.isCompleted) MaterialTheme.colorScheme.primary else Color.Transparent)
                                    .border(2.dp, if (sub.isCompleted) Color.Transparent else Color.White.copy(alpha = 0.2f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                if (sub.isCompleted) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = sub.title,
                                color = Color.White.copy(alpha = if (sub.isCompleted) 0.4f else 0.8f),
                                fontSize = 14.sp,
                                textDecoration = if (sub.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar", tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.size(20.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color(0xFFE57373).copy(alpha = 0.7f), modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun ModernChip(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    color: Color,
    alpha: Float = 1f
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(CircleShape)
            .background(color.copy(alpha = 0.1f * alpha))
            .border(1.dp, color.copy(alpha = 0.2f * alpha), CircleShape)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = color.copy(alpha = alpha), modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(4.dp))
        }
        Text(
            text = text,
            color = color.copy(alpha = alpha),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
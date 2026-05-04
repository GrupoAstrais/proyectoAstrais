package com.mm.astraisandroid.ui.features.tasks


import com.mm.astraisandroid.R
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.res.stringResource
import com.mm.astraisandroid.data.models.TaskPriority
import com.mm.astraisandroid.ui.theme.Gray300
import com.mm.astraisandroid.ui.theme.Gray700
import com.mm.astraisandroid.ui.theme.Primary
import com.mm.astraisandroid.ui.theme.Surface
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTaskDialog(
    task: TaskUIModel,
    onDismiss: () -> Unit,
    onEdit: (titulo: String, descripcion: String, prioridad: TaskPriority, fechaLimite: String?, frecuencia: String?) -> Unit
) {
    var titulo by remember { mutableStateOf(task.title) }
    var desc by remember { mutableStateOf(task.description) }
    var prioridadInt by remember { mutableIntStateOf(task.priority.ordinal) }

    var frecuencia by remember { mutableStateOf(task.habitFrequency ?: "DAILY") }
    var fechaLimite by remember { mutableStateOf(task.dueDate?.split("T")?.firstOrNull() ?: "") }
    var showDatePicker by remember { mutableStateOf(false) }

    val prioridades = listOf("BAJA", "MEDIA", "ALTA")
    val frecuencias = listOf("DAILY" to "Diario", "WEEKLY" to "Semanal", "MONTHLY" to "Mensual")

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 600.dp)
                .shadow(
                    elevation = 16.dp,
                    shape = RoundedCornerShape(28.dp),
                    ambientColor = Color.Black.copy(alpha = 0.4f),
                    spotColor = Primary.copy(alpha = 0.1f)
                )
                .clip(RoundedCornerShape(28.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Surface.copy(alpha = 0.92f),
                            Surface.copy(alpha = 0.85f)
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
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
        ) {
            Text(
                text = stringResource(R.string.task_edit_title),
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(20.dp))

            GlassTextFieldSection(
                label = stringResource(R.string.task_label_title),
                value = titulo,
                onValueChange = { titulo = it },
                placeholder = stringResource(R.string.task_placeholder_title),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            GlassTextFieldSection(
                label = stringResource(R.string.task_label_description),
                value = desc,
                onValueChange = { desc = it },
                placeholder = stringResource(R.string.task_placeholder_description),
                singleLine = false,
                minHeight = 60.dp
            )

            Spacer(modifier = Modifier.height(16.dp))

            GlassToggleSection(
                label = stringResource(R.string.task_label_priority),
                options = prioridades,
                selectedIndex = prioridadInt,
                onSelected = { prioridadInt = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            AnimatedVisibility(visible = task.tipo == "HABITO") {
                Column {
                    GlassToggleSection(
                        label = "NUEVA FRECUENCIA",
                        options = frecuencias.map { it.second },
                        selectedIndex = frecuencias.indexOfFirst { it.first == frecuencia }.takeIf { it >= 0 } ?: 0,
                        onSelected = { index -> frecuencia = frecuencias[index].first }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            AnimatedVisibility(visible = task.tipo == "UNICO") {
                Column {
                    Text(stringResource(R.string.task_label_new_due_date_optional), color = Gray300.copy(alpha = 0.6f), fontSize = 10.sp, letterSpacing = 1.5.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.04f))
                            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                            .clickable { showDatePicker = true }
                            .padding(horizontal = 14.dp, vertical = 14.dp)
                    ) {
                        Text(
                            text = if (fechaLimite.isEmpty()) stringResource(R.string.task_keep_unchanged) else fechaLimite,
                            color = if (fechaLimite.isEmpty()) Gray300.copy(alpha = 0.3f) else Color.White,
                            fontSize = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
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
                    Text(stringResource(R.string.dialog_cancel), color = Gray300, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (titulo.isNotBlank()) Primary else Gray700)
                        .shadow(
                            elevation = if (titulo.isNotBlank()) 8.dp else 0.dp,
                            shape = RoundedCornerShape(12.dp),
                            ambientColor = if (titulo.isNotBlank()) Primary.copy(alpha = 0.3f) else Color.Transparent,
                            spotColor = if (titulo.isNotBlank()) Color.White.copy(alpha = 0.1f) else Color.Transparent
                        )
                        .clickable(enabled = titulo.isNotBlank()) {
                            val prio = TaskPriority.entries.getOrElse(prioridadInt) { TaskPriority.LOW }
                            val freqFinal = if (task.tipo == "HABITO") frecuencia else null
                            val fechaFinal = if (task.tipo == "UNICO" && fechaLimite.isNotBlank()) "${fechaLimite}T23:59:59Z" else null
                            onEdit(titulo, desc, prio, fechaFinal, freqFinal)
                        }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(stringResource(R.string.task_save_button), color = if (titulo.isNotBlank()) Color.White else Gray300.copy(alpha = 0.4f), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        fechaLimite = formatter.format(Date(millis))
                    }
                    showDatePicker = false
                }) { Text(stringResource(R.string.dialog_accept)) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text(stringResource(R.string.dialog_cancel)) }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
internal fun GlassTextFieldSection(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    singleLine: Boolean,
    minHeight: androidx.compose.ui.unit.Dp? = null
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, color = Gray300.copy(alpha = 0.6f), fontSize = 10.sp, letterSpacing = 1.5.sp, fontWeight = FontWeight.SemiBold)
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .let { if (minHeight != null) it.heightIn(min = minHeight) else it }
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(alpha = 0.04f))
                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                .padding(horizontal = 14.dp, vertical = 14.dp),
            textStyle = TextStyle(color = Color.White, fontSize = 14.sp),
            singleLine = singleLine,
            decorationBox = { inner ->
                if (value.isEmpty()) {
                    Text(placeholder, color = Gray300.copy(alpha = 0.3f), fontSize = 14.sp)
                }
                inner()
            }
        )
    }
}

@Composable
internal fun GlassToggleSection(
    label: String,
    options: List<String>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, color = Gray300.copy(alpha = 0.6f), fontSize = 10.sp, letterSpacing = 1.5.sp, fontWeight = FontWeight.SemiBold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            options.forEachIndexed { index, option ->
                val isSelected = selectedIndex == index
                GlassToggleButton(
                    text = option,
                    isSelected = isSelected,
                    modifier = Modifier.weight(1f),
                    onClick = { onSelected(index) }
                )
            }
        }
    }
}

@Composable
internal fun GlassToggleButton(text: String, isSelected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val animatedBg by animateColorAsState(
        targetValue = if (isSelected) Primary.copy(alpha = 0.2f) else Color.Transparent,
        label = "toggleBg"
    )
    val animatedBorder by animateColorAsState(
        targetValue = if (isSelected) Primary.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.1f),
        label = "toggleBorder"
    )
    val animatedText by animateColorAsState(
        targetValue = if (isSelected) Color.White else Gray300.copy(alpha = 0.5f),
        label = "toggleText"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(animatedBg)
            .border(1.dp, animatedBorder, RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = animatedText,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

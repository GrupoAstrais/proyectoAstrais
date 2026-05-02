package com.mm.astraisandroid.ui.features.tasks

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.mm.astraisandroid.data.models.TaskPriority
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Diálogo de edición de tareas.
 *
 * Carga los datos actuales de la tarea proporcionada y permite modificarlos.
 * Solo muestra los campos relevantes según el tipo de tarea.
 *
 * @param task Modelo de UI de la tarea que se va a editar.
 * @param onDismiss Callback invocado al cerrar el diálogo sin guardar cambios.
 * @param onEdit Callback invocado al confirmar con los nuevos valores del formulario.
 */
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
    var fechaLimite by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }

    val prioridades = listOf("BAJA", "MEDIA", "ALTA")
    val frecuencias = listOf("DAILY" to "Diario", "WEEKLY" to "Semanal", "MONTHLY" to "Mensual")

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 600.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF1A1A2E))
                .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(20.dp))
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
        ) {
            Text(
                text = "Editar Tarea",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace
            )

            Spacer(modifier = Modifier.height(16.dp))

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("TÍTULO", color = Color.White.copy(alpha = 0.4f), fontSize = 10.sp, fontFamily = FontFamily.Monospace, letterSpacing = 1.5.sp)
                BasicTextField(
                    value = titulo,
                    onValueChange = { titulo = it },
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(Color.White.copy(alpha = 0.07f)).border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(10.dp)).padding(horizontal = 14.dp, vertical = 10.dp),
                    textStyle = TextStyle(color = Color.White, fontSize = 13.sp, fontFamily = FontFamily.Monospace)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("DESCRIPCIÓN", color = Color.White.copy(alpha = 0.4f), fontSize = 10.sp, fontFamily = FontFamily.Monospace, letterSpacing = 1.5.sp)
                BasicTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 60.dp, max = 150.dp).clip(RoundedCornerShape(10.dp)).background(Color.White.copy(alpha = 0.07f)).border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(10.dp)).padding(horizontal = 14.dp, vertical = 10.dp),
                    textStyle = TextStyle(color = Color.White, fontSize = 13.sp, fontFamily = FontFamily.Monospace)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("PRIORIDAD", color = Color.White.copy(alpha = 0.4f), fontSize = 10.sp, fontFamily = FontFamily.Monospace, letterSpacing = 1.5.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    prioridades.forEachIndexed { index, p ->
                        val isSelected = prioridadInt == index
                        Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(8.dp)).background(if (isSelected) Color.White.copy(alpha = 0.15f) else Color.Transparent).border(1.dp, if (isSelected) Color.White.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp)).clickable { prioridadInt = index }.padding(vertical = 7.dp), contentAlignment = Alignment.Center) {
                            Text(text = p, color = if (isSelected) Color.White else Color.White.copy(alpha = 0.4f), fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            AnimatedVisibility(visible = task.tipo == "HABITO") {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(bottom = 12.dp)) {
                    Text("NUEVA FRECUENCIA", color = Color.White.copy(alpha = 0.4f), fontSize = 10.sp, fontFamily = FontFamily.Monospace, letterSpacing = 1.5.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        frecuencias.forEach { (clave, etiqueta) ->
                            val isSelected = frecuencia == clave
                            Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(8.dp)).background(if (isSelected) Color.White.copy(alpha = 0.15f) else Color.Transparent).border(1.dp, if (isSelected) Color.White.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp)).clickable { frecuencia = clave }.padding(vertical = 7.dp), contentAlignment = Alignment.Center) {
                                Text(text = etiqueta, color = if (isSelected) Color.White else Color.White.copy(alpha = 0.4f), fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }
                }
            }

            AnimatedVisibility(visible = task.tipo == "UNICO") {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(bottom = 12.dp)) {
                    Text("NUEVA FECHA LÍMITE (Opcional)", color = Color.White.copy(alpha = 0.4f), fontSize = 10.sp, fontFamily = FontFamily.Monospace, letterSpacing = 1.5.sp)
                    Box(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(Color.White.copy(alpha = 0.07f)).border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(10.dp)).clickable { showDatePicker = true }.padding(horizontal = 14.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = if (fechaLimite.isEmpty()) "Dejar sin cambios..." else fechaLimite,
                            color = if (fechaLimite.isEmpty()) Color.White.copy(alpha = 0.2f) else Color.White,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(10.dp)).background(Color.White.copy(alpha = 0.06f)).clickable { onDismiss() }.padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                    Text("Cancelar", color = Color.White.copy(alpha = 0.5f), fontSize = 13.sp, fontFamily = FontFamily.Monospace)
                }
                Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(10.dp)).background(if (titulo.isNotBlank()) Color.White else Color.White.copy(alpha = 0.1f))
                    .clickable(enabled = titulo.isNotBlank()) {
                        val prio = TaskPriority.entries.getOrElse(prioridadInt) { TaskPriority.LOW }
                        val freqFinal = if (task.tipo == "HABITO") frecuencia else null
                        val fechaFinal = if (task.tipo == "UNICO" && fechaLimite.isNotBlank()) "${fechaLimite}T23:59:59Z" else null

                        onEdit(titulo, desc, prio, fechaFinal, freqFinal)
                    }
                    .padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                    Text("Guardar", color = if (titulo.isNotBlank()) Color.Black else Color.White.copy(alpha = 0.3f), fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
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
                }) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
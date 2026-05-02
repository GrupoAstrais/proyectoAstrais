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
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Diálogo de creación de tareas para Astrais.
 *
 * Permite al usuario configurar una nueva tarea o subtarea indicando título,
 * descripción, tipo (única, hábito u objetivo), prioridad, y datos adicionales
 * según el tipo seleccionado (frecuencia para hábitos, fecha límite para únicas).
 *
 * @param parentId Identificador del objetivo padre si se crea una subtarea; `null` para tareas padre.
 * @param onDismiss Callback invocado al cerrar el diálogo sin crear la tarea.
 * @param onCreate Callback invocado al confirmar la creación con los datos introducidos por el usuario.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTareaDialog(
    parentId: Int? = null,
    onDismiss: () -> Unit,
    onCreate: (titulo: String, desc: String, tipo: String, prioridad: Int, frecuencia: String?, fechaLimite: String?) -> Unit
) {
    var fechaLimite by remember { mutableStateOf("") }
    var titulo by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }

    var tipo by remember { mutableStateOf(if (parentId != null) "UNICO" else "UNICO") }

    var prioridad by remember { mutableStateOf(0) }
    var frecuencia by remember { mutableStateOf("DAILY") }
    var showDatePicker by remember { mutableStateOf(false) }

    val tipos = listOf("UNICO", "OBJETIVO", "HABITO")
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
                text = if (parentId != null) "Nueva subtarea" else "Nueva tarea",
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
                    textStyle = TextStyle(color = Color.White, fontSize = 13.sp, fontFamily = FontFamily.Monospace),
                    decorationBox = { inner -> if (titulo.isEmpty()) Text("Nombre de la tarea...", color = Color.White.copy(alpha = 0.2f), fontSize = 13.sp, fontFamily = FontFamily.Monospace); inner() }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("DESCRIPCIÓN", color = Color.White.copy(alpha = 0.4f), fontSize = 10.sp, fontFamily = FontFamily.Monospace, letterSpacing = 1.5.sp)
                BasicTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 60.dp, max = 150.dp).clip(RoundedCornerShape(10.dp)).background(Color.White.copy(alpha = 0.07f)).border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(10.dp)).padding(horizontal = 14.dp, vertical = 10.dp),
                    textStyle = TextStyle(color = Color.White, fontSize = 13.sp, fontFamily = FontFamily.Monospace),
                    decorationBox = { inner -> if (desc.isEmpty()) Text("Detalles adicionales...", color = Color.White.copy(alpha = 0.2f), fontSize = 13.sp, fontFamily = FontFamily.Monospace); inner() }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            AnimatedVisibility(visible = parentId == null) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(bottom = 12.dp)) {
                    Text("TIPO", color = Color.White.copy(alpha = 0.4f), fontSize = 10.sp, fontFamily = FontFamily.Monospace, letterSpacing = 1.5.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        tipos.forEach { t ->
                            val isSelected = tipo == t
                            Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(8.dp)).background(if (isSelected) Color.White.copy(alpha = 0.15f) else Color.Transparent).border(1.dp, if (isSelected) Color.White.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp)).clickable { tipo = t }.padding(vertical = 7.dp), contentAlignment = Alignment.Center) {
                                Text(text = t, color = if (isSelected) Color.White else Color.White.copy(alpha = 0.4f), fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("PRIORIDAD", color = Color.White.copy(alpha = 0.4f), fontSize = 10.sp, fontFamily = FontFamily.Monospace, letterSpacing = 1.5.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    prioridades.forEachIndexed { index, p ->
                        val isSelected = prioridad == index
                        Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(8.dp)).background(if (isSelected) Color.White.copy(alpha = 0.15f) else Color.Transparent).border(1.dp, if (isSelected) Color.White.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp)).clickable { prioridad = index }.padding(vertical = 7.dp), contentAlignment = Alignment.Center) {
                            Text(text = p, color = if (isSelected) Color.White else Color.White.copy(alpha = 0.4f), fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            AnimatedVisibility(visible = tipo == "HABITO") {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(bottom = 12.dp)) {
                    Text("FRECUENCIA", color = Color.White.copy(alpha = 0.4f), fontSize = 10.sp, fontFamily = FontFamily.Monospace, letterSpacing = 1.5.sp)
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

            AnimatedVisibility(visible = tipo == "UNICO") {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(bottom = 12.dp)) {
                    Text("FECHA LÍMITE", color = Color.White.copy(alpha = 0.4f), fontSize = 10.sp, fontFamily = FontFamily.Monospace, letterSpacing = 1.5.sp)
                    Box(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(Color.White.copy(alpha = 0.07f)).border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(10.dp)).clickable { showDatePicker = true }.padding(horizontal = 14.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = if (fechaLimite.isEmpty()) "Seleccionar fecha..." else fechaLimite,
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
                        onCreate(titulo, desc, tipo, prioridad, if (tipo == "HABITO") frecuencia else null, if (tipo == "UNICO") fechaLimite else null)
                    }
                    .padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                    Text("Crear", color = if (titulo.isNotBlank()) Color.Black else Color.White.copy(alpha = 0.3f), fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
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
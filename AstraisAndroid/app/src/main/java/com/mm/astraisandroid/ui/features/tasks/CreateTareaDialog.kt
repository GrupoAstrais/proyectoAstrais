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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.res.stringResource
import com.mm.astraisandroid.ui.theme.Gray300
import com.mm.astraisandroid.ui.theme.Gray700
import com.mm.astraisandroid.ui.theme.Primary
import com.mm.astraisandroid.ui.theme.Surface
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
                text = if (parentId != null) stringResource(R.string.task_new_subtask_title) else stringResource(R.string.task_new_task_title),
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

            AnimatedVisibility(visible = parentId == null) {
                Column {
                    GlassToggleSection(
                        label = stringResource(R.string.task_label_type),
                        options = tipos,
                        selectedIndex = tipos.indexOf(tipo).takeIf { it >= 0 } ?: 0,
                        onSelected = { index -> tipo = tipos[index] }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            GlassToggleSection(
                label = stringResource(R.string.task_label_priority),
                options = prioridades,
                selectedIndex = prioridad,
                onSelected = { index -> prioridad = index }
            )

            Spacer(modifier = Modifier.height(16.dp))

            AnimatedVisibility(visible = tipo == "HABITO") {
                Column {
                    GlassToggleSection(
                        label = stringResource(R.string.task_label_frequency),
                        options = frecuencias.map { it.second },
                        selectedIndex = frecuencias.indexOfFirst { it.first == frecuencia }.takeIf { it >= 0 } ?: 0,
                        onSelected = { index -> frecuencia = frecuencias[index].first }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            AnimatedVisibility(visible = tipo == "UNICO") {
                Column {
                    Text(stringResource(R.string.task_label_due_date), color = Gray300.copy(alpha = 0.6f), fontSize = 10.sp, letterSpacing = 1.5.sp, fontWeight = FontWeight.SemiBold)
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
                            text = if (fechaLimite.isEmpty()) stringResource(R.string.task_select_date) else fechaLimite,
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
                            onCreate(titulo, desc, tipo, prioridad, if (tipo == "HABITO") frecuencia else null, if (tipo == "UNICO") fechaLimite else null)
                        }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(stringResource(R.string.task_create_button), color = if (titulo.isNotBlank()) Color.White else Gray300.copy(alpha = 0.4f), fontSize = 14.sp, fontWeight = FontWeight.Bold)
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

package com.mm.astraisandroid

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
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

@Composable
fun CreateTareaDialog(
    onDismiss: () -> Unit,
    onCreate: (titulo: String, desc: String, tipo: String, prioridad: Int) -> Unit
) {
    var titulo    by remember { mutableStateOf("") }
    var desc      by remember { mutableStateOf("") }
    var tipo      by remember { mutableStateOf("UNICO") }
    var prioridad by remember { mutableStateOf(0) } // 0: Baja, 1: Media, 2: Alta

    val tipos = listOf("UNICO", "OBJETIVO", "HABITO")
    val prioridades = listOf("BAJA", "MEDIA", "ALTA")

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF1A1A2E))
                .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(20.dp))
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Nueva tarea",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace
            )

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

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("DESCRIPCIÓN", color = Color.White.copy(alpha = 0.4f), fontSize = 10.sp, fontFamily = FontFamily.Monospace, letterSpacing = 1.5.sp)
                BasicTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 60.dp).clip(RoundedCornerShape(10.dp)).background(Color.White.copy(alpha = 0.07f)).border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(10.dp)).padding(horizontal = 14.dp, vertical = 10.dp),
                    textStyle = TextStyle(color = Color.White, fontSize = 13.sp, fontFamily = FontFamily.Monospace),
                    decorationBox = { inner -> if (desc.isEmpty()) Text("Detalles adicionales...", color = Color.White.copy(alpha = 0.2f), fontSize = 13.sp, fontFamily = FontFamily.Monospace); inner() }
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
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

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(10.dp)).background(Color.White.copy(alpha = 0.06f)).clickable { onDismiss() }.padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                    Text("Cancelar", color = Color.White.copy(alpha = 0.5f), fontSize = 13.sp, fontFamily = FontFamily.Monospace)
                }
                Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(10.dp)).background(if (titulo.isNotBlank()) Color.White else Color.White.copy(alpha = 0.1f)).clickable(enabled = titulo.isNotBlank()) { onCreate(titulo, desc, tipo, prioridad) }.padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                    Text("Crear", color = if (titulo.isNotBlank()) Color.Black else Color.White.copy(alpha = 0.3f), fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
            }
        }
    }
}
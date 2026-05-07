package com.mm.astraisandroid.ui.features.logros


import com.mm.astraisandroid.R
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.mm.astraisandroid.ui.components.AstraisGlassCard
import com.mm.astraisandroid.ui.components.AstraisGlassSurface
import com.mm.astraisandroid.ui.components.AstraisScreenHeader
import com.mm.astraisandroid.ui.components.Glassmorphism
import com.mm.astraisandroid.ui.theme.Gray300
import com.mm.astraisandroid.ui.theme.Surface
import com.mm.astraisandroid.ui.theme.Tertiary

/**
 * Modelo de UI que representa un logro o achievement del usuario.
 *
 * @property id Identificador único del logro.
 * @property titulo Título descriptivo del logro.
 * @property descripcion Explicación de cómo obtener el logro.
 * @property icono Nombre del icono asociado al logro.
 * @property condicion Texto que describe la condición de desbloqueo.
 * @property completado Indica si el usuario ya ha desbloqueado este logro.
 * @property recompensaLudiones Cantidad de Ludiones otorgados al desbloquear.
 * @property esActivo Indica si el logro está activo en el juego.
 */
data class LogroItem(
    val id: Int,
    val titulo: String,
    val descripcion: String,
    val icono: String,
    val condicion: String,
    val completado: Boolean,
    val recompensaLudiones: Int,
    val esActivo: Boolean
)

val dummyAchievements = listOf(
    LogroItem(1, "Primeros Pasos", "Completa tu primera tarea", "Star", "Completar 1 tarea", true, 50, true),
    LogroItem(2, "Productividad", "Completa 10 tareas en total", "EmojiEvents", "Completar 10 tareas", true, 100, true),
    LogroItem(3, "Trabajo en Equipo", "Unete a un grupo", "Star", "Unirse a 1 grupo", true, 75, true),
    LogroItem(4, "Racha de Fuego", "Mantien una racha de 7 dias", "EmojiEvents", "Racha de 7 dias", false, 200, true),
    LogroItem(5, "Maestro de Tareas", "Completa 50 tareas", "Star", "Completar 50 tareas", false, 500, true),
    LogroItem(6, "Coleccionista", "Consigue 5 companeros", "EmojiEvents", "Obtener 5 companeros", false, 150, true),
    LogroItem(7, "Explorador", "Prueba 3 temas diferentes", "Star", "Usar 3 temas", false, 100, true),
    LogroItem(8, "Leyenda", "Alcanza el nivel 50", "EmojiEvents", "Llegar a nivel 50", false, 1000, false)
)

/**
 * Pantalla de logros del usuario con barra de progreso global y lista de achievements.
 *
 * Muestra un resumen de logros completados vs totales, barra de progreso porcentual
 * y tarjetas individuales para cada logro con su estado (completado/pendiente) y recompensa.
 *
 * @param onBack Acción ejecutada al pulsar el botón de volver.
 */
@Composable
fun LogrosScreen(
    onBack: () -> Unit = {}
) {
    val achievements = dummyAchievements
    val completedCount = achievements.count { it.completado }
    val totalCount = achievements.size
    val progress = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AstraisScreenHeader(
            title = stringResource(R.string.achievement_title),
            onBackClick = onBack
        )

        AstraisGlassCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.achievement_count_fraction, completedCount, totalCount),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = Glassmorphism.TEXT_PRIMARY),
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = stringResource(R.string.achievement_completed_label),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Gray300.copy(alpha = Glassmorphism.TEXT_SECONDARY),
                        fontFamily = FontFamily.Monospace
                    )
                }

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = Tertiary,
                    trackColor = Color.White.copy(alpha = Glassmorphism.BG_TERTIARY)
                )

                Text(
                    text = stringResource(R.string.achievement_progress_percent, (progress * 100).toInt()),
                    style = MaterialTheme.typography.labelMedium,
                    color = Gray300.copy(alpha = Glassmorphism.TEXT_TERTIARY),
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 90.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(achievements, key = { it.id }) { logro ->
                LogroCard(logro = logro)
            }
        }
    }
}

/**
 * Tarjeta individual de logro con icono, título, descripción y recompensa.
 *
 * Los logros completados se muestran con opacidad completa y color tertiary,
 * mientras que los pendientes aparecen semitransparentes.
 *
 * @param logro Modelo de logro a renderizar.
 */
@Composable
fun LogroCard(logro: LogroItem) {
    val alpha = if (logro.completado) 1f else 0.5f
    val borderColor = if (logro.completado) Tertiary.copy(alpha = 0.45f) else Color.White.copy(alpha = 0.1f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Surface.copy(alpha = 0.15f * alpha),
                        Surface.copy(alpha = 0.06f * alpha)
                    )
                )
            )
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AstraisGlassSurface(
                modifier = Modifier.size(48.dp),
                shape = MaterialTheme.shapes.medium,
                backgroundAlpha = if (logro.completado) Glassmorphism.BG_TERTIARY else Glassmorphism.BG_SECONDARY
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (logro.completado) Icons.Default.EmojiEvents else Icons.Default.Star,
                        contentDescription = null,
                        tint = if (logro.completado) Tertiary else Gray300.copy(alpha = Glassmorphism.TEXT_TERTIARY),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = logro.titulo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = Glassmorphism.TEXT_PRIMARY * alpha),
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = logro.descripcion,
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray300.copy(alpha = Glassmorphism.TEXT_SECONDARY * alpha),
                    fontFamily = FontFamily.Monospace,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = logro.condicion,
                    style = MaterialTheme.typography.labelSmall,
                    color = Gray300.copy(alpha = Glassmorphism.TEXT_TERTIARY * alpha),
                    fontFamily = FontFamily.Monospace
                )
            }

            AstraisGlassSurface(
                shape = RoundedCornerShape(8.dp),
                backgroundAlpha = if (logro.completado) Glassmorphism.BG_TERTIARY else Glassmorphism.BG_SECONDARY,
                borderAlpha = if (logro.completado) Glassmorphism.BORDER_SECONDARY else Glassmorphism.BORDER_PRIMARY * 0.5f
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = if (logro.completado) Tertiary else Gray300.copy(alpha = Glassmorphism.TEXT_TERTIARY),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "${logro.recompensaLudiones}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (logro.completado) Tertiary else Gray300.copy(alpha = Glassmorphism.TEXT_SECONDARY),
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

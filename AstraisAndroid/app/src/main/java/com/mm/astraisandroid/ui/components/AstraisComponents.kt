package com.mm.astraisandroid.ui.components


import com.mm.astraisandroid.R
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource

/**
 * Constantes de opacidad para el sistema de diseño glassmorphism de Astrais.
 *
 * Define los valores alfa utilizados para fondos, bordes, texto e iconos
 * en las superficies con efecto de cristal esmerilado.
 */
object Glassmorphism {
    /** Opacidad ultra ligera para fondos sutilísimos. */
    const val BG_ULTRA_LIGHT = 0.015f
    /** Opacidad estándar para fondos de superficies de cristal. */
    const val BG_PRIMARY = 0.04f
    /** Opacidad reducida para fondos secundarios como chips. */
    const val BG_SECONDARY = 0.02f
    /** Opacidad elevada para fondos destacados. */
    const val BG_TERTIARY = 0.08f
    /** Opacidad premium para elementos de máxima relevancia visual. */
    const val BG_PREMIUM = 0.12f
    /** Opacidad estándar para bordes de superficies de cristal. */
    const val BORDER_PRIMARY = 0.15f
    /** Opacidad reducida para bordes secundarios. */
    const val BORDER_SECONDARY = 0.08f
    /** Opacidad para texto principal sobre fondos de cristal. */
    const val TEXT_PRIMARY = 0.95f
    /** Opacidad para texto secundario. */
    const val TEXT_SECONDARY = 0.70f
    /** Opacidad para texto terciario o placeholders. */
    const val TEXT_TERTIARY = 0.45f
    /** Opacidad para iconos sobre fondos de cristal. */
    const val ICON_ALPHA = 0.85f
}

/**
 * Estilos visuales disponibles para el botón [AstraisButton].
 *
 * @property Primary Botón principal con color de énfasis de la aplicación.
 * @property Secondary Botón secundario con color de superficie variante.
 * @property Destructive Botón de acción destructiva con color de error.
 */
enum class AstraisButtonStyle { Primary, Secondary, Destructive }

/**
 * Botón reutilizable con soporte para tres estilos visuales y estado de carga.
 *
 * @param text Texto mostrado en el botón.
 * @param onClick Acción ejecutada al pulsar el botón.
 * @param modifier Modificador de composición para personalizar layout y estilo.
 * @param enabled Indica si el botón está habilitado para interacción.
 * @param loading Cuando es `true`, muestra un indicador de progreso en lugar del texto.
 * @param style Estilo visual del botón según [AstraisButtonStyle].
 */
@Composable
fun AstraisButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    style: AstraisButtonStyle = AstraisButtonStyle.Primary
) {
    val colors = when (style) {
        AstraisButtonStyle.Primary -> ButtonDefaults.buttonColors()
        AstraisButtonStyle.Secondary -> ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
        AstraisButtonStyle.Destructive -> ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.error,
            contentColor = MaterialTheme.colorScheme.onError
        )
    }
    Button(onClick = onClick, modifier = modifier, enabled = enabled && !loading, colors = colors) {
        if (loading) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.padding(vertical = 2.dp)
            )
        } else {
            Text(text = text, style = MaterialTheme.typography.titleMedium)
        }
    }
}

/**
 * Botón con borde delineado (outlined) para acciones secundarias.
 *
 * @param text Texto mostrado en el botón.
 * @param onClick Acción ejecutada al pulsar el botón.
 * @param modifier Modificador de composición para personalizar layout y estilo.
 */
@Composable
fun AstraisOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Text(text = text, style = MaterialTheme.typography.titleMedium)
    }
}

/**
 * Tarjeta con fondo de superficie variante semitransparente y forma media.
 *
 * @param modifier Modificador de composición para personalizar layout y estilo.
 * @param content Contenido composable renderizado dentro de la tarjeta.
 */
@Composable
fun AstraisCard(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        content()
    }
}

/**
 * Campo de texto delineado (outlined) con etiqueta para formularios.
 *
 * @param value Contenido actual del campo de texto.
 * @param onValueChange Callback invocado cuando el usuario modifica el texto.
 * @param label Etiqueta descriptiva mostrada sobre el campo.
 * @param modifier Modificador de composición para personalizar layout y estilo.
 */
@Composable
fun AstraisTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier
    )
}

/**
 * Vista de estado que muestra loading, error, vacío o contenido según las condiciones.
 *
 * Evalúa las banderas en orden de prioridad: loading > error > vacío > contenido.
 *
 * @param isLoading Cuando es `true`, muestra un indicador de progreso centrado.
 * @param error Mensaje de error a mostrar; si no es `null`, se muestra sobre el contenido.
 * @param isEmpty Cuando es `true` y no hay error, muestra el texto de estado vacío.
 * @param emptyText Texto mostrado cuando la lista de datos está vacía.
 * @param content Contenido principal mostrado cuando no hay loading, error ni estado vacío.
 */
@Composable
fun AstraisStateView(
    isLoading: Boolean,
    error: String?,
    isEmpty: Boolean,
    emptyText: String,
    content: @Composable () -> Unit
) {
    when {
        isLoading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        error != null -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(error, color = MaterialTheme.colorScheme.error)
        }
        isEmpty -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(emptyText, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        else -> content()
    }
}

/**
 * Diálogo de confirmación genérico con botones de confirmar y cancelar.
 *
 * @param title Título del diálogo.
 * @param body Cuerpo o descripción del diálogo.
 * @param confirmText Texto del botón de confirmación.
 * @param onConfirm Acción ejecutada al pulsar el botón de confirmar.
 * @param onDismiss Acción ejecutada al descartar el diálogo.
 */
@Composable
fun AstraisConfirmDialog(
    title: String,
    body: String,
    confirmText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(body) },
        confirmButton = { TextButton(onClick = onConfirm) { Text(confirmText) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.dialog_cancel)) } }
    )
}

/**
 * Encabezado de sección con título y contenido opcional en el extremo derecho.
 *
 * @param title Título de la sección.
 * @param trailing Contenido composable opcional alineado a la derecha (ej. botón de acción).
 */
@Composable
fun AstraisSectionHeader(title: String, trailing: @Composable (() -> Unit)? = null) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground)
        trailing?.invoke()
    }
}

/**
 * Encabezado de pantalla con título, botón de retroceso opcional y contenido trailing.
 *
 * Utiliza tipografía monoespaciada en negrita y respeta el estilo glassmorphism.
 *
 * @param title Título principal de la pantalla.
 * @param modifier Modificador de composición para personalizar layout.
 * @param onBackClick Acción al pulsar el botón de retroceso; si es `null`, no se muestra.
 * @param trailing Contenido composable opcional alineado a la derecha.
 */
@Composable
fun AstraisScreenHeader(
    title: String,
    modifier: Modifier = Modifier,
    onBackClick: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (onBackClick != null) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.cd_back),
                        tint = MaterialTheme.colorScheme.onBackground.copy(alpha = Glassmorphism.ICON_ALPHA),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = Glassmorphism.TEXT_PRIMARY),
                fontFamily = FontFamily.Monospace,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }
        trailing?.invoke()
    }
}

/**
 * Superficie con efecto glassmorphism configurable (fondo semitransparente + borde).
 *
 * @param modifier Modificador de composición para personalizar layout y estilo.
 * @param shape Forma de la superficie (por defecto [MaterialTheme.shapes.extraLarge]).
 * @param backgroundAlpha Opacidad del fondo de cristal.
 * @param borderAlpha Opacidad del borde de cristal.
 * @param onClick Si se proporciona, la superficie es clicable.
 * @param content Contenido composable renderizado dentro de la superficie.
 */
@Composable
fun AstraisGlassSurface(
    modifier: Modifier = Modifier,
    shape: androidx.compose.ui.graphics.Shape = MaterialTheme.shapes.extraLarge,
    backgroundAlpha: Float = Glassmorphism.BG_PRIMARY,
    borderAlpha: Float = Glassmorphism.BORDER_PRIMARY,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val baseModifier = modifier
        .clip(shape)
        .background(
            MaterialTheme.colorScheme.onBackground.copy(alpha = backgroundAlpha)
        )
        .border(
            width = 0.5.dp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = borderAlpha),
            shape = shape
        )

    Box(
        modifier = if (onClick != null) {
            baseModifier.clickable { onClick() }
        } else baseModifier,
        content = content
    )
}

/**
 * Tarjeta con efecto glassmorphism y padding interno predefinido.
 *
 * Envuelve [AstraisGlassSurface] con forma extra-large y 20dp de padding.
 *
 * @param modifier Modificador de composición para personalizar layout y estilo.
 * @param onClick Si se proporciona, la tarjeta es clicable.
 * @param content Contenido composable renderizado dentro de la tarjeta.
 */
@Composable
fun AstraisGlassCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    AstraisGlassSurface(
        modifier = modifier,
        shape = MaterialTheme.shapes.extraLarge,
        onClick = onClick
    ) {
        Box(modifier = Modifier.padding(20.dp)) {
            content()
        }
    }
}

/**
 * Chip con efecto glassmorphism de menor opacidad y forma grande.
 *
 * Utiliza [Glassmorphism.BG_SECONDARY] y [Glassmorphism.BORDER_SECONDARY]
 * para un aspecto más sutil que [AstraisGlassCard].
 *
 * @param modifier Modificador de composición para personalizar layout y estilo.
 * @param onClick Si se proporciona, el chip es clicable.
 * @param content Contenido composable renderizado dentro del chip.
 */
@Composable
fun AstraisGlassChip(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    AstraisGlassSurface(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        backgroundAlpha = Glassmorphism.BG_SECONDARY,
        borderAlpha = Glassmorphism.BORDER_SECONDARY,
        onClick = onClick,
        content = content
    )
}


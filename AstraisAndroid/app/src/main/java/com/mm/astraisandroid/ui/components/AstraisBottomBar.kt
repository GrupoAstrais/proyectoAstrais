package com.mm.astraisandroid.ui.components


import com.mm.astraisandroid.R
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource

/**
 * Representa un ítem individual en la barra de navegación inferior.
 *
 * @property title Texto descriptivo del ítem.
 * @property icon Vector icónico asociado al ítem.
 */
data class NavItem(val title: String, val icon: ImageVector)

/**
 * Barra de navegación inferior con estilo glassmorphism y animaciones de selección.
 *
 * Contiene cinco ítems: Inicio, Tareas, Agregar, Grupos y Tienda.
 * Los ítems de Grupos y Tienda se deshabilitan para usuarios invitados.
 *
 * @param selected Índice del ítem actualmente seleccionado.
 * @param isGuest Indica si el usuario está en modo invitado (deshabilita Grupos y Tienda).
 * @param onSelect Callback invocado al seleccionar un ítem con su índice.
 */
@Composable
fun AstraisBottomBar(selected: Int, isGuest: Boolean = false, onSelect: (Int) -> Unit) {
    val items = listOf(
        NavItem(stringResource(R.string.general_nav_home), Icons.Filled.Home),
        NavItem(stringResource(R.string.general_nav_tasks), Icons.Filled.CheckCircle),
        NavItem(stringResource(R.string.general_nav_add), Icons.Filled.AddCircle),
        NavItem(stringResource(R.string.general_nav_groups), Icons.Filled.Person),
        NavItem(stringResource(R.string.general_nav_store), Icons.Filled.ShoppingCart)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 12.dp,
                    shape = RoundedCornerShape(24.dp),
                    ambientColor = Color.Black.copy(alpha = 0.3f),
                    spotColor = Color.White.copy(alpha = 0.05f)
                )
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.75f),
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.15f),
                            Color.White.copy(alpha = 0.05f)
                        )
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, item ->
                val isSelected = selected == index
                val isCenter = index == 2
                val isDisabled = isGuest && (index == 3 || index == 4)

                GlassBottomBarItem(
                    item = item,
                    isSelected = isSelected,
                    isCenter = isCenter,
                    isDisabled = isDisabled,
                    onClick = { onSelect(index) }
                )
            }
        }
    }
}

/**
 * Ítem individual de la barra de navegación inferior con animación de transición.
 *
 * Muestra un icono por defecto y anima al texto del título cuando está seleccionado.
 * El ítem central tiene un tamaño mayor y fondo primario destacado.
 *
 * @param item Datos del ítem (título e icono).
 * @param isSelected Indica si este ítem es el actualmente seleccionado.
 * @param isCenter Indica si es el ítem central (botón de agregar).
 * @param isDisabled Indica si el ítem está deshabilitado (invitados).
 * @param onClick Acción ejecutada al pulsar el ítem.
 */
@Composable
private fun GlassBottomBarItem(
    item: NavItem,
    isSelected: Boolean,
    isCenter: Boolean,
    isDisabled: Boolean,
    onClick: () -> Unit
) {
    val itemSize = if (isCenter) 54.dp else 44.dp

    Box(
        modifier = Modifier
            .size(itemSize)
            .clip(CircleShape)
            .background(
                when {
                    isCenter -> MaterialTheme.colorScheme.primary
                    isSelected -> Color.White.copy(alpha = 0.12f)
                    else -> Color.Transparent
                }
            )
            .then(
                if (isSelected && !isCenter) {
                    Modifier.border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                } else {
                    Modifier
                }
            )
            .clickable(enabled = !isDisabled) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(
            targetState = isSelected && !isCenter,
            transitionSpec = {
                scaleIn(initialScale = 0.7f, animationSpec = tween(300)) +
                        fadeIn(tween(300)) togetherWith
                        scaleOut(targetScale = 0.7f, animationSpec = tween(300)) +
                        fadeOut(tween(300))
            },
            label = "tab_anim_${item.title}"
        ) { showText ->
            if (showText) {
                Text(
                    text = item.title,
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            } else {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.title,
                    tint = when {
                        isDisabled -> Color.White.copy(alpha = 0.2f)
                        isCenter -> Color.White
                        isSelected -> Color.White
                        else -> Color.White.copy(alpha = 0.5f)
                    },
                    modifier = Modifier.size(if (isCenter) 28.dp else 22.dp)
                )
            }
        }
    }
}

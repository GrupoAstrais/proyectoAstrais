package com.mm.astraisandroid.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class NavItem(val title: String, val icon: ImageVector)

@Composable
fun AstraisBottomBar(selected: Int, isGuest: Boolean = false, onSelect: (Int) -> Unit) {
    val items = listOf(
        NavItem("Home", Icons.Filled.Home),
        NavItem("Tasks", Icons.Filled.CheckCircle),
        NavItem("Add", Icons.Filled.AddCircle),
        NavItem("Groups", Icons.Filled.Person),
        NavItem("Store", Icons.Filled.ShoppingCart)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0x16FFFFFF))
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEachIndexed { index, item ->
            val isSelected = selected == index
            val isCenter = index == 2
            val isDisabled = isGuest && (index == 3 || index == 4)

            Box(
                modifier = Modifier
                    .size(if (isCenter) 54.dp else 44.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            isCenter -> Color.White
                            isSelected -> Color.White.copy(alpha = 0.15f)
                            else -> Color.Transparent
                        }
                    )
                    .clickable { onSelect(index) },
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
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 0.5.sp
                        )
                    } else {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.title,
                            tint = when {
                                isDisabled -> Color.White.copy(alpha = 0.2f)
                                isCenter -> Color.Black
                                else -> Color.White.copy(alpha = 0.5f)
                            },
                            modifier = Modifier.size(if (isCenter) 28.dp else 22.dp)
                        )
                    }
                }
            }
        }
    }
}

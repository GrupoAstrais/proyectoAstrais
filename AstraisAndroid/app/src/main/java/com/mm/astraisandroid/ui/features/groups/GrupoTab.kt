package com.mm.astraisandroid.ui.features.groups

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mm.astraisandroid.ui.features.auth.AuthBackground
import com.mm.astraisandroid.R

private val GreenAccent  = Color(0xFFD6DCEE)
private val CardBg       = Color.White.copy(alpha = 0.07f)
private val CardBorder   = Color.White.copy(alpha = 0.12f)

data class Grupo(
    val id: Int,
    val name: String,
    val subtitle: String,
    val avatarRes: Int? = null,
    val avatarColor: Color = Color(0xFFD4F0A0),
)

@Composable
fun GrupoTab(onBack: () -> Unit = {}) {
    var query by remember { mutableStateOf("") }

    val grupos = listOf(
        Grupo(
            id = 1,
            name = "Grupo Astrais",
            subtitle = "TFG project",
            avatarRes = R.drawable.gato1,
            avatarColor = Color(0xFFE8F5C0)
        ),
        Grupo(
            id = 2,
            name = "Grupo No Astrais",
            subtitle = "TFG project",
            avatarRes = R.drawable.gato2,
            avatarColor = Color(0xFF4A6741)
        ),
        Grupo(
            id = 3,
            name = "Grupo Locura",
            subtitle = "TFG project",
            avatarRes = R.drawable.gato3,
            avatarColor = Color(0xFFE8F5C0)
        ),
    )

    val filtered = grupos.filter {
        query.isBlank() || it.name.contains(query, ignoreCase = true)
    }

    AuthBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Top bar
            GruposTopBar(onBack = onBack)

            // Buscador
            SearchBar(query = query, onQueryChange = { query = it })

            // Lista
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(filtered, key = { it.id }) { grupo ->
                    GrupoCard(grupo = grupo)
                }
            }
        }
    }
}

@Composable
fun GruposTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.clickable { onBack() }
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Volver",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = "Grupos",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace
            )
        }

        // Guarrada por no poner un icono, pido perdon
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(CardBg)
                .border(1.dp, CardBorder, RoundedCornerShape(10.dp))
                .clickable { },
            contentAlignment = Alignment.Center
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(3.dp),
                horizontalAlignment = Alignment.End,
                modifier = Modifier.padding(horizontal = 7.dp)
            ) {
                Box(modifier = Modifier.width(16.dp).height(1.5.dp).background(Color.White))
                Box(modifier = Modifier.width(11.dp).height(1.5.dp).background(Color.White))
                Box(modifier = Modifier.width(7.dp).height(1.5.dp).background(Color.White))
            }
        }
    }
}

@Composable
fun SearchBar(query: String, onQueryChange: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = 0.07f))
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.3f),
            modifier = Modifier.size(18.dp)
        )
        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            textStyle = TextStyle(
                color = Color.White,
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace
            ),
            decorationBox = { inner ->
                if (query.isEmpty()) {
                    Text(
                        text = "Buscar grupo...",
                        color = Color.White.copy(alpha = 0.25f),
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
                inner()
            }
        )
    }
}

@Composable
fun GrupoCard(grupo: Grupo) {
    var expanded by remember { mutableStateOf(false) }
    val arrowRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(300),
        label = "arrow_${grupo.id}"
    )
    val shape = RoundedCornerShape(20.dp)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(GreenAccent)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (grupo.avatarRes != null) {
                Image(
                    painter = painterResource(id = grupo.avatarRes),
                    contentDescription = "Avatar de ${grupo.name}",
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(grupo.avatarColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = grupo.name.firstOrNull()?.toString() ?: "?",
                        color = Color.Black,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = grupo.name,
                    color = Color.Black,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = grupo.subtitle,
                    color = Color.Black.copy(alpha = 0.5f),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "Opciones",
                tint = Color.Black.copy(alpha = 0.6f),
                modifier = Modifier
                    .size(20.dp)
                    .clickable { }
            )

            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = if (expanded) "Colapsar" else "Expandir",
                tint = Color.Black.copy(alpha = 0.6f),
                modifier = Modifier
                    .size(22.dp)
                    .rotate(arrowRotation)
            )
        }

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(tween(300)) + fadeIn(tween(300)),
            exit  = shrinkVertically(tween(300)) + fadeOut(tween(300))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.85f))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "MIEMBROS",
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.5.sp
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    repeat(3) { index ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.1f))
                                    .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = listOf("A", "M", "R")[index],
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Text(
                                text = listOf("Astra", "Marco", "Raquel")[index],
                                color = Color.White.copy(alpha = 0.4f),
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color.White.copy(alpha = 0.08f))
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    GrupoStat(label = "Tareas", value = "12")
                    GrupoStat(label = "Completadas", value = "8")
                    GrupoStat(label = "XP", value = "640")
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(GreenAccent.copy(alpha = 0.15f))
                        .border(1.dp, GreenAccent.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .clickable { }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Ver grupo",
                        color = GreenAccent,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Composable
fun GrupoStat(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Text(
            text = value,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.35f),
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}
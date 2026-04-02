package com.mm.astraisandroid.ui.tabs

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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CurrencyPound
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mm.astraisandroid.ui.auth.components.AuthBackground
import com.mm.astraisandroid.R

private val PrimaryBlue = Color(0xFF8B9DFF)
private val SecondaryBlue = Color(0xFFA8B8FF)

enum class Rareza(val color: Color, val borderColor: Color, val glowColor: Color) {
    COMUN(
        color = Color(0xFF9E9E9E),
        borderColor = Color(0xFFBDBDBD),
        glowColor = Color.Transparent
    ),
    RARO(
        color = Color(0xFF4CAF50),
        borderColor = Color(0xFF81C784),
        glowColor = Color(0x404CAF50)
    ),
    EPICO(
        color = Color(0xFF9C27B0),
        borderColor = Color(0xFFBA68C8),
        glowColor = Color(0x409C27B0)
    ),
    LEGENDARIO(
        color = Color(0xFFFF9800),
        borderColor = Color(0xFFFFB74D),
        glowColor = Color(0x40FF9800)
    ),
    MITICO(
        color = Color(0xFFE91E63),
        borderColor = Color(0xFFF06292),
        glowColor = Color(0x40E91E63)
    )
}

data class TiendaItem(
    val id: Int,
    val name: String,
    val price: Int,
    val color: Color,
    val imageRes: Int? = null,
    val rarity: Rareza = Rareza.COMUN,
    val isPurchased: Boolean = false
)

data class CategoriaTienda(
    val id: Int,
    val nombre: String,
    val items: List<TiendaItem>
)

@Composable
fun TiendaTab(ludiones: Int) {
    var selectedTab by remember { mutableStateOf(0) }
    var query by remember { mutableStateOf("") }

    val tabs = listOf("Mascota", "Temas", "Colores")

    val categorias = listOf(
        CategoriaTienda(
            id = 1,
            nombre = "Animales Acuáticos",
            items = listOf(
                TiendaItem(1, "Pez Dorado", 150, Color(0xFFFFD700),
                    imageRes = R.drawable.gato1, rarity = Rareza.COMUN),
                TiendaItem(2, "Delfín", 300, Color(0xFF4A90E2),
                    imageRes = R.drawable.gato2, rarity = Rareza.RARO),
                TiendaItem(3, "Tiburón", 500, Color(0xFF5C6BC0),
                    imageRes = R.drawable.gato3, rarity = Rareza.EPICO),
                TiendaItem(4, "Pulpo", 400, Color(0xFFE91E63),
                    imageRes = R.drawable.gato4, rarity = Rareza.LEGENDARIO),
                TiendaItem(5, "Ballena", 600, Color(0xFF3F51B5),
                    imageRes = R.drawable.gato2, rarity = Rareza.MITICO),
            )
        ),
        CategoriaTienda(
            id = 2,
            nombre = "DIOS ME LIBRE DE JETPACK",
            items = listOf(
                TiendaItem(1, "Composable", 150, Color(0xFFFFD700),
                    imageRes = R.drawable.gato1, rarity = Rareza.RARO),
                TiendaItem(2, "Box", 300, Color(0xFF4A90E2),
                    imageRes = R.drawable.gato2, rarity = Rareza.LEGENDARIO),
                TiendaItem(3, "Row por todos lados", 500, Color(0xFF5C6BC0),
                    imageRes = R.drawable.gato3, rarity = Rareza.COMUN),
                TiendaItem(4, "Algo guapo", 400, Color(0xFFE91E63),
                    imageRes = R.drawable.gato4, rarity = Rareza.RARO),
                TiendaItem(5, "Widget", 600, Color(0xFF3F51B5),
                    imageRes = R.drawable.gato2, rarity = Rareza.EPICO),
            )
        ),
    )

    val filteredCategorias = categorias.map { categoria ->
        categoria.copy(
            items = categoria.items.filter {
                query.isBlank() || it.name.contains(query, ignoreCase = true)
            }
        )
    }.filter { it.items.isNotEmpty() }

    AuthBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            TiendaHeader(ludiones = ludiones)

            // Subtitle
            Text(
                text = "Cosméticos",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            // Tabs
            TiendaTabs(tabs = tabs, selectedTab = selectedTab, onTabSelected = { selectedTab = it })

            // Search Bar
            TiendaSearchBar(query = query, onQueryChange = { query = it })

            // Lista de categorías
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                filteredCategorias.forEach { categoria ->
                    CategoriaExpansible(categoria = categoria)
                }
            }
        }
    }
}

@Composable
fun TiendaHeader(ludiones: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Tienda",
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.CurrencyPound,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.3f),
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = "${ludiones.toString()}",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
fun TiendaTabs(tabs: List<String>, selectedTab: Int, onTabSelected: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(PrimaryBlue.copy(alpha = 0.6f)),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        tabs.forEachIndexed { index, tab ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onTabSelected(index) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = tab,
                    color = if (selectedTab == index) Color.White else Color.White.copy(alpha = 0.6f),
                    fontSize = 14.sp,
                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium,
                    fontFamily = FontFamily.Monospace
                )

                if (index < tabs.lastIndex) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .width(1.dp)
                            .height(16.dp)
                            .background(Color.White.copy(alpha = 0.3f))
                    )
                }
            }
        }
    }
}

@Composable
fun TiendaSearchBar(query: String, onQueryChange: (String) -> Unit) {
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
            imageVector = Icons.Filled.Search,
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
                        text = "Buscar...",
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
fun CategoriaExpansible(categoria: CategoriaTienda) {
    var expanded by remember { mutableStateOf(false) }
    val arrowRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(300),
        label = "arrow_${categoria.id}"
    )

    val cardBg = Color.White.copy(alpha = 0.08f)
    val cardBorder = Color.White.copy(alpha = 0.15f)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(cardBg)
            .border(1.dp, cardBorder, RoundedCornerShape(16.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = categoria.nombre,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "${categoria.items.size} items",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )

                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = if (expanded) "Colapsar" else "Expandir",
                    tint = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier
                        .size(22.dp)
                        .rotate(arrowRotation)
                )
            }
        }

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(tween(300)) + fadeIn(tween(300)),
            exit = shrinkVertically(tween(300)) + fadeOut(tween(300))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    categoria.items.forEach { item ->
                        TiendaItemCard(item = item)
                    }
                }
            }
        }
    }
}

@Composable
fun TiendaItemCard(item: TiendaItem) {
    val rarezaColors = item.rarity
    val cardBg = rarezaColors.color.copy(alpha = 0.3f)
    val cardBorder = rarezaColors.borderColor
    val glowColor = rarezaColors.glowColor

    Box(
        modifier = Modifier
            .width(100.dp)
            .height(120.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        item.color.copy(alpha = 0.6f),
                        cardBg
                    )
                )
            )
            .then(
                if (glowColor != Color.Transparent) {
                    Modifier.border(
                        width = 2.dp,
                        color = glowColor,
                        shape = RoundedCornerShape(12.dp)
                    )
                } else {
                    Modifier
                }
            )
            .border(
                2.dp,
                if (item.isPurchased) Color(0xFF00E676) else cardBorder,
                RoundedCornerShape(12.dp)
            )
            .clickable { },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(4.dp)
        ) {
            Text(
                text = item.rarity.name,
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.align(Alignment.TopEnd)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(8.dp)
        ) {
            if (item.imageRes != null) {
                Image(
                    painter = painterResource(id = item.imageRes),
                    contentDescription = item.name,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (item.isPurchased) {
                        Text(
                            text = "✓",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Text(
                text = item.name,
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = FontFamily.Monospace,
                maxLines = 1
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.coin),
                    contentDescription = "Moneda",
                    modifier = Modifier.size(12.dp),
                    colorFilter = ColorFilter.tint(
                        if (item.isPurchased) Color(0xFF00E676) else Color.White
                    )
                )
                Text(
                    text = "${item.price}",
                    color = if (item.isPurchased) Color(0xFF00E676) else Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}
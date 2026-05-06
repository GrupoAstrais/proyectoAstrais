package com.mm.astraisandroid.ui.features.store


import com.mm.astraisandroid.R
import android.widget.Toast
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CurrencyPound
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.res.stringResource
import androidx.core.graphics.toColorInt
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mm.astraisandroid.data.api.ThemeConfig
import com.mm.astraisandroid.data.models.Cosmetic
import com.mm.astraisandroid.ui.components.AstraisGlassSurface
import com.mm.astraisandroid.ui.components.AstraisScreenHeader
import com.mm.astraisandroid.ui.components.Glassmorphism
import com.mm.astraisandroid.ui.features.auth.AuthBackground
import com.mm.astraisandroid.ui.theme.Gray300
import com.mm.astraisandroid.util.AvatarImageRenderer
import com.mm.astraisandroid.util.LottiePetRenderer
import kotlinx.coroutines.flow.collectLatest
import kotlinx.serialization.json.Json

/**
 * Obtiene el color asociado a la rareza de un cosmético.
 *
 * @param rareza Cadena de rareza (LEGENDARIO, EPICO, RARO, etc.).
 * @return Color correspondiente a la rareza, o blanco semitransparente por defecto.
 */
fun getRarityColor(rarity: String): Color {
    return when (rarity.uppercase()) {
        "LEGENDARIO" -> Color(0xFFFFCA28)
        "EPICO" -> Color(0xFFAB47BC)
        "RARO" -> Color(0xFF42A5F5)
        else -> Color.White.copy(alpha = 0.15f)
    }
}

/**
 * Pantalla de la tienda de cosméticos con filtrado por categoría y diálogo de compra.
 *
 * Muestra los artículos disponibles organizados por tabs (Todos, Mascotas, Avatares, Temas)
 * y agrupados por colección. Al pulsar un artículo se abre un diálogo de detalle con
 * información de rareza, descripción y opción de compra con Ludiones.
 *
 * @param ludiones Cantidad actual de Ludiones del usuario para validar compras.
 * @param storeViewModel ViewModel de la tienda inyectado por Hilt.
 * @param onCosmeticChanged Callback ejecutado tras comprar un artículo (refresca datos del usuario).
 */
@Composable
fun TiendaTab(
    ludiones: Int,
    storeViewModel: StoreViewModel = hiltViewModel(),
    onCosmeticChanged: () -> Unit = {}
) {
    val state by storeViewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedCosmetic by remember { mutableStateOf<Cosmetic?>(null) }

    LaunchedEffect(Unit) {
        storeViewModel.loadStore()
        storeViewModel.uiEvent.collectLatest { event ->
            when(event) {
                is StoreEvent.BuySuccess -> onCosmeticChanged()
                else -> {}
            }
        }
    }

    val storePets = state.items.filter { it.type.name.contains("PET") }
    val storeThemes = state.items.filter { it.type.name == "APP_THEME" }
    val storeAvatars = state.items.filter { it.type.name == "AVATAR_PART" }

    AuthBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AstraisScreenHeader(
                title = stringResource(R.string.store_title),
                trailing = {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.3f))
                            .border(
                                1.dp,
                                Brush.linearGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.15f),
                                        Color.White.copy(alpha = 0.06f)
                                    )
                                ),
                                RoundedCornerShape(16.dp)
                            )
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.CurrencyPound,
                            contentDescription = stringResource(R.string.cd_ludiones),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "$ludiones",
                            color = Color.White.copy(alpha = Glassmorphism.TEXT_PRIMARY),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.15f))
                    .border(
                        1.dp,
                        Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.1f),
                                Color.White.copy(alpha = 0.04f)
                            )
                        ),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                GlassStoreTabSelector(stringResource(R.string.store_tab_all), selectedTab == 0, Modifier.weight(1f)) { selectedTab = 0 }
                GlassStoreTabSelector(stringResource(R.string.store_tab_pets), selectedTab == 1, Modifier.weight(1.3f)) { selectedTab = 1 }
                GlassStoreTabSelector(stringResource(R.string.store_tab_avatars), selectedTab == 2, Modifier.weight(1.2f)) { selectedTab = 2 }
                GlassStoreTabSelector(stringResource(R.string.store_tab_themes), selectedTab == 3, Modifier.weight(1f)) { selectedTab = 3 }
            }

            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else {
                Crossfade(targetState = selectedTab, label = "tab_fade") { tab ->
                    if (state.items.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(stringResource(R.string.store_empty_items), color = Gray300.copy(alpha = 0.5f))
                        }
                    } else {
                        when (tab) {
                            0 -> {
                                val groupedByTheme = state.items.groupBy { it.coleccion.uppercase() }
                                LazyColumn(
                                    contentPadding = PaddingValues(bottom = 90.dp),
                                    verticalArrangement = Arrangement.spacedBy(28.dp),
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    groupedByTheme.forEach { (themeName, itemsInTheme) ->
                                        item {
                                            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                                Text(
                                                    text = stringResource(R.string.store_collection_header, themeName),
                                                    color = MaterialTheme.colorScheme.tertiary,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 4.dp)
                                                )
                                                LazyRow(
                                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                                    contentPadding = PaddingValues(horizontal = 4.dp)
                                                ) {
                                                    items(itemsInTheme, key = { it.id }) { item ->
                                                        if (item.type.name != "APP_THEME") {
                                                            GlassPetStoreCard(item, Modifier.width(160.dp)) { selectedCosmetic = item }
                                                        } else {
                                                            GlassThemeStoreCard(item, Modifier.width(160.dp)) { selectedCosmetic = item }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            1 -> {
                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(2),
                                    verticalArrangement = Arrangement.spacedBy(16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    contentPadding = PaddingValues(bottom = 90.dp),
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    items(storePets, key = { it.id }) { item -> GlassPetStoreCard(item) { selectedCosmetic = item } }
                                }
                            }
                            2 -> {
                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(2),
                                    verticalArrangement = Arrangement.spacedBy(16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    contentPadding = PaddingValues(bottom = 90.dp),
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                     items(storeAvatars, key = { it.id }) { item -> GlassPetStoreCard(item) { selectedCosmetic = item } }
                                }
                            }
                            3 -> {
                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(2),
                                    verticalArrangement = Arrangement.spacedBy(16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    contentPadding = PaddingValues(bottom = 90.dp),
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    items(storeThemes, key = { it.id }) { item -> GlassThemeStoreCard(item) { selectedCosmetic = item } }
                                }
                            }
                        }
                    }
                }
            }
        }

        selectedCosmetic?.let { cosmetic ->
            GlassStoreDetailDialog(
                item = cosmetic,
                ludiones = ludiones,
                onDismiss = { selectedCosmetic = null },
                onBuy = {
                    storeViewModel.buyItem(cosmetic.id) { onCosmeticChanged(); selectedCosmetic = null }
                }
            )
        }
    }
}

/**
 * Botón de tab para la tienda con estilo glassmorphism.
 *
 * @param text Texto descriptivo del tab.
 * @param isSelected Indica si este tab es el seleccionado actualmente.
 * @param modifier Modificador de composición para personalizar layout.
 * @param onClick Acción ejecutada al pulsar el tab.
 */
@Composable
fun GlassStoreTabSelector(text: String, isSelected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val bgColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.Transparent
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f) else Color.Transparent
    val textColor = if (isSelected) MaterialTheme.colorScheme.primary else Gray300.copy(alpha = Glassmorphism.TEXT_TERTIARY / 0.95f)

    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .then(
                if (isSelected) Modifier.border(1.dp, borderColor, RoundedCornerShape(8.dp)) else Modifier
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

/**
 * Tarjeta de artículo de mascota/avatar en la tienda con preview y precio.
 *
 * @param item Modelo de cosmético a mostrar.
 * @param modifier Modificador de composición para personalizar layout.
 * @param onClick Acción ejecutada al pulsar la tarjeta.
 */
@Composable
fun GlassPetStoreCard(item: Cosmetic, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val isOwned = item.owned
    val rarityColor = getRarityColor(item.rarity)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = Color.Black.copy(alpha = 0.15f),
                spotColor = Color.Transparent
            )
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.2f),
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.08f)
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = if (isOwned) {
                    Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.08f),
                            Color.White.copy(alpha = 0.03f)
                        )
                    )
                } else {
                    Brush.linearGradient(
                        colors = listOf(
                            rarityColor.copy(alpha = 0.45f),
                            rarityColor.copy(alpha = 0.15f)
                        )
                    )
                },
                shape = RoundedCornerShape(20.dp)
            )
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                if (isOwned) Color.White.copy(alpha = 0.03f) else rarityColor.copy(alpha = 0.15f),
                                Color.Transparent
                            )
                        )
                    )
                    .border(
                        1.dp,
                        if (isOwned) Color.White.copy(alpha = 0.08f) else rarityColor.copy(alpha = 0.35f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (item.type.name == "AVATAR_PART") {
                    AvatarImageRenderer(
                        assetRef = item.assetRef,
                        initial = item.name.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                        size = 70.dp
                    )
                } else {
                    LottiePetRenderer(assetRef = item.assetRef, modifier = Modifier.size(70.dp))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = item.name,
                color = Color.White.copy(alpha = Glassmorphism.TEXT_PRIMARY),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
            Text(
                text = item.coleccion.uppercase(),
                color = if (isOwned) Gray300.copy(alpha = 0.4f) else rarityColor,
                fontSize = 10.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (isOwned) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.05f))
                        .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        stringResource(R.string.store_owned_label),
                        color = Gray300.copy(alpha = 0.3f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.CurrencyPound, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "${item.price}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

/**
 * Tarjeta de tema de aplicación en la tienda con preview de colores.
 *
 * @param item Modelo de cosmético de tipo APP_THEME a mostrar.
 * @param modifier Modificador de composición para personalizar layout.
 * @param onClick Acción ejecutada al pulsar la tarjeta.
 */
@Composable
fun GlassThemeStoreCard(item: Cosmetic, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val isOwned = item.owned
    val rarityColor = getRarityColor(item.rarity)
    val parsedConfig = remember(item.theme) {
        try {
            item.theme?.takeIf { it.isNotBlank() }?.let { Json { ignoreUnknownKeys = true }.decodeFromString<ThemeConfig>(it) }
        } catch (e: Exception) { null }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = Color.Black.copy(alpha = 0.15f),
                spotColor = Color.Transparent
            )
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.2f),
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.08f)
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = if (isOwned) {
                    Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.08f),
                            Color.White.copy(alpha = 0.03f)
                        )
                    )
                } else {
                    Brush.linearGradient(
                        colors = listOf(
                            rarityColor.copy(alpha = 0.45f),
                            rarityColor.copy(alpha = 0.15f)
                        )
                    )
                },
                shape = RoundedCornerShape(20.dp)
            )
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (parsedConfig != null) {
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color(parsedConfig.background.toColorInt()).copy(alpha = 0.8f),
                                    Color(parsedConfig.backgroundAlt.toColorInt()).copy(alpha = 0.8f)
                                )
                            )
                        )
                        .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            GlassStoreColorCircle(parsedConfig.primary, size = 20)
                            GlassStoreColorCircle(parsedConfig.secondary, size = 20)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            GlassStoreColorCircle(parsedConfig.tertiary, size = 20)
                            GlassStoreColorCircle(parsedConfig.error, size = 20)
                        }
                    }
                }
            } else {
                Box(modifier = Modifier.size(90.dp).clip(CircleShape).background(Gray300.copy(alpha = 0.3f)))
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = item.name,
                color = Color.White.copy(alpha = Glassmorphism.TEXT_PRIMARY),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
            Text(
                text = stringResource(R.string.store_app_theme_label),
                color = if (isOwned) Gray300.copy(alpha = 0.4f) else MaterialTheme.colorScheme.tertiary,
                fontSize = 10.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (isOwned) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.05f))
                        .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        stringResource(R.string.store_owned_label),
                        color = Gray300.copy(alpha = 0.3f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.CurrencyPound, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "${item.price}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

/**
 * Círculo de color para preview de temas en la tienda.
 *
 * @param hex Color en formato hexadecimal.
 * @param size Tamaño del círculo en dp.
 */
@Composable
fun GlassStoreColorCircle(hex: String, size: Int = 16) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(Color(hex.toColorInt()))
            .border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape)
    )
}

/**
 * Diálogo modal de detalle de artículo en la tienda con opción de compra.
 *
 * Muestra preview del cosmético, nombre, descripción, rareza, precio en Ludiones
 * y botón de compra (deshabilitado si no hay fondos suficientes o ya es propiedad).
 *
 * @param item Modelo de cosmético a mostrar.
 * @param ludiones Cantidad actual de Ludiones del usuario.
 * @param onDismiss Acción ejecutada al cerrar el diálogo sin comprar.
 * @param onBuy Acción ejecutada al confirmar la compra.
 */
@Composable
fun GlassStoreDetailDialog(item: Cosmetic, ludiones: Int, onDismiss: () -> Unit, onBuy: () -> Unit) {
    val canAfford = ludiones >= item.price

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .shadow(
                    elevation = 16.dp,
                    shape = RoundedCornerShape(24.dp),
                    ambientColor = Color.Black.copy(alpha = 0.4f),
                    spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                )
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
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
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(24.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                    Color.Transparent
                                )
                            )
                        )
                        .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (item.type.name == "AVATAR_PART") {
                        AvatarImageRenderer(
                            assetRef = item.assetRef,
                            initial = item.name.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                            size = 120.dp
                        )
                    } else if (item.type.name != "APP_THEME") {
                        LottiePetRenderer(assetRef = item.assetRef, modifier = Modifier.size(120.dp))
                    } else {
                        val parsedConfig = remember(item.theme) {
                            try {
                                item.theme?.takeIf { it.isNotBlank() }?.let { Json { ignoreUnknownKeys = true }.decodeFromString<ThemeConfig>(it) }
                            } catch (e: Exception) { null }
                        }
                        if (parsedConfig != null) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                GlassStoreColorCircle(parsedConfig.primary, size = 32)
                                GlassStoreColorCircle(parsedConfig.secondary, size = 32)
                                GlassStoreColorCircle(parsedConfig.tertiary, size = 32)
                                GlassStoreColorCircle(parsedConfig.background, size = 32)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = item.name,
                    color = Color.White.copy(alpha = Glassmorphism.TEXT_PRIMARY),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = item.desc,
                    color = Gray300.copy(alpha = 0.6f),
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(20.dp))

                if (item.owned) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.06f))
                            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            stringResource(R.string.store_already_owned),
                            color = Gray300.copy(alpha = 0.5f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (canAfford) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.error.copy(alpha = 0.1f))
                            .border(
                                1.dp,
                                if (canAfford) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f) else MaterialTheme.colorScheme.error.copy(alpha = 0.3f),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Filled.CurrencyPound, contentDescription = null, tint = if (canAfford) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.store_price_ludiones, item.price),
                            color = if (canAfford) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    if (!canAfford) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(stringResource(R.string.store_insufficient_funds), color = MaterialTheme.colorScheme.error, fontSize = 10.sp)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
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
                        Text(stringResource(R.string.store_back_button), color = Gray300, fontSize = 14.sp)
                    }

                    val canBuy = !item.owned && canAfford
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (canBuy) MaterialTheme.colorScheme.primary else Gray300.copy(alpha = 0.2f))
                            .shadow(
                                elevation = if (canBuy) 6.dp else 0.dp,
                                shape = RoundedCornerShape(12.dp),
                                ambientColor = if (canBuy) MaterialTheme.colorScheme.primary.copy(alpha = 0.25f) else Color.Transparent,
                                spotColor = if (canBuy) Color.White.copy(alpha = 0.1f) else Color.Transparent
                            )
                            .clickable(enabled = canBuy) { onBuy() }
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (item.owned) stringResource(R.string.store_purchased) else stringResource(R.string.store_acquire),
                            color = if (canBuy) Color.White else Gray300.copy(alpha = 0.4f),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

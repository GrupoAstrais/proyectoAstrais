package com.mm.astraisandroid.ui.features.store

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mm.astraisandroid.util.LottiePetRenderer
import com.mm.astraisandroid.util.AvatarImageRenderer
import com.mm.astraisandroid.ui.features.auth.AuthBackground
import com.mm.astraisandroid.ui.components.AstraisScreenHeader
import com.mm.astraisandroid.ui.components.Glassmorphism
import com.mm.astraisandroid.data.api.ThemeConfig
import kotlinx.serialization.json.Json
import androidx.core.graphics.toColorInt
import com.mm.astraisandroid.data.models.Cosmetic
import kotlinx.coroutines.flow.collectLatest
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext

fun getRarityColor(price: Int): Color {
    return when {
        price >= 5000 -> Color(0xFFFFCA28)
        price >= 1000 -> Color(0xFFAB47BC)
        price >= 500  -> Color(0xFF42A5F5)
        else          -> Color.White.copy(alpha = 0.15f)
    }
}

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
                is StoreEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
                is StoreEvent.BuySuccess -> onCosmeticChanged()
            }
        }
    }

    val storePets = state.items.filter { it.type.name.contains("PET") }
    val storeThemes = state.items.filter { it.type.name == "APP_THEME" }
    val storeAccessories = state.items.filter { it.type.name == "AVATAR_PART" }

    AuthBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AstraisScreenHeader(
                title = "Tienda",
                trailing = {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.onBackground.copy(alpha = Glassmorphism.BG_TERTIARY))
                            .border(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = Glassmorphism.BORDER_PRIMARY), RoundedCornerShape(16.dp))
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.CurrencyPound,
                            contentDescription = "Ludiones",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "$ludiones",
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = Glassmorphism.TEXT_PRIMARY),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black.copy(alpha = 0.2f))
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StoreTabSelector("Todos", selectedTab == 0, Modifier.weight(1f)) { selectedTab = 0 }
                StoreTabSelector("Compañeros", selectedTab == 1, Modifier.weight(1.3f)) { selectedTab = 1 }
                StoreTabSelector("Accesorios", selectedTab == 2, Modifier.weight(1.2f)) { selectedTab = 2 }
                StoreTabSelector("Temas", selectedTab == 3, Modifier.weight(1f)) { selectedTab = 3 }
            }

            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else if (state.error != null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = state.error!!, color = MaterialTheme.colorScheme.error)
                }
            } else {
                Crossfade(targetState = selectedTab, label = "tab_fade") { tab ->
                    if (state.items.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No hay artículos disponibles.", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f), fontFamily = FontFamily.Monospace)
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
                                                    text = "COLECCIÓN $themeName",
                                                    color = MaterialTheme.colorScheme.tertiary,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Black,
                                                    fontFamily = FontFamily.Monospace,
                                                    modifier = Modifier.padding(horizontal = 4.dp)
                                                )
                                                LazyRow(
                                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                                    contentPadding = PaddingValues(horizontal = 4.dp)
                                                ) {
                                                    items(itemsInTheme, key = { it.id }) { item ->
                                                        if (item.type.name != "APP_THEME") {
                                                            PetStoreCard(item, Modifier.width(160.dp)) { selectedCosmetic = item }
                                                        } else {
                                                            ThemeStoreCard(item, Modifier.width(160.dp)) { selectedCosmetic = item }
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
                                    items(storePets, key = { it.id }) { item -> PetStoreCard(item) { selectedCosmetic = item } }
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
                                                    items(storeAccessories, key = { it.id }) { item -> PetStoreCard(item) { selectedCosmetic = item } }
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
                                                    items(storeThemes, key = { it.id }) { item -> ThemeStoreCard(item) { selectedCosmetic = item } }
                                                }
                                            }
                        }
                    }
                }
            }
        }

        selectedCosmetic?.let { cosmetic ->
            StoreDetailDialog(
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


@Composable
fun StoreTabSelector(text: String, isSelected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val bgColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.Transparent
    val textColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)

    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = textColor, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium, fontFamily = FontFamily.Monospace)
    }
}

@Composable
fun PetStoreCard(item: Cosmetic, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val isOwned = item.owned
    val rarityColor = getRarityColor(item.price)
    val bgColor = Color.Black.copy(alpha = 0.3f)
    val borderColor = if (isOwned) Color.White.copy(alpha=0.05f) else rarityColor.copy(alpha=0.7f)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(90.dp)
                .clip(CircleShape)
                .background(Brush.radialGradient(colors = listOf(
                    if (isOwned) Color.White.copy(alpha=0.03f) else rarityColor.copy(alpha=0.2f),
                    Color.Transparent
                ))),
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
        Text(text = item.name, color = MaterialTheme.colorScheme.onBackground, fontSize = 14.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, maxLines = 1)
        Text(text = item.coleccion.uppercase(), color = if (isOwned) Color.White.copy(alpha=0.4f) else rarityColor, fontSize = 10.sp, fontFamily = FontFamily.Monospace)

        Spacer(modifier = Modifier.height(16.dp))

        if (isOwned) {
            Box(modifier = Modifier.fillMaxWidth().height(36.dp).background(Color.White.copy(alpha=0.05f), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                Text("EN INVENTARIO", color = Color.White.copy(alpha=0.3f), fontSize = 10.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth().height(36.dp).background(MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp)),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.CurrencyPound, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "${item.price}", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun ThemeStoreCard(item: Cosmetic, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val isOwned = item.owned
    val parsedConfig = remember(item.theme) {
        try {
            item.theme?.let { Json { ignoreUnknownKeys = true }.decodeFromString<ThemeConfig>(it) }
        } catch (e: Exception) { null }
    }

    val bgColor = Color.Black.copy(alpha = 0.3f)
    val borderColor = if (isOwned) Color.White.copy(alpha=0.05f) else MaterialTheme.colorScheme.primary.copy(alpha=0.4f)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (parsedConfig != null) {
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(Brush.horizontalGradient(listOf(Color(parsedConfig.background.toColorInt()), Color(
                        parsedConfig.backgroundAlt.toColorInt())))),
                contentAlignment = Alignment.Center
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        StoreColorCircle(parsedConfig.primary, size = 20)
                        StoreColorCircle(parsedConfig.secondary, size = 20)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        StoreColorCircle(parsedConfig.tertiary, size = 20)
                        StoreColorCircle(parsedConfig.error, size = 20)
                    }
                }
            }
        } else {
            Box(modifier = Modifier.size(90.dp).clip(CircleShape).background(Color.Gray))
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text(text = item.name, color = MaterialTheme.colorScheme.onBackground, fontSize = 14.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, maxLines = 1)
        Text(text = "TEMA APP", color = if (isOwned) Color.White.copy(alpha=0.4f) else MaterialTheme.colorScheme.tertiary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)

        Spacer(modifier = Modifier.height(16.dp))

        if (isOwned) {
            Box(modifier = Modifier.fillMaxWidth().height(36.dp).background(Color.White.copy(alpha=0.05f), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                Text("EN INVENTARIO", color = Color.White.copy(alpha=0.3f), fontSize = 10.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth().height(36.dp).background(MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp)),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.CurrencyPound, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "${item.price}", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun StoreColorCircle(hex: String, size: Int = 16) {
    Box(
        modifier = Modifier.size(size.dp).clip(CircleShape).background(Color(hex.toColorInt()))
            .border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape)
    )
}

@Composable
fun StoreDetailDialog(item: Cosmetic, ludiones: Int, onDismiss: () -> Unit, onBuy: () -> Unit) {
    val canAfford = ludiones >= item.price

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f), RoundedCornerShape(24.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.fillMaxWidth().height(140.dp).clip(RoundedCornerShape(16.dp)).background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                if (item.type.name != "APP_THEME") {
                    LottiePetRenderer(assetRef = item.assetRef, modifier = Modifier.size(120.dp))
                } else {
                    val parsedConfig = remember(item.theme) {
                        try {
                            item.theme?.let { Json { ignoreUnknownKeys = true }.decodeFromString<ThemeConfig>(it) }
                        } catch (e: Exception) { null }
                    }
                    if (parsedConfig != null) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            StoreColorCircle(parsedConfig.primary, size = 32)
                            StoreColorCircle(parsedConfig.secondary, size = 32)
                            StoreColorCircle(parsedConfig.tertiary, size = 32)
                            StoreColorCircle(parsedConfig.background, size = 32)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            Text(text = item.name, color = MaterialTheme.colorScheme.onSurface, fontSize = 22.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = item.desc, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontSize = 13.sp, fontFamily = FontFamily.Monospace, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(20.dp))

            if (item.owned) {
                Box(modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(Color.Black.copy(alpha = 0.2f)).padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text("YA EN INVENTARIO", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(if (canAfford) MaterialTheme.colorScheme.primary.copy(alpha=0.15f) else MaterialTheme.colorScheme.error.copy(alpha=0.1f)).padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Filled.CurrencyPound, contentDescription = null, tint = if (canAfford) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "${item.price} LUDIONES", color = if (canAfford) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error, fontSize = 16.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                }
                if (!canAfford) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Fondos insuficientes", color = MaterialTheme.colorScheme.error, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).background(Color.Black.copy(alpha = 0.2f)).clickable { onDismiss() }.padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) { Text("Volver", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontSize = 14.sp, fontFamily = FontFamily.Monospace) }

                val buyBtnColor = if (!item.owned && canAfford) MaterialTheme.colorScheme.primary else Color.Black.copy(alpha = 0.2f)
                val buyTextColor = if (!item.owned && canAfford) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)

                Box(
                    modifier = Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).background(buyBtnColor).clickable(enabled = !item.owned && canAfford) { onBuy() }.padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) { Text(text = if (item.owned) "Comprado" else "Adquirir", color = buyTextColor, fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace) }
            }
        }
    }
}
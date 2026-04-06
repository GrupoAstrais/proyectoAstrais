package com.mm.astraisandroid.ui.tabs

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mm.astraisandroid.util.LottiePetRenderer
import com.mm.astraisandroid.data.api.CosmeticResponse
import com.mm.astraisandroid.ui.auth.components.AuthBackground
import com.mm.astraisandroid.ui.viewmodels.StoreUIState
import com.mm.astraisandroid.ui.viewmodels.StoreViewModel
import com.mm.astraisandroid.data.api.ThemeConfig
import kotlinx.serialization.json.Json

@Composable
fun InventarioTab(
    storeViewModel: StoreViewModel = hiltViewModel(),
    onCosmeticChanged: () -> Unit = {}
) {
    val storeItems by storeViewModel.items.collectAsStateWithLifecycle()
    val uiState by storeViewModel.uiState.collectAsStateWithLifecycle()

    // 0 = Mascotas 1 = Fondos
    var selectedTab by remember { mutableIntStateOf(0) }

    var selectedCosmetic by remember { mutableStateOf<CosmeticResponse?>(null) }

    LaunchedEffect(Unit) { storeViewModel.loadStore() }

    val myItems = storeItems.filter { it.owned }
    val myPets = myItems.filter { it.type == "PET" }
    val myThemes = myItems.filter { it.type == "APP_THEME" }

    AuthBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Mi Inventario",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.08f))
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TabSelector("Compañeros", selectedTab == 0, Modifier.weight(1f)) { selectedTab = 0 }
                TabSelector("Temas y Fondos", selectedTab == 1, Modifier.weight(1f)) { selectedTab = 1 }
            }

            when (uiState) {
                is StoreUIState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
                is StoreUIState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = (uiState as StoreUIState.Error).message, color = MaterialTheme.colorScheme.error)
                    }
                }
                is StoreUIState.Success -> {
                    Crossfade(targetState = selectedTab, label = "tab_fade") { tab ->
                        val currentList = if (tab == 0) myPets else myThemes

                        if (currentList.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(
                                    text = if (tab == 0) "No tienes compañeros aún." else "No tienes fondos desbloqueados.",
                                    color = Color.White.copy(alpha = 0.5f),
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        } else {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(if (tab == 0) 3 else 2),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                contentPadding = PaddingValues(bottom = 90.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(currentList, key = { it.id }) { item ->
                                    if (tab == 0) {
                                        PetInventoryCard(item) { selectedCosmetic = item }
                                    } else {
                                        ThemeInventoryCard(item) { selectedCosmetic = item }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        selectedCosmetic?.let { cosmetic ->
            CosmeticDetailDialog(
                item = cosmetic,
                onDismiss = { selectedCosmetic = null },
                onEquip = {
                    storeViewModel.equipItem(cosmetic.id) {
                        onCosmeticChanged()
                        selectedCosmetic = null
                    }
                },
                onUnequip = {
                    storeViewModel.equipItem(cosmetic.id) {
                        onCosmeticChanged()
                        selectedCosmetic = null
                    }
                }
            )
        }
    }
}


@Composable
fun TabSelector(text: String, isSelected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) Color.White.copy(alpha = 0.15f) else Color.Transparent)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isSelected) Color.White else Color.White.copy(alpha = 0.5f),
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
fun PetInventoryCard(item: CosmeticResponse, onClick: () -> Unit) {
    val isEquipped = item.equipped
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(
                width = if (isEquipped) 2.dp else 1.dp,
                color = if (isEquipped) MaterialTheme.colorScheme.tertiary else Color.White.copy(alpha = 0.1f),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(60.dp).background(if (isEquipped) MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f) else Color.Black.copy(alpha=0.3f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            LottiePetRenderer(assetRef = item.assetRef, modifier = Modifier.size(50.dp))
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(text = item.name, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1, textAlign = TextAlign.Center)

        Spacer(modifier = Modifier.height(8.dp))
        if (isEquipped) {
            Box(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f), RoundedCornerShape(6.dp)).padding(vertical = 4.dp), contentAlignment = Alignment.Center) {
                Text("USANDO", color = MaterialTheme.colorScheme.tertiary, fontSize = 9.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
fun ThemeInventoryCard(item: CosmeticResponse, onClick: () -> Unit) {
    val isEquipped = item.equipped
    val parsedConfig = remember(item.theme) {
        try { Json { ignoreUnknownKeys = true }.decodeFromString<ThemeConfig>(item.theme) }
        catch (e: Exception) { null }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(
                width = if (isEquipped) 2.dp else 1.dp,
                color = if (isEquipped) MaterialTheme.colorScheme.tertiary else Color.White.copy(alpha = 0.1f),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (parsedConfig != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Brush.horizontalGradient(
                        colors = listOf(
                            Color(android.graphics.Color.parseColor(parsedConfig.background)),
                            Color(android.graphics.Color.parseColor(parsedConfig.backgroundAlt))
                        )
                    )),
                contentAlignment = Alignment.Center
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    ColorCircle(parsedConfig.primary)
                    ColorCircle(parsedConfig.secondary)
                    ColorCircle(parsedConfig.tertiary)
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxWidth().height(60.dp).background(Color.Gray, RoundedCornerShape(12.dp)))
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text(text = item.name, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1)

        Spacer(modifier = Modifier.height(12.dp))
        if (isEquipped) {
            Box(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f), RoundedCornerShape(8.dp)).padding(vertical = 6.dp), contentAlignment = Alignment.Center) {
                Text("EQUIPADO", color = MaterialTheme.colorScheme.tertiary, fontSize = 10.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
fun ColorCircle(hex: String, size: Int = 16) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(Color(android.graphics.Color.parseColor(hex)))
            .border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape)
    )
}

@Composable
fun CosmeticDetailDialog(
    item: CosmeticResponse,
    onDismiss: () -> Unit,
    onEquip: () -> Unit,
    onUnequip: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF1A1A2E))
                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(24.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                if (item.type == "PET") {
                    LottiePetRenderer(assetRef = item.assetRef, modifier = Modifier.size(120.dp))
                } else {
                    val parsedConfig = remember(item.theme) {
                        try { Json { ignoreUnknownKeys = true }.decodeFromString<ThemeConfig>(item.theme) }
                        catch (e: Exception) { null }
                    }
                    if (parsedConfig != null) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ColorCircle(parsedConfig.primary, size = 32)
                            ColorCircle(parsedConfig.secondary, size = 32)
                            ColorCircle(parsedConfig.tertiary, size = 32)
                            ColorCircle(parsedConfig.background, size = 32)
                        }
                    } else {
                        Text("Error de tema", color = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = item.name,
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = item.desc,
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White.copy(alpha = 0.05f))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = if(item.type == "PET") "TIPO: COMPAÑERO" else "TIPO: TEMA",
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.06f))
                        .clickable { onDismiss() }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Cerrar", color = Color.White.copy(alpha = 0.6f), fontSize = 14.sp, fontFamily = FontFamily.Monospace)
                }

                val equipBtnColor = if (item.equipped) Color(0xFFFF4C4C).copy(alpha = 0.8f) else Color(0xFFC172FF)

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(equipBtnColor)
                        .clickable {
                            if (item.equipped) onUnequip() else onEquip()
                        }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (item.equipped) "Desequipar" else "Equipar",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}
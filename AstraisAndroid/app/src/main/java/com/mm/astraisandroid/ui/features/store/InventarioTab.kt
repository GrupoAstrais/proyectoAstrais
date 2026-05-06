package com.mm.astraisandroid.ui.features.store


import com.mm.astraisandroid.R
import android.widget.Toast
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
 * Pantalla de inventario personal con filtrado por tipo de cosmético y diálogo de equipamiento.
 *
 * Muestra los artículos adquiridos por el usuario organizados en tabs (Mascotas, Avatares, Temas).
 * Al pulsar un artículo se abre un diálogo de detalle con opción de equipar/desequipar.
 *
 * @param storeViewModel ViewModel de la tienda inyectado por Hilt.
 * @param onCosmeticChanged Callback ejecutado tras equipar/desequipar un artículo (refresca datos del usuario).
 */
@Composable
fun InventarioTab(
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

    val myItems = state.items.filter { it.owned }
    val myPets = myItems.filter { it.type.name.contains("PET") }
    val myAvatars = myItems.filter { it.type.name == "AVATAR_PART" }
    val myThemes = myItems.filter { it.type.name == "APP_THEME" }

    AuthBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AstraisScreenHeader(stringResource(R.string.inventory_title))

            AstraisGlassSurface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                shape = MaterialTheme.shapes.medium,
                backgroundAlpha = Glassmorphism.BG_SECONDARY,
                borderAlpha = Glassmorphism.BORDER_SECONDARY
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    GlassTabSelector(stringResource(R.string.inventory_tab_pets), selectedTab == 0, Modifier.weight(1f)) { selectedTab = 0 }
                    GlassTabSelector(stringResource(R.string.inventory_tab_avatars), selectedTab == 1, Modifier.weight(1f)) { selectedTab = 1 }
                    GlassTabSelector(stringResource(R.string.inventory_tab_themes), selectedTab == 2, Modifier.weight(1f)) { selectedTab = 2 }
                }
            }

            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else {
                Crossfade(targetState = selectedTab, label = "tab_fade") { tab ->
                    val currentList = when (tab) {
                        0 -> myPets
                        1 -> myAvatars
                        else -> myThemes
                    }

                    if (currentList.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White.copy(alpha = 0.03f))
                                .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = when (tab) {
                                    0 -> stringResource(R.string.inventory_empty_pets)
                                     1 -> stringResource(R.string.inventory_empty_avatars)
                                    else -> stringResource(R.string.inventory_empty_themes)
                                },
                                color = Gray300.copy(alpha = Glassmorphism.TEXT_SECONDARY),
                            )
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(if (tab == 2) 2 else 3),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 90.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(currentList, key = { it.id }) { item ->
                                if (tab == 0 || tab == 1) {
                                    GlassPetInventoryCard(item) { selectedCosmetic = item }
                                } else {
                                    GlassThemeInventoryCard(item) { selectedCosmetic = item }
                                }
                            }
                        }
                    }
                }
            }
        }

        selectedCosmetic?.let { cosmetic ->
            GlassCosmeticDetailDialog(
                item = cosmetic,
                onDismiss = { selectedCosmetic = null },
                onEquip = {
                    storeViewModel.equipItem(cosmetic.id, isCurrentlyEquipped = false) {
                        onCosmeticChanged()
                        selectedCosmetic = null
                    }
                },
                onUnequip = {
                    storeViewModel.equipItem(cosmetic.id, isCurrentlyEquipped = true) {
                        onCosmeticChanged()
                        selectedCosmetic = null
                    }
                }
            )
        }
    }
}

/**
 * Botón de tab para el inventario con estilo glassmorphism.
 *
 * @param text Texto descriptivo del tab.
 * @param isSelected Indica si este tab es el seleccionado actualmente.
 * @param modifier Modificador de composición para personalizar layout.
 * @param onClick Acción ejecutada al pulsar el tab.
 */
@Composable
fun GlassTabSelector(text: String, isSelected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) Color.White.copy(alpha = Glassmorphism.BG_TERTIARY) else Color.Transparent)
            .then(
                if (isSelected) Modifier.border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(8.dp)) else Modifier
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isSelected) Color.White.copy(alpha = Glassmorphism.TEXT_PRIMARY) else Gray300.copy(alpha = Glassmorphism.TEXT_TERTIARY / 0.95f),
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

/**
 * Tarjeta de mascota/avatar en el inventario con indicador de equipamiento.
 *
 * @param item Modelo de cosmético a mostrar.
 * @param onClick Acción ejecutada al pulsar la tarjeta.
 */
@Composable
fun GlassPetInventoryCard(item: Cosmetic, onClick: () -> Unit) {
    val isEquipped = item.equipped
    val rarityColor = getRarityColor(item.rarity)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .shadow(
                elevation = if (isEquipped) 8.dp else 4.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = Color.Black.copy(alpha = 0.15f),
                spotColor = if (isEquipped) rarityColor.copy(alpha = 0.15f) else Color.Transparent
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
                width = if (isEquipped) 2.dp else 1.dp,
                brush = Brush.linearGradient(
                    colors = if (isEquipped)
                        listOf(rarityColor.copy(alpha = 0.5f), rarityColor.copy(alpha = 0.15f))
                    else
                        listOf(Color.White.copy(alpha = 0.12f), Color.White.copy(alpha = 0.04f))
                ),
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
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                rarityColor.copy(alpha = 0.15f),
                                Color.Transparent
                            )
                        )
                    )
                    .border(1.dp, rarityColor.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (item.type.name == "AVATAR_PART") {
                    AvatarImageRenderer(
                        assetRef = item.assetRef,
                        initial = item.name.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                        size = 60.dp
                    )
                } else {
                    LottiePetRenderer(assetRef = item.assetRef ?: "", modifier = Modifier.size(60.dp))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = item.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = Glassmorphism.TEXT_PRIMARY),
                maxLines = 1,
                textAlign = TextAlign.Center
            )
            Text(
                text = item.rarity.lowercase().replaceFirstChar { it.uppercase() },
                fontSize = 10.sp,
                color = rarityColor,
                fontWeight = FontWeight.SemiBold
            )

            if (isEquipped) {
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(rarityColor.copy(alpha = 0.12f))
                        .border(1.dp, rarityColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        stringResource(R.string.inventory_equipped_label),
                        style = MaterialTheme.typography.labelSmall,
                        color = rarityColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * Tarjeta de tema de aplicación en el inventario con preview de colores.
 *
 * @param item Modelo de cosmético de tipo APP_THEME a mostrar.
 * @param onClick Acción ejecutada al pulsar la tarjeta.
 */
@Composable
fun GlassThemeInventoryCard(item: Cosmetic, onClick: () -> Unit) {
    val isEquipped = item.equipped
    val rarityColor = getRarityColor(item.rarity)

    val parsedConfig = remember(item.theme) {
        try {
            item.theme?.takeIf { it.isNotBlank() }?.let { Json { ignoreUnknownKeys = true }.decodeFromString<ThemeConfig>(it) }
        } catch (e: Exception) { null }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .shadow(
                elevation = if (isEquipped) 8.dp else 4.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = Color.Black.copy(alpha = 0.15f),
                spotColor = if (isEquipped) rarityColor.copy(alpha = 0.15f) else Color.Transparent
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
                width = if (isEquipped) 2.dp else 1.dp,
                brush = Brush.linearGradient(
                    colors = if (isEquipped)
                        listOf(rarityColor.copy(alpha = 0.5f), rarityColor.copy(alpha = 0.15f))
                    else
                        listOf(Color.White.copy(alpha = 0.12f), Color.White.copy(alpha = 0.04f))
                ),
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
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(
                        if (parsedConfig != null)
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color(parsedConfig.background.toColorInt()).copy(alpha = 0.9f),
                                    Color(parsedConfig.backgroundAlt.toColorInt()).copy(alpha = 0.9f)
                                )
                            )
                        else
                            Brush.horizontalGradient(
                                colors = listOf(Gray300.copy(alpha = 0.2f), Gray300.copy(alpha = 0.1f))
                            )
                    )
                    .border(1.dp, rarityColor.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (parsedConfig != null) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            GlassColorCircle(parsedConfig.primary, 14)
                            GlassColorCircle(parsedConfig.secondary, 14)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            GlassColorCircle(parsedConfig.tertiary, 14)
                            GlassColorCircle(parsedConfig.text, 14)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = item.name,
                color = Color.White.copy(alpha = Glassmorphism.TEXT_PRIMARY),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                textAlign = TextAlign.Center
            )
            Text(
                text = item.rarity.lowercase().replaceFirstChar { it.uppercase() },
                fontSize = 10.sp,
                color = rarityColor,
                fontWeight = FontWeight.SemiBold
            )

            if (isEquipped) {
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(rarityColor.copy(alpha = 0.12f))
                        .border(1.dp, rarityColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        stringResource(R.string.inventory_equipped_label),
                        fontSize = 10.sp,
                        color = rarityColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * Círculo de color para preview de temas en el inventario.
 *
 * @param hex Color en formato hexadecimal.
 * @param size Tamaño del círculo en dp.
 */
@Composable
fun GlassColorCircle(hex: String, size: Int = 16) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(Color(hex.toColorInt()))
            .border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape)
    )
}

/**
 * Diálogo modal de detalle de cosmético en el inventario con opción de equipar/desequipar.
 *
 * Muestra preview del artículo, nombre, descripción, tipo y botón para alternar
 * su estado de equipamiento.
 *
 * @param item Modelo de cosmético a mostrar.
 * @param onDismiss Acción ejecutada al cerrar el diálogo.
 * @param onEquip Acción ejecutada al equipar el artículo.
 * @param onUnequip Acción ejecutada al desequipar el artículo.
 */
@Composable
fun GlassCosmeticDetailDialog(
    item: Cosmetic,
    onDismiss: () -> Unit,
    onEquip: () -> Unit,
    onUnequip: () -> Unit
) {
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
                        LottiePetRenderer(assetRef = item.assetRef ?: "", modifier = Modifier.size(120.dp))
                    } else {
                        val parsedConfig = remember(item.theme) {
                            try {
                                item.theme?.takeIf { it.isNotBlank() }?.let { Json { ignoreUnknownKeys = true }.decodeFromString<ThemeConfig>(it) }
                            } catch (e: Exception) { null }
                        }
                        if (parsedConfig != null) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                GlassColorCircle(parsedConfig.primary, size = 32)
                                GlassColorCircle(parsedConfig.secondary, size = 32)
                                GlassColorCircle(parsedConfig.tertiary, size = 32)
                                GlassColorCircle(parsedConfig.background, size = 32)
                            }
                        } else {
                            Text(stringResource(R.string.inventory_theme_error), color = Gray300)
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

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.06f))
                        .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = when {
                            item.type.name.contains("PET") -> stringResource(R.string.inventory_type_pet)
                            item.type.name == "APP_THEME" -> stringResource(R.string.inventory_type_theme)
                            else -> stringResource(R.string.inventory_type_avatar)
                        },
                        color = Gray300.copy(alpha = 0.4f),
                        fontSize = 10.sp
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
                            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                            .clickable { onDismiss() }
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(stringResource(R.string.dialog_close), color = Gray300, fontSize = 14.sp)
                    }

                    val isEquipped = item.equipped
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isEquipped) MaterialTheme.colorScheme.error.copy(alpha = 0.2f) else MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f))
                            .border(
                                1.dp,
                                if (isEquipped) MaterialTheme.colorScheme.error.copy(alpha = 0.4f) else MaterialTheme.colorScheme.tertiary.copy(alpha = 0.4f),
                                RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                if (isEquipped) onUnequip() else onEquip()
                            }
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isEquipped) stringResource(R.string.inventory_unequip) else stringResource(R.string.inventory_equip),
                            color = if (isEquipped) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

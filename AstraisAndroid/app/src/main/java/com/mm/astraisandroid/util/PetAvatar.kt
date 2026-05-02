package com.mm.astraisandroid.util

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.mm.astraisandroid.data.api.BASE_URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URL

/**
 * Obtiene el archivo de caché local para una mascota específica.
 *
 * @param context Contexto de la aplicación.
 * @param assetRef Identificador único del recurso de la mascota.
 * @return Archivo correspondiente al recurso de la mascota dentro del directorio de caché.
 */
private fun petCacheFile(context: Context, assetRef: String): File {
    val dir = File(context.filesDir, "pets_cache")
    if (!dir.exists()) dir.mkdirs()
    return File(dir, assetRef)
}

/**
 * Asegura que el archivo de animación de la mascota esté disponible en el almacenamiento local.
 *
 * Si el archivo no existe en caché, lo descarga desde el servidor utilizando [BASE_URL].
 * La operación de red se ejecuta en el dispatcher de entrada/salida ([Dispatchers.IO]).
 *
 * @param context Contexto de la aplicación.
 * @param assetRef Identificador único del recurso de la mascota.
 * @return El archivo local si la operación fue exitosa, o `null` si ocurrió un error.
 */
private suspend fun ensurePetCached(context: Context, assetRef: String): File? {
    val file = petCacheFile(context, assetRef)
    if (file.exists()) return file
    return withContext(Dispatchers.IO) {
        runCatching {
            val bytes = URL("$BASE_URL/assets/pets/$assetRef").readBytes()
            file.writeBytes(bytes)
            file
        }.getOrElse {
            Log.e("PetAvatar", "Error cacheando mascota $assetRef: ${it.message}")
            null
        }
    }
}

/**
 * Renderiza una animación Lottie de una mascota.
 *
 * Gestiona automáticamente la descarga y caché local del recurso animado.
 * Muestra un indicador de carga mientras se obtiene el archivo, y mensajes de error
 * si la descarga o la carga de la composición fallan.
 *
 * @param assetRef Identificador único del recurso de la mascota.
 * @param modifier Modificador de Compose para aplicar al contenedor de la animación.
 */
@Composable
fun LottiePetRenderer(assetRef: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var localFile by remember(assetRef) { mutableStateOf<File?>(null) }
    var loadError by remember(assetRef) { mutableStateOf(false) }

    LaunchedEffect(assetRef) {
        loadError = false
        localFile = null
        val file = ensurePetCached(context, assetRef)
        if (file != null) {
            localFile = file
        } else {
            loadError = true
        }
    }

    val spec = localFile?.let { LottieCompositionSpec.File(it.absolutePath) }
    val compositionResult = rememberLottieComposition(spec ?: LottieCompositionSpec.File(""))
    val composition by compositionResult

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        when {
            loadError -> {
                Text(
                    text = "Sin conexion",
                    color = Color.Gray,
                    fontSize = 8.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(4.dp)
                )
            }
            localFile == null || compositionResult.isLoading -> {
                CircularProgressIndicator(
                    color = Color(0xFFC172FF),
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(24.dp)
                )
            }
            compositionResult.error != null -> {
                Text(
                    text = "Error",
                    color = Color.Red,
                    fontSize = 8.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(4.dp)
                )
            }
            else -> {
                LottieAnimation(
                    composition = composition,
                    iterations = LottieConstants.IterateForever,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
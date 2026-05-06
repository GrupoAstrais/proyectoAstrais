package com.mm.astraisandroid.ui.features.minigames

import android.annotation.SuppressLint
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

// 1. El Puente: Esta clase escucha a Javascript
/**
 * Puente entre JavaScript del WebView y Kotlin para recibir resultados de minijuegos.
 *
 * @param onGameFinished Callback ejecutado cuando el minijuego envía su puntuación final.
 */
class WebAppInterface(private val onGameFinished: (Int) -> Unit) {
    /**
     * Método invocado desde JavaScript para enviar la puntuación del minijuego.
     *
     * @param score Puntuación obtenida por el jugador.
     */
    @JavascriptInterface
    fun sendResult(score: Int) {
        onGameFinished(score)
    }
}

/**
 * Pantalla que embebe un minijuego HTML5 en un WebView.
 *
 * Carga un minijuego desde una URL remota y establece un puente JavaScript-Kotlin
 * para recibir la puntuación cuando el juego finaliza.
 *
 * @param url URL del minijuego HTML5 a cargar.
 * @param onScoreSubmit Callback ejecutado con la puntuación final del minijuego.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun ClickerGameScreen(
    url: String,
    onScoreSubmit: (Int) -> Unit
) {
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                webViewClient = WebViewClient() // Evita que se abra en el navegador externo Chrome

                // Conectamos la interfaz de Kotlin con el nombre "Android" en JS
                addJavascriptInterface(WebAppInterface { score ->
                    onScoreSubmit(score)
                }, "Android")

                loadUrl(url)
            }
        }
    )
}
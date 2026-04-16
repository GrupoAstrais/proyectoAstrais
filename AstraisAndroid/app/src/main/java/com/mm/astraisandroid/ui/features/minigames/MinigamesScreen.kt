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
class WebAppInterface(private val onGameFinished: (Int) -> Unit) {
    @JavascriptInterface
    fun sendResult(score: Int) {
        onGameFinished(score)
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun ClickerGameScreen(
    url: String, // La IP de tu Ktor, ej: "http://192.168.1.X:5684/minijuegos/clicker/index.html"
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
package com.mm.astraisandroid.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

/**
 * Efecto Compose que oculta la barra de navegación del sistema y configura
 * el comportamiento de las barras del sistema para mostrarse temporalmente al deslizar.
 *
 * Se ejecuta como [SideEffect] para asegurar que la modificación de la ventana
 * se realice después de que la composición se haya aplicado a la vista.
 */
@Composable
fun HideSystemBarsEffect() {
    val view = LocalView.current

    SideEffect {
        val window = (view.context as android.app.Activity).window
        val insetsController = WindowCompat.getInsetsController(window, view)
        insetsController.hide(WindowInsetsCompat.Type.navigationBars())
        insetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
}

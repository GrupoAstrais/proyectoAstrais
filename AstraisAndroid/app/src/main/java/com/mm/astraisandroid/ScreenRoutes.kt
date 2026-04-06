package com.mm.astraisandroid

/**
 * Define las rutas de navegación de la aplicación.
 *
 * * Se utiliza junto con el NavHost de Jetpack Compose para gestionar el
 * cambio entre pantallas de forma tipada y centralizada.
 *
 * @property route La cadena de texto técnica que identifica la ruta en el
 *    navGraph.
 */
sealed class ScreenRoutes(val route: String) {

    /** Ruta para la pantalla de inicio de sesión. */
    object Login : ScreenRoutes("login")

    /** Ruta para la pantalla de registro de nuevos usuarios. */
    object Register : ScreenRoutes("register")

    /** Ruta principal de la aplicación tras el login. */
    object Home : ScreenRoutes("home")

    /** Ruta para la pantalla de visualización y edición del perfil del usuario. */
    object Profile : ScreenRoutes("profile")
}
package com.mm.astraisandroid

sealed class ScreenRoutes(val route: String) {
    object Login    : ScreenRoutes("login")
    object Register : ScreenRoutes("register")
    object Home     : ScreenRoutes("home")
    object Profile  : ScreenRoutes("profile")
}
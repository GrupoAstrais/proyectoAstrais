package com.mm.astraisandroid.navigation

import kotlinx.serialization.Serializable

/**
 * Define todas las rutas de navegación disponibles en la aplicación.
 * * Cada objeto representa una pantalla o sección específica dentro del grafo de navegación.
 */
sealed interface Route {

    /**
     * Ruta hacia la pantalla de inicio de sesión.
     */
    @Serializable
    data object Login : Route

    /**
     * Ruta hacia la pantalla de registro para nuevos usuarios.
     */
    @Serializable
    data object Register : Route

    /**
     * Ruta hacia la pantalla de configuración inicial del perfil.
     */
    @Serializable
    data object Onboarding : Route

    /**
     * Ruta hacia la pantalla principal de la aplicación.
     * Es el contenedor del `BottomNavigationBar` y los tabs.
     */
    @Serializable
    data object Home : Route

    /**
     * Ruta hacia la pantalla del perfil del usuario.
     */
    @Serializable
    data object Profile : Route

    /**
     * Ruta hacia la pantalla de Inventario.
     */
    @Serializable
    data object Inventory : Route

    /**
     * Ruta hacia el tab de Tareas.
     */
    @Serializable
    data object TasksTab : Route

    /**
     * Ruta hacia el tab de Grupos.
     */
    @Serializable
    data object GroupTab : Route

    /**
     * Ruta hacia la pantalla de detalle de un grupo.
     */
    @Serializable
    data class GroupDetail(
        val gid: Int,
        val role: Int,
        val name: String,
        val description: String
    ) : Route

    /**
     * Pantalla secundaria con tareas administrativas del grupo:
     * editar info, invitaciones, historial y abandonar/eliminar.
     */
    @Serializable
    data class GroupSettings(
        val gid: Int,
        val role: Int,
        val name: String,
        val description: String
    ) : Route

    /**
     * Ruta hacia el tab de la Tienda.
     */
    @Serializable
    data object StoreTab : Route

    /**
     * Ruta hacia la pantalla de Logros.
     */
    @Serializable
    data object Logros : Route
}
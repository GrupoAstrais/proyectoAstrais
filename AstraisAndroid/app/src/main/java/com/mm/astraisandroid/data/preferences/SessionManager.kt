package com.mm.astraisandroid.data.preferences

import kotlinx.coroutines.flow.StateFlow

/**
 * Interfaz para la gestión de sesión de usuario.
 *
 * Abstrae el almacenamiento de tokens, estado de invitado, grupo personal y
 * deep links pendientes. Expone dos [StateFlow] reactivos para que la UI observe
 * cambios de sesión en tiempo real.
 */
interface SessionManager {
    /** `true` si existe una sesión activa (usuario registrado o invitado). */
    val isSessionActive: StateFlow<Boolean>
    /** `true` si la sesión activa es de invitado (sin token de backend). */
    val isGuestSession: StateFlow<Boolean>

    /** Obtiene el token de acceso JWT almacenado, o `null` si no existe. */
    fun getAccessToken(): String?
    /** Obtiene el token de refresco almacenado, o `null` si no existe. */
    fun getRefreshToken(): String?
    /** Obtiene el ID del grupo personal del usuario, o `null` si no está guardado. */
    fun getPersonalGid(): Int?
    /**
     * Obtiene el GID efectivo para operaciones.
     *
     * @return El grupo personal si existe; en caso contrario [GUEST_GID].
     */
    fun getEffectiveGid(): Int

    /** Almacena los tokens de sesión y marca la sesión como no invitada. */
    fun saveTokens(access: String, refresh: String)
    /** Inicia una sesión de invitado local sin comunicación con el servidor. */
    fun startGuestSession()
    /** Guarda el ID del grupo personal del usuario autenticado. */
    fun savePersonalGid(gid: Int)
    /** Promueve una sesión de invitado a usuario registrado sustituyendo los tokens. */
    fun promoteGuestToUser(access: String, refresh: String)

    /** Almacena una URL de deep link para ser procesada tras la autenticación. */
    fun savePendingDeepLink(url: String)
    /** Consume y devuelve la URL de deep link pendiente, o `null` si no hay ninguna. */
    fun consumePendingDeepLink(): String?

    /** Verifica si existe un token de acceso (sesión de usuario registrado). */
    fun hasSession(): Boolean
    /** Verifica si la sesión actual es de invitado. */
    fun isGuest(): Boolean
    /** Verifica si existe cualquier tipo de sesión (registrada o invitada). */
    fun hasAnySession(): Boolean

    /** Limpia todos los datos de sesión (tokens, GID, flags de invitado, deep links). */
    fun clear()

    companion object {
        /** Valor de GID usado para representar una sesión de invitado sin grupo asignado. */
        const val GUEST_GID: Int = -1
    }
}

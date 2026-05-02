package com.mm.astraisandroid.data.preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementación concreta de [SessionManager] que utiliza [EncryptedSharedPreferences]
 * para almacenar tokens y datos de sesión de forma segura.
 *
 * Expone dos [StateFlow] reactivos (`isSessionActive`, `isGuestSession`) que la UI
 * puede observar para reaccionar a cambios de autenticación en tiempo real.
 *
 * @property context Contexto de la aplicación, necesario para crear las preferencias encriptadas.
 */
@Singleton
class SessionManagerImpl @Inject constructor(
    @ApplicationContext context: Context
) : SessionManager {

    private val sharedPreferences: SharedPreferences

    companion object {
        private const val ACCESS_KEY = "access_token"
        private const val REFRESH_KEY = "refresh_token"
        private const val GID_KEY = "personal_gid"
        private const val PENDING_DEEP_LINK = "pending_deep_link"
    }

    private val _isSessionActive = MutableStateFlow(true)
    override val isSessionActive: StateFlow<Boolean> = _isSessionActive.asStateFlow()

    private val _isGuestSession = MutableStateFlow(false)
    override val isGuestSession: StateFlow<Boolean> = _isGuestSession.asStateFlow()

    init {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        sharedPreferences = EncryptedSharedPreferences.create(
            context.applicationContext,
            "secure_session_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        _isGuestSession.value = sharedPreferences.getBoolean("is_guest", false)
        _isSessionActive.value = hasAnySession()
    }

    /**
     * Obtiene el GID efectivo para operaciones, fallback a [SessionManager.GUEST_GID].
     *
     * @return GID personal o valor de invitado.
     */
    override fun getEffectiveGid(): Int = getPersonalGid() ?: SessionManager.GUEST_GID

    /**
     * Almacena los tokens JWT de sesión y marca la sesión como de usuario registrado.
     *
     * @param access Token de acceso.
     * @param refresh Token de refresco.
     */
    override fun saveTokens(access: String, refresh: String) {
        sharedPreferences.edit()?.apply {
            putString(ACCESS_KEY, access)
            putString(REFRESH_KEY, refresh)
            putBoolean("is_guest", false)
            apply()
        }

        _isGuestSession.value = false
        _isSessionActive.value = true
    }

    /**
     * Inicia una sesión de invitado local sin tokens de backend.
     */
    override fun startGuestSession() {
        sharedPreferences.edit()?.putBoolean("is_guest", true)?.apply()
        _isGuestSession.value = true
        _isSessionActive.value = true
    }

    /**
     * Guarda el identificador del grupo personal del usuario.
     *
     * @param gid ID del grupo personal.
     */
    override fun savePersonalGid(gid: Int) {
        sharedPreferences.edit()?.putInt(GID_KEY, gid)?.apply()
    }

    /** @return Token de acceso almacenado o `null`. */
    override fun getAccessToken(): String? = sharedPreferences.getString(ACCESS_KEY, null)
    /** @return Token de refresco almacenado o `null`. */
    override fun getRefreshToken(): String? = sharedPreferences.getString(REFRESH_KEY, null)

    /**
     * Obtiene el GID personal guardado.
     *
     * @return El GID si fue guardado previamente; `null` si el valor por defecto es -1.
     */
    override fun getPersonalGid(): Int? {
        val gid = sharedPreferences.getInt(GID_KEY, -1)
        return if (gid != -1) gid else null
    }

    /**
     * Almacena una URL de deep link para procesarla tras la autenticación.
     *
     * @param url URL del deep link.
     */
    override fun savePendingDeepLink(url: String) {
        sharedPreferences.edit()?.putString(PENDING_DEEP_LINK, url)?.apply()
    }

    /**
     * Consume y devuelve la URL de deep link pendiente.
     *
     * @return La URL almacenada o `null` si no existe; elimina el valor tras leerlo.
     */
    override fun consumePendingDeepLink(): String? {
        val value = sharedPreferences.getString(PENDING_DEEP_LINK, null)
        if (value != null) {
            sharedPreferences.edit()?.remove(PENDING_DEEP_LINK)?.apply()
        }
        return value
    }

    /** @return `true` si existe un token de acceso (sesión de usuario registrado). */
    override fun hasSession(): Boolean = getAccessToken() != null
    /** @return `true` si la sesión actual es de invitado. */
    override fun isGuest(): Boolean = _isGuestSession.value
    /** @return `true` si existe cualquier tipo de sesión (registrada o invitada). */
    override fun hasAnySession(): Boolean = hasSession() || isGuest()

    /**
     * Limpia completamente el almacenamiento de sesión (tokens, GID, flags, deep links)
     * y actualiza los [StateFlow] a `false`.
     */
    override fun clear() {
        sharedPreferences.edit()?.clear()?.apply()
        _isGuestSession.value = false
        _isSessionActive.value = false
    }

    /**
     * Promueve una sesión de invitado a usuario registrado sustituyendo los tokens
     * y eliminando la marca de invitado.
     *
     * @param access Token de acceso del usuario registrado.
     * @param refresh Token de refresco del usuario registrado.
     */
    override fun promoteGuestToUser(access: String, refresh: String) {
        sharedPreferences.edit()?.apply {
            putString(ACCESS_KEY, access)
            putString(REFRESH_KEY, refresh)
            putBoolean("is_guest", false)
            apply()
        }

        _isGuestSession.value = false
        _isSessionActive.value = true
    }
}

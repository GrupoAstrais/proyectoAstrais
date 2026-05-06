package com.mm.astraisandroid.ui.features.profile

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mm.astraisandroid.data.models.User
import com.mm.astraisandroid.data.repository.UserRepository
import com.mm.astraisandroid.data.preferences.SessionManager
import com.mm.astraisandroid.util.LocaleHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Estado de la pantalla de perfil de usuario.
 *
 * @property isLoading Indica si hay una operación de red en curso.
 * @property isOffline `true` si el último intento de sincronización falló.
 * @property user Modelo de usuario cargado, o `null` si no hay datos.
 * @property error Mensaje de error de la última operación fallida, o `null`.
 */
data class UserScreenState(
    val isLoading: Boolean = false,
    val isOffline: Boolean = false,
    val user: User? = null,
    val error: String? = null
)

/**
 * ViewModel de la pantalla de perfil de usuario.
 *
 * Gestiona la carga de datos del usuario, actualización de nombre y preferencias de idioma.
 * Soporta modo invitado con persistencia local de preferencias.
 *
 * @property repository Repositorio de usuario para operaciones de red.
 * @property sessionManager Gestor de sesión para validar si el usuario es invitado.
 * @property appContext Contexto de la aplicación para gestión de idioma.
 */
@HiltViewModel
class UserViewModel @Inject constructor(
    private val repository: UserRepository,
    private val sessionManager: SessionManager,
    @ApplicationContext private val appContext: Context
) : ViewModel() {

    private val _state = MutableStateFlow(UserScreenState())
    val state: StateFlow<UserScreenState> = _state.asStateFlow()

    /**
     * Obtiene los datos del usuario desde el servidor y aplica su preferencia de idioma.
     * Bloqueada para usuarios invitados (guest).
     */
    fun fetchUser() {
        if (sessionManager.isGuest()) return
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val user = repository.getMe()
                user.language?.let { LocaleHelper.setLanguage(appContext, it) }

                _state.update {
                    it.copy(
                        isLoading = false,
                        user = user,
                        isOffline = false
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        isOffline = true,
                        error = e.message
                    )
                }
            }
        }
    }

    /**
     * Actualiza el nombre de usuario en el servidor y en el estado local.
     *
     * @param newName Nuevo nombre del usuario.
     */
    fun updateUsername(newName: String) {
        viewModelScope.launch {
            val user = state.value.user ?: return@launch
            try {
                if (!sessionManager.isGuest()) {
                    repository.updateUsername(user.id, newName)
                }
                _state.update {
                    it.copy(user = user.copy(name = newName))
                }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message) }
            }
        }
    }

    /**
     * Actualiza el perfil del usuario (nombre e idioma) en el servidor.
     * Para invitados, solo aplica el idioma localmente.
     *
     * @param newName Nuevo nombre del usuario.
     * @param language Código de idioma a aplicar (p. ej. "ESP", "ENG").
     * @param onSuccess Callback ejecutado tras la actualización exitosa.
     */
    fun updateProfile(newName: String, language: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val user = state.value.user ?: return@launch
            if (sessionManager.isGuest()) {
                LocaleHelper.setLanguage(appContext, language)
                _state.update { it.copy(user = user.copy(name = newName, language = language)) }
                onSuccess()
                return@launch
            }
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                repository.updateProfile(user.id, newName, language)
                LocaleHelper.setLanguage(appContext, language)
                _state.update {
                    it.copy(
                        isLoading = false,
                        user = user.copy(name = newName, language = language)
                    )
                }
                onSuccess()
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}
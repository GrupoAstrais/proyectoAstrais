package com.mm.astraisandroid.ui.features.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mm.astraisandroid.data.api.LoginRequest
import com.mm.astraisandroid.data.repository.AuthRepository
import com.mm.astraisandroid.data.preferences.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Estados posibles de la pantalla de inicio de sesión.
 */
sealed class LoginUIState {
    /** Estado inicial; no hay operación en curso. */
    object Idle : LoginUIState()
    /** Se está procesando una petición de autenticación. */
    object Loading : LoginUIState()
    /** La última operación falló.
     * @property message Descripción del error. */
    data class Error(val message: String) : LoginUIState()
}

/**
 * Eventos de navegación o notificación emitidos por [LoginViewModel] hacia la UI.
 */
sealed class LoginEvent {
    /** Navegar a la pantalla principal de la aplicación. */
    object NavigateToHome : LoginEvent()
    /** Navegar al flujo de onboarding para completar el perfil. */
    object NavigateToOnboarding : LoginEvent()

}

/**
 * ViewModel de la pantalla de inicio de sesión.
 *
 * Gestiona el estado de autenticación y expone eventos de navegación a través
 * de un [Channel]. Soporta login con credenciales propias, login con Google
 * y sesión de invitado.
 *
 * @property repository Repositorio de autenticación que interactúa con el backend.
 * @property sessionManager Gestor de sesión para almacenar tokens y estado de invitado.
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: AuthRepository,
    private val sessionManager: SessionManager
) : ViewModel() {
    private val _loginState = MutableStateFlow<LoginUIState>(LoginUIState.Idle)
    /** Estado público de la pantalla de login; expuesto como [StateFlow] de solo lectura. */
    val loginState: StateFlow<LoginUIState> = _loginState

    private val _uiEvent = Channel<LoginEvent>()
    /** Flujo de eventos puntuales (navegación, toasts) consumidos por la UI. */
    val uiEvent = _uiEvent.receiveAsFlow()

    /**
     * Inicia sesión con correo y contraseña.
     *
     * @param request Datos de login (email y password).
     */
    fun login(request: LoginRequest) {
        viewModelScope.launch {
            _loginState.value = LoginUIState.Loading
            try {
                val needsOnboarding = repository.login(request)
                if (needsOnboarding) {
                    _uiEvent.send(LoginEvent.NavigateToOnboarding)
                } else {
                    _uiEvent.send(LoginEvent.NavigateToHome)
                }
            } catch (e: Exception) {
                _loginState.value = LoginUIState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    /**
     * Inicia sesión mediante autenticación OAuth con Google.
     *
     * @param idToken Token de identificación proporcionado por Google.
     */
    fun loginWithGoogle(idToken: String) {
        viewModelScope.launch {
            _loginState.value = LoginUIState.Loading
            try {
                val needsOnboarding = repository.loginWithGoogle(idToken)
                if (needsOnboarding) {
                    _uiEvent.send(LoginEvent.NavigateToOnboarding)
                } else {
                    _uiEvent.send(LoginEvent.NavigateToHome)
                }
            } catch (e: Exception) {
                _loginState.value = LoginUIState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    /**
     * Inicia una sesión de invitado local sin comunicación con el servidor.
     */
    fun startGuestSession() {
        sessionManager.startGuestSession()
    }
}
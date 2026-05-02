package com.mm.astraisandroid.ui.features.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mm.astraisandroid.data.api.MailVerifierRequest
import com.mm.astraisandroid.data.api.RegisterRequest
import com.mm.astraisandroid.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Estados posibles de la pantalla de registro.
 */
sealed class RegisterUIState {
    /** Estado inicial; no hay operación en curso. */
    object Idle : RegisterUIState()
    /** Se está procesando una petición de registro o verificación. */
    object Loading : RegisterUIState()
    /** El código de verificación ha sido enviado al correo del usuario. */
    object CodeSent : RegisterUIState()
    /** La última operación falló.
     * @property message Descripción del error. */
    data class Error(val message: String) : RegisterUIState()
}

/**
 * Eventos de navegación o notificación emitidos por [RegisterViewModel] hacia la UI.
 */
sealed class RegisterEvent {
    /** Navegar a la pantalla de inicio de sesión tras registro exitoso. */
    object NavigateToLogin : RegisterEvent()
    /** Navegar al flujo de onboarding tras verificación exitosa. */
    object NavigateToOnboarding : RegisterEvent()
    /** Mostrar un mensaje temporal (toast) al usuario.
     * @property message Texto a mostrar. */
    data class ShowToast(val message: String) : RegisterEvent()
}

/**
 * ViewModel de la pantalla de registro.
 *
 * Gestiona el flujo de creación de cuenta: envío de credenciales, recepción del
 * código de verificación por email y confirmación del mismo. Expone el estado
 * del registro y eventos de navegación a la UI.
 *
 * @property repository Repositorio de autenticación que interactúa con el backend.
 */
@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {
    private val _registerState = MutableStateFlow<RegisterUIState>(RegisterUIState.Idle)
    /** Estado público de la pantalla de registro; expuesto como [StateFlow] de solo lectura. */
    val registerState: StateFlow<RegisterUIState> = _registerState

    private val _uiEvent = Channel<RegisterEvent>()
    /** Flujo de eventos puntuales (navegación, toasts) consumidos por la UI. */
    val uiEvent = _uiEvent.receiveAsFlow()

    private var registeredEmail = ""
    private var registeredPass = ""

    /**
     * Envía la petición de registro al backend.
     *
     * @param request Datos del nuevo usuario (nombre, email, contraseña e idioma).
     */
    fun register(request: RegisterRequest) {
        viewModelScope.launch {
            _registerState.value = RegisterUIState.Loading
            try {
                repository.register(request)
                registeredEmail = request.email
                registeredPass = request.passwd
                _registerState.value = RegisterUIState.CodeSent
            } catch (e: Exception) {
                _registerState.value = RegisterUIState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    /**
     * Verifica el código enviado por email para completar el registro.
     *
     * @param code Código de verificación introducido por el usuario.
     */
    fun verifyCode(code: String) {
        viewModelScope.launch {
            _registerState.value = RegisterUIState.Loading
            try {
                repository.verifyEmail(registeredEmail, code, registeredPass)
                _uiEvent.send(RegisterEvent.NavigateToOnboarding)
            } catch (e: Exception) {
                _registerState.value = RegisterUIState.Error(e.message ?: "Error de verificación")
            }
        }
    }
}
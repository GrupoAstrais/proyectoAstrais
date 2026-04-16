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

sealed class RegisterUIState {
    object Idle : RegisterUIState()
    object Loading : RegisterUIState()
    object CodeSent : RegisterUIState()
    data class Error(val message: String) : RegisterUIState()
}

sealed class RegisterEvent {
    object NavigateToLogin : RegisterEvent()
    data class ShowToast(val message: String) : RegisterEvent()
}

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {
    private val _registerState = MutableStateFlow<RegisterUIState>(RegisterUIState.Idle)
    val registerState: StateFlow<RegisterUIState> = _registerState

    private val _uiEvent = Channel<RegisterEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    var registeredEmail = ""

    fun register(request: RegisterRequest) {
        viewModelScope.launch {
            _registerState.value = RegisterUIState.Loading
            try {
                repository.register(request)
                registeredEmail = request.email
                _registerState.value = RegisterUIState.CodeSent
            } catch (e: Exception) {
                _registerState.value = RegisterUIState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun verifyCode(code: String) {
        viewModelScope.launch {
            _registerState.value = RegisterUIState.Loading
            try {
                repository.verifyEmail(registeredEmail, code)

                _uiEvent.send(RegisterEvent.NavigateToLogin)
            } catch (e: Exception) {
                _registerState.value = RegisterUIState.Error(e.message ?: "Error de verificación")
            }
        }
    }
}
package com.mm.astraisandroid.api

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mm.astraisandroid.data.api.BackendRepository
import com.mm.astraisandroid.data.api.MailVerifierRequest
import com.mm.astraisandroid.data.api.RegisterRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class RegisterUIState {
    object Idle : RegisterUIState()
    object Loading : RegisterUIState()
    object CodeSent : RegisterUIState()
    object RegisterSuccess : RegisterUIState()
    data class RegisterError(val message: String) : RegisterUIState()
}

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val backendRepository: BackendRepository
) : ViewModel() {
    private val _registerState = MutableStateFlow<RegisterUIState>(RegisterUIState.Idle)
    val registerState: StateFlow<RegisterUIState> = _registerState

    var registeredEmail = ""

    fun register(request: RegisterRequest) {
        viewModelScope.launch {
            _registerState.value = RegisterUIState.Loading
            try {
                backendRepository.performRegister(request)
                registeredEmail = request.email
                _registerState.value = RegisterUIState.CodeSent
            } catch (e: Exception) {
                _registerState.value = RegisterUIState.RegisterError(e.message ?: "Error")
            }
        }
    }

    fun verifyCode(code: String) {
        viewModelScope.launch {
            _registerState.value = RegisterUIState.Loading
            try {
                backendRepository.verifyEmail(MailVerifierRequest(registeredEmail, code))
                _registerState.value = RegisterUIState.RegisterSuccess
            } catch (e: Exception) {
                _registerState.value = RegisterUIState.RegisterError(e.message ?: "Error de verificación")
            }
        }
    }

    fun resetState() { _registerState.value = RegisterUIState.Idle }
}
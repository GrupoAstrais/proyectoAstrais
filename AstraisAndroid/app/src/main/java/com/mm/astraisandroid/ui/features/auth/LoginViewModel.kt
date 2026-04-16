package com.mm.astraisandroid.ui.features.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mm.astraisandroid.data.api.LoginRequest
import com.mm.astraisandroid.data.api.RegisterRequest
import com.mm.astraisandroid.data.preferences.SessionManager
import com.mm.astraisandroid.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class LoginUIState {
    object Idle : LoginUIState()
    object Loading : LoginUIState()
    data class Error(val message: String) : LoginUIState()
}

sealed class LoginEvent {
    object NavigateToHome : LoginEvent()
    data class ShowToast(val message: String) : LoginEvent()
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {
    private val _loginState = MutableStateFlow<LoginUIState>(LoginUIState.Idle)
    val loginState: StateFlow<LoginUIState> = _loginState

    private val _uiEvent = Channel<LoginEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    fun login(request: LoginRequest) {
        viewModelScope.launch {
            _loginState.value = LoginUIState.Loading
            try {
                repository.login(request)
                _uiEvent.send(LoginEvent.NavigateToHome)
            } catch (e: Exception) {
                _loginState.value = LoginUIState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun loginWithGoogle(idToken: String) {
        viewModelScope.launch {
            _loginState.value = LoginUIState.Loading
            try {
                repository.loginWithGoogle(idToken)
                _uiEvent.send(LoginEvent.NavigateToHome)
            } catch (e: Exception) {
                _loginState.value = LoginUIState.Error(e.message ?: "Error desconocido")
            }
        }
    }
}
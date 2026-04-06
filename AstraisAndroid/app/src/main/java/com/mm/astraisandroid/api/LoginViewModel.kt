package com.mm.astraisandroid.api

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mm.astraisandroid.data.api.BackendRepository
import com.mm.astraisandroid.data.api.LoginRequest
import com.mm.astraisandroid.data.api.RegisterRequest
import com.mm.astraisandroid.data.preferences.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class LoginUIState {
    object Idle : LoginUIState()
    object Loading : LoginUIState()
    object LoginSuccess : LoginUIState()
    data class LoginError(val message: String) : LoginUIState()
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val backendRepository: BackendRepository
) : ViewModel() {
    private val _loginState = MutableStateFlow<LoginUIState>(LoginUIState.Idle)
    val loginState: StateFlow<LoginUIState> = _loginState

    fun login(request: LoginRequest) {
        viewModelScope.launch {
            _loginState.value = LoginUIState.Loading
            try {
                val response = backendRepository.performLogin(request)
                SessionManager.saveTokens(response.jwtAccessToken, response.jwtRefreshToken)
                val me = backendRepository.getMe()
                me.personalGid?.let { SessionManager.savePersonalGid(it) }
                _loginState.value = LoginUIState.LoginSuccess
            } catch (e: Exception) {
                _loginState.value = LoginUIState.LoginError(e.message ?: "Error desconocido")
            }
        }
    }

    fun testRegister(request: RegisterRequest){
        viewModelScope.launch {
            _loginState.value = LoginUIState.Loading
            try {
                backendRepository.performRegister(request)
                _loginState.value = LoginUIState.Idle
            } catch (e: Exception) {
                _loginState.value = LoginUIState.LoginError(e.message ?: "Error")
            }
        }
    }
    fun resetState() { _loginState.value = LoginUIState.Idle }
}
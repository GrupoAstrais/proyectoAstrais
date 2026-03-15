package com.mm.astrais_android

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    data class Success(val token: String) : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}

class LoginViewModel : ViewModel() {

    private val apiService = ApiService()

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading

            val request = RegisterRequest(name = "Tets", email = email, passwd = password, lang = "ESP")
            val result = apiService.register(request)

            result
                .onSuccess { body ->
                    _uiState.value = LoginUiState.Success("")
                }
                .onFailure { error ->
                    _uiState.value = LoginUiState.Error(error.message ?: "Error de conexión")
                    Log.e("LOGIN", "Error: ${error.message}", error)
                }
        }
    }

    fun resetState() {
        _uiState.value = LoginUiState.Idle
    }
}
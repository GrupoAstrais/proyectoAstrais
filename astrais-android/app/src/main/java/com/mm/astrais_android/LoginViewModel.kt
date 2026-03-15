package com.mm.astrais_android

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

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading

            try {
                val request = LoginRequest(name= "Test", email, password, lang="ESP")
                val response = RetrofitClient.apiService.login(request)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true) {
                        _uiState.value = LoginUiState.Success(body.token ?: "")
                    } else {
                        _uiState.value = LoginUiState.Error(body?.message ?: "Error desconocido")
                    }
                } else {
                    _uiState.value = LoginUiState.Error("Error ${response.code()}: ${response.message()} ${response.raw()}")
                }
            } catch (e: Exception) {
                _uiState.value = LoginUiState.Error("Error de conexión: ${e.message}")
            }
        }
    }

    fun resetState() {
        _uiState.value = LoginUiState.Idle
    }
}
package com.mm.astraisandroid.api

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mm.astraisandroid.data.api.BackendRepository
import com.mm.astraisandroid.data.api.RegisterRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class RegisterUIState {
    object Idle : RegisterUIState()
    object Loading : RegisterUIState()
    object RegisterSuccess : RegisterUIState()
    data class RegisterError(val message: String) : RegisterUIState()
}

class RegisterViewModel : ViewModel() {

    private val _registerState = MutableStateFlow<RegisterUIState>(RegisterUIState.Idle)
    val registerState: StateFlow<RegisterUIState> = _registerState

    fun register(request: RegisterRequest) {
        viewModelScope.launch {
            _registerState.value = RegisterUIState.Loading

            BackendRepository.performRegister(request)
                .onSuccess {
                    _registerState.value = RegisterUIState.RegisterSuccess
                }
                .onFailure { error ->
                    Log.d("MM", "Register error: $error")
                    _registerState.value = RegisterUIState.RegisterError(
                        error.message ?: "Error desconocido"
                    )
                }
        }
    }

    fun resetState() {
        _registerState.value = RegisterUIState.Idle
    }
}
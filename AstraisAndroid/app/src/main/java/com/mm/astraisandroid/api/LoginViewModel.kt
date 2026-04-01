package com.mm.astraisandroid.api

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mm.astraisandroid.SessionDataStore
import com.mm.astraisandroid.TokenHolder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class LoginUIState {
    object Idle : LoginUIState()
    object Loading : LoginUIState()
    object LoginSuccess : LoginUIState()
    data class LoginError(val message: String) : LoginUIState()
}

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val session = SessionDataStore(application)
    private val _loginState = MutableStateFlow<LoginUIState>(LoginUIState.Idle)
    val loginState: StateFlow<LoginUIState> = _loginState

    fun login(request: LoginRequest) {
        viewModelScope.launch {
            _loginState.value = LoginUIState.Loading

            BackendRepository.performLogin(request)
                .onSuccess { response ->
                    TokenHolder.setAccessToken(response.jwtAccessToken)
                    TokenHolder.setRefreshToken(response.jwtRefreshToken)
                    session.saveTokens(response.jwtAccessToken, response.jwtRefreshToken)

                    BackendRepository.getMe()
                        .onSuccess { me ->
                            TokenHolder.setPersonalGid(me.personalGid)
                            me.personalGid?.let { session.savePersonalGid(it) }
                        }

                    _loginState.value = LoginUIState.LoginSuccess
                }
                .onFailure { d ->
                    Log.d("MM", "ktor error: $d")
                    _loginState.value = LoginUIState.LoginError(d.message ?: "Error desconocido")
                }
        }
    }

    fun testRegister(request: RegisterRequest){
        viewModelScope.launch {
            _loginState.value = LoginUIState.Loading

            BackendRepository.performRegister(request)
                .onSuccess { response ->
                    _loginState.value = LoginUIState.Idle
                }
                .onFailure { d->
                    Log.d("MM", "ktor error: $d")
                    _loginState.value = LoginUIState.LoginError(d.message ?: "Error desconocido")
                }
        }
    }

    fun resetState() {
        _loginState.value = LoginUIState.Idle
    }
}
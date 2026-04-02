package com.mm.astraisandroid

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mm.astraisandroid.api.BackendRepository
import com.mm.astraisandroid.api.UserMeResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class UserViewModel : ViewModel() {
    private val _userData = MutableStateFlow<UserMeResponse?>(null)
    val userData: StateFlow<UserMeResponse?> = _userData

    fun fetchUser() {
        viewModelScope.launch {
            BackendRepository.getMe().onSuccess { user ->
                _userData.value = user
            }
        }
    }
}
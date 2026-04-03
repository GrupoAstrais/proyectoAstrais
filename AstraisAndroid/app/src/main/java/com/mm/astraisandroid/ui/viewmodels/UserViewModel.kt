package com.mm.astraisandroid.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mm.astraisandroid.AppCache
import com.mm.astraisandroid.data.api.BackendRepository
import com.mm.astraisandroid.data.api.UserMeResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class UserViewModel : ViewModel() {
    private val _userData = MutableStateFlow<UserMeResponse?>(null)
    val userData: StateFlow<UserMeResponse?> = _userData

    private val _isOffline = MutableStateFlow(false)
    val isOffline: StateFlow<Boolean> = _isOffline

    fun fetchUser() {
        viewModelScope.launch {
            BackendRepository.getMe()
                .onSuccess { user ->
                    _userData.value = user
                    _isOffline.value = false
                    AppCache.saveUser(user)
                }
                .onFailure {
                    _isOffline.value = true
                    val cachedUser = AppCache.getUser()
                    if (cachedUser != null) {
                        _userData.value = cachedUser
                    }
                }
        }
    }
}
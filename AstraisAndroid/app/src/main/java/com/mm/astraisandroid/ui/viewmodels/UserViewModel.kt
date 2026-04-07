package com.mm.astraisandroid.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mm.astraisandroid.data.api.BackendRepository
import com.mm.astraisandroid.data.api.UserMeResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val backendRepository: BackendRepository
) : ViewModel() {
    private val _userData = MutableStateFlow<UserMeResponse?>(null)
    val userData: StateFlow<UserMeResponse?> = _userData

    private val _isOffline = MutableStateFlow(false)
    val isOffline: StateFlow<Boolean> = _isOffline

    fun fetchUser() {
        viewModelScope.launch {
            try {
                val user = backendRepository.getMe()
                _userData.value = user
                _isOffline.value = false
            } catch (e: Exception) {
                _isOffline.value = true
            }
        }
    }
}
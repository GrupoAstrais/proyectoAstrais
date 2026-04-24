package com.mm.astraisandroid.ui.features.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mm.astraisandroid.data.api.UserMeResponse
import com.mm.astraisandroid.data.models.User
import com.mm.astraisandroid.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UserScreenState(
    val isLoading: Boolean = false,
    val isOffline: Boolean = false,
    val user: User? = null,
    val error: String? = null
)

@HiltViewModel
class UserViewModel @Inject constructor(
    private val repository: UserRepository
) : ViewModel() {

    private val _state = MutableStateFlow(UserScreenState())
    val state: StateFlow<UserScreenState> = _state.asStateFlow()

    fun fetchUser() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val user = repository.getMe()

                _state.update {
                    it.copy(
                        isLoading = false,
                        user = user,
                        isOffline = false
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        isOffline = true,
                        error = e.message
                    )
                }
            }
        }
    }

    fun updateUsername(newName: String) {
        viewModelScope.launch {
            val user = state.value.user ?: return@launch
            try {
                if (!com.mm.astraisandroid.data.preferences.SessionManager.isGuest()) {
                    repository.updateUsername(user.id, newName)
                }
                // Update local state immediately
                _state.update {
                    it.copy(user = user.copy(name = newName))
                }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message) }
            }
        }
    }
}
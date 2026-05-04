package com.mm.astraisandroid.ui.features.profile

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mm.astraisandroid.data.models.User
import com.mm.astraisandroid.data.repository.UserRepository
import com.mm.astraisandroid.data.preferences.SessionManager
import com.mm.astraisandroid.util.LocaleHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
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
    private val repository: UserRepository,
    private val sessionManager: SessionManager,
    @ApplicationContext private val appContext: Context
) : ViewModel() {

    private val _state = MutableStateFlow(UserScreenState())
    val state: StateFlow<UserScreenState> = _state.asStateFlow()

    fun fetchUser() {
        if (sessionManager.isGuest()) return
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val user = repository.getMe()
                user.language?.let { LocaleHelper.setLanguage(appContext, it) }

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
                if (!sessionManager.isGuest()) {
                    repository.updateUsername(user.id, newName)
                }
                _state.update {
                    it.copy(user = user.copy(name = newName))
                }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message) }
            }
        }
    }

    fun updateProfile(newName: String, language: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val user = state.value.user ?: return@launch
            if (sessionManager.isGuest()) {
                LocaleHelper.setLanguage(appContext, language)
                _state.update { it.copy(user = user.copy(name = newName, language = language)) }
                onSuccess()
                return@launch
            }
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                repository.updateProfile(user.id, newName, language)
                LocaleHelper.setLanguage(appContext, language)
                _state.update {
                    it.copy(
                        isLoading = false,
                        user = user.copy(name = newName, language = language)
                    )
                }
                onSuccess()
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}
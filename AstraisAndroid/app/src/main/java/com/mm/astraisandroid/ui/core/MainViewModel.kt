package com.mm.astraisandroid.ui.core

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mm.astraisandroid.data.preferences.SessionManager
import com.mm.astraisandroid.data.repository.AuthRepository
import com.mm.astraisandroid.data.repository.GroupRepository
import com.mm.astraisandroid.util.logging.AppLogger
import com.mm.astraisandroid.util.logging.LogFeature
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class MainUiEvent {
    data class ShowToast(val message: String) : MainUiEvent()
}

data class MainUiState(
    val isSessionActive: Boolean = false,
    val isGuest: Boolean = false,
    val shouldNavigateToOnboarding: Boolean = false,
    val pendingDeepLink: String? = null
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val groupRepository: GroupRepository,
    private val sessionManager: SessionManager,
    private val logger: AppLogger
) : ViewModel() {

    private val _state = MutableStateFlow(
        MainUiState(
            isSessionActive = sessionManager.hasAnySession(),
            isGuest = sessionManager.isGuest()
        )
    )
    val state: StateFlow<MainUiState> = _state.asStateFlow()

    private val _events = Channel<MainUiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        viewModelScope.launch {
            sessionManager.isSessionActive.collect { active ->
                _state.update { it.copy(isSessionActive = active, isGuest = sessionManager.isGuest()) }
            }
        }
        viewModelScope.launch {
            checkOnboarding()
        }
    }

    fun onLogout() {
        viewModelScope.launch {
            logger.i(LogFeature.AUTH, "Logout initiated")
            authRepository.logout()
        }
    }

    fun onDeepLinkReceived(url: String) {
        viewModelScope.launch {
            logger.i(LogFeature.AUTH, "Deep link received: $url")
            val isJoinLink = runCatching { android.net.Uri.parse(url) }.getOrNull()?.let { uri ->
                val isCustom = uri.scheme == "astrais" && uri.host == "groups" && uri.path?.startsWith("/join") == true
                val isHttps = uri.scheme == "https" && uri.host == "astrais.app" && uri.path?.startsWith("/groups/join") == true
                isCustom || isHttps
            } ?: false

            if (!isJoinLink) {
                logger.w(LogFeature.AUTH, "Ignoring non-join deep link: $url")
                return@launch
            }

            if (!state.value.isSessionActive || sessionManager.isGuest()) {
                sessionManager.savePendingDeepLink(url)
                _events.send(MainUiEvent.ShowToast("Inicia sesión para usar la invitación"))
                return@launch
            }

            handleJoinLink(url)
        }
    }

    private suspend fun handleJoinLink(url: String) {
        val uri = android.net.Uri.parse(url)
        val code = uri.getQueryParameter("code")
        val ok = runCatching {
            if (!code.isNullOrBlank()) groupRepository.joinByCode(code)
            else groupRepository.joinByUrl(url)
            true
        }.getOrDefault(false)

        if (ok) {
            logger.i(LogFeature.AUTH, "Joined group via deep link")
            _events.send(MainUiEvent.ShowToast("Te has unido al grupo"))
            _state.update { it.copy(pendingDeepLink = "navigate_to_groups") }
        } else {
            logger.e(LogFeature.AUTH, "Failed to join group via deep link")
            _events.send(MainUiEvent.ShowToast("No se pudo usar el enlace de invitación"))
        }
    }

    fun consumePendingDeepLink() {
        viewModelScope.launch {
            if (!state.value.isSessionActive || sessionManager.isGuest()) return@launch
            val pendingUrl = sessionManager.consumePendingDeepLink() ?: return@launch
            handleJoinLink(pendingUrl)
        }
    }

    suspend fun checkOnboarding() {
        if (!state.value.isSessionActive || sessionManager.isGuest()) return
        val shouldGo = runCatching { authRepository.needsOnboarding() }.getOrDefault(false)
        if (shouldGo) {
            logger.i(LogFeature.AUTH, "Onboarding required")
            _state.update { it.copy(shouldNavigateToOnboarding = true) }
        }
    }

    fun onOnboardingConsumed() {
        _state.update { it.copy(shouldNavigateToOnboarding = false) }
    }

    fun onDeepLinkConsumed() {
        _state.update { it.copy(pendingDeepLink = null) }
    }
}

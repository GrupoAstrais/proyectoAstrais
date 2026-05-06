package com.mm.astraisandroid.ui.core

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mm.astraisandroid.data.preferences.SessionManager
import com.mm.astraisandroid.data.repository.AuthRepository
import com.mm.astraisandroid.data.repository.GroupRepository
import com.mm.astraisandroid.ui.components.SnackbarManager
import com.mm.astraisandroid.util.logging.AppLogger
import com.mm.astraisandroid.util.logging.LogFeature
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Estado de la interfaz de usuario gestionado por [MainViewModel].
 *
 * @property isSessionActive Indica si hay una sesión activa (registrada o invitada).
 * @property isGuest Indica si la sesión activa es de invitado.
 * @property shouldNavigateToOnboarding Flag que señala si se debe navegar a la pantalla de onboarding.
 * @property pendingDeepLink URL de deep link pendiente de procesamiento tras autenticación.
 */
data class MainUiState(
    val isSessionActive: Boolean = false,
    val isGuest: Boolean = false,
    val shouldNavigateToOnboarding: Boolean = false,
    val pendingDeepLink: String? = null
)

/**
 * ViewModel global que gestiona el estado de sesión, deep links y navegación a onboarding.
 *
 * Observa los cambios de sesión desde [SessionManager] y coordina las acciones
 * de logout, procesamiento de enlaces de invitación y verificación de onboarding.
 *
 * @property authRepository Repositorio de autenticación para logout y verificación de onboarding.
 * @property groupRepository Repositorio de grupos para unirse mediante deep links.
 * @property sessionManager Gestor de sesión para consultar y observar estado de autenticación.
 * @property logger Sistema de logging estructurado.
 * @property snackbarManager Gestor de mensajes tipo snackbar para notificaciones al usuario.
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val groupRepository: GroupRepository,
    private val sessionManager: SessionManager,
    private val logger: AppLogger,
    private val snackbarManager: SnackbarManager
) : ViewModel() {

    private val _state = MutableStateFlow(
        MainUiState(
            isSessionActive = sessionManager.hasAnySession(),
            isGuest = sessionManager.isGuest()
        )
    )
    val state: StateFlow<MainUiState> = _state.asStateFlow()

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

    /**
     * Inicia el proceso de cierre de sesión en una corrutina del ViewModelScope.
     * Limpia tokens, datos locales y detiene la sincronización.
     */
    fun onLogout() {
        viewModelScope.launch {
            logger.i(LogFeature.AUTH, "Logout initiated")
            authRepository.logout()
        }
    }

    /**
     * Procesa un deep link recibido. Si es un enlace de unión a grupo y el usuario
     * está autenticado, lo procesa inmediatamente. Si no, lo almacena para después del login.
     *
     * @param url URL del deep link recibido.
     */
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
                snackbarManager.showMessage("Inicia sesión para usar la invitación")
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
            snackbarManager.showMessage("Te has unido al grupo")
            _state.update { it.copy(pendingDeepLink = "navigate_to_groups") }
        } else {
            logger.e(LogFeature.AUTH, "Failed to join group via deep link")
            snackbarManager.showMessage("No se pudo usar el enlace de invitación")
        }
    }

    /**
     * Intenta procesar un deep link de unión a grupo almacenado previamente.
     * Solo se ejecuta si hay una sesión activa no invitada.
     */
    fun consumePendingDeepLink() {
        viewModelScope.launch {
            if (!state.value.isSessionActive || sessionManager.isGuest()) return@launch
            val pendingUrl = sessionManager.consumePendingDeepLink() ?: return@launch
            handleJoinLink(pendingUrl)
        }
    }

    /**
     * Verifica si el usuario autenticado necesita completar el onboarding.
     * Si es así, actualiza el estado para disparar la navegación.
     */
    suspend fun checkOnboarding() {
        if (!state.value.isSessionActive || sessionManager.isGuest()) return
        val shouldGo = runCatching { authRepository.needsOnboarding() }.getOrDefault(false)
        if (shouldGo) {
            logger.i(LogFeature.AUTH, "Onboarding required")
            _state.update { it.copy(shouldNavigateToOnboarding = true) }
        }
    }

    /**
     * Marca la navegación a onboarding como consumida, reseteando la flag de estado.
     */
    fun onOnboardingConsumed() {
        _state.update { it.copy(shouldNavigateToOnboarding = false) }
    }

    /**
     * Marca el deep link pendiente como consumido, reseteando la URL en el estado.
     */
    fun onDeepLinkConsumed() {
        _state.update { it.copy(pendingDeepLink = null) }
    }
}

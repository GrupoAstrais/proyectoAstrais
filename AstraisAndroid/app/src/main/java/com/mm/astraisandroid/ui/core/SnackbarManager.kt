package com.mm.astraisandroid.ui.components

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Evento de notificación mostrado mediante un Snackbar.
 *
 * @property message Mensaje principal a mostrar al usuario.
 * @property actionLabel Texto opcional para el botón de acción del snackbar.
 */
data class SnackbarEvent(
    val message: String,
    val actionLabel: String? = null
)

/**
 * Interfaz para la gestión centralizada de mensajes Snackbar.
 *
 * Expone un [Flow] reactivo que los consumidores pueden observar para mostrar
 * notificaciones efímeras al usuario.
 */
interface SnackbarManager {
    /** Flujo de eventos snackbar emitidos por la aplicación. */
    val messages: Flow<SnackbarEvent>
    /**
     * Envía un mensaje para ser mostrado como snackbar.
     * @param message Texto del mensaje.
     * @param actionLabel Texto opcional para el botón de acción.
     */
    suspend fun showMessage(message: String, actionLabel: String? = null)
}

/**
 * Implementación singleton de [SnackbarManager] que utiliza un [Channel] buffered
 * para emitir eventos snackbar de forma asíncrona.
 */
@Singleton
class SnackbarManagerImpl @Inject constructor() : SnackbarManager {
    /** Canal buffered que almacena eventos snackbar pendientes de consumo. */
    private val _messages = Channel<SnackbarEvent>(Channel.BUFFERED)
    /** Flujo de eventos expuesto a los consumidores como [Flow]. */
    override val messages = _messages.receiveAsFlow()
    /**
     * Envía un evento snackbar al canal.
     * @param message Texto del mensaje.
     * @param actionLabel Texto opcional para el botón de acción.
     */
    override suspend fun showMessage(message: String, actionLabel: String?) {
        _messages.send(SnackbarEvent(message, actionLabel))
    }
}

/**
 * ViewModel que expone los eventos del [SnackbarManager] para ser observados
 * desde composables. Actúa como puente entre el manager singleton y la UI.
 *
 * @property snackbarManager Manager singleton inyectado para enviar y recibir eventos.
 */
@HiltViewModel
class GlobalSnackbarViewModel @Inject constructor(
    private val snackbarManager: SnackbarManager
) : ViewModel() {
    /** Flujo de eventos snackbar delegados al manager. */
    val snackbarEvents = snackbarManager.messages

    /**
     * Envía un mensaje snackbar a través del manager.
     * @param message Texto del mensaje.
     * @param actionLabel Texto opcional para el botón de acción.
     */
    fun showMessage(message: String, actionLabel: String? = null) {
        viewModelScope.launch {
            snackbarManager.showMessage(message, actionLabel)
        }
    }
}
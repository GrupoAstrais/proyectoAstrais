package com.mm.astraisandroid.ui.features.store

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mm.astraisandroid.data.models.Cosmetic
import com.mm.astraisandroid.data.preferences.SessionManager
import com.mm.astraisandroid.data.repository.StoreRepository
import com.mm.astraisandroid.ui.components.SnackbarManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Estado de la pantalla de tienda/inventario.
 *
 * @property isLoading Indica si hay una operación de red en curso.
 * @property items Lista de cosméticos cargados desde el servidor.
 * @property error Mensaje de error de la última operación fallida, o `null`.
 */
data class StoreScreenState(
    val isLoading: Boolean = false,
    val items: List<Cosmetic> = emptyList(),
    val error: String? = null
)

/**
 * Eventos de UI emitidos por el ViewModel de la tienda.
 */
sealed class StoreEvent {
    /** Evento emitido tras una compra exitosa. */
    object BuySuccess : StoreEvent()
}

/**
 * ViewModel de la tienda e inventario de cosméticos.
 *
 * Gestiona la carga de artículos disponibles, compra de cosméticos con Ludiones
 * y equipamiento/desequipamiento de artículos adquiridos.
 *
 * @property repository Repositorio de la tienda para operaciones de red.
 * @property sessionManager Gestor de sesión para validar si el usuario es invitado.
 * @property snackbarManager Gestor de snackbars para mostrar notificaciones transitorias.
 */
@HiltViewModel
class StoreViewModel @Inject constructor(
    private val repository: StoreRepository,
    private val sessionManager: SessionManager,
    private val snackbarManager: SnackbarManager
) : ViewModel() {

    private val _state = MutableStateFlow(StoreScreenState())
    val state: StateFlow<StoreScreenState> = _state.asStateFlow()

    private val _uiEvent = MutableStateFlow<StoreEvent?>(null)
    val uiEvent: StateFlow<StoreEvent?> = _uiEvent.asStateFlow()

    /**
     * Carga los artículos disponibles en la tienda desde el servidor.
     * Bloqueada para usuarios invitados (guest).
     */
    fun loadStore() {
        if (sessionManager.isGuest()) return
        viewModelScope.launch {
            refreshStore()
        }
    }

    private suspend fun refreshStore() {
        _state.update { it.copy(isLoading = true, error = null) }
        try {
            val items = repository.getStoreItems()
            _state.update { it.copy(isLoading = false, items = items) }
        } catch (e: Exception) {
            _state.update { it.copy(isLoading = false) }
            snackbarManager.showMessage(e.message ?: "Error de conexión")
        }
    }

    /**
     * Compra un cosmético con Ludiones y refresca la lista de artículos.
     *
     * @param id Identificador del cosmético a comprar.
     * @param onSuccess Callback ejecutado tras la compra exitosa.
     */
    fun buyItem(id: Int, onSuccess: () -> Unit) {
        if (sessionManager.isGuest()) return
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                repository.buyCosmetic(id)
                refreshStore()
                onSuccess()
                _uiEvent.value = StoreEvent.BuySuccess
                snackbarManager.showMessage("¡Compra realizada con éxito!")
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false) }
                snackbarManager.showMessage(e.message ?: "Fondos insuficientes")
            }
        }
    }

    /**
     * Equipa o desequipa un cosmético adquirido.
     *
     * @param id Identificador del cosmético.
     * @param isCurrentlyEquipped `true` si el artículo está actualmente equipado (para desequiparlo).
     * @param onSuccess Callback ejecutado tras la operación exitosa.
     */
    fun equipItem(id: Int, isCurrentlyEquipped: Boolean, onSuccess: () -> Unit) {
        if (sessionManager.isGuest()) return
        viewModelScope.launch {
            try {
                repository.equipCosmetic(id)
                refreshStore()
                onSuccess()
                val msg = if (isCurrentlyEquipped) "Desequipado" else "¡Equipado con éxito!"
                snackbarManager.showMessage(msg)
            } catch (e: Exception) {
                snackbarManager.showMessage(e.message ?: "Error al equipar")
            }
        }
    }
}

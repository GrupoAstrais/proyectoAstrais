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

data class StoreScreenState(
    val isLoading: Boolean = false,
    val items: List<Cosmetic> = emptyList(),
    val error: String? = null
)

sealed class StoreEvent {
    object BuySuccess : StoreEvent()
}

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

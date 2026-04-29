package com.mm.astraisandroid.ui.features.store

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mm.astraisandroid.data.models.Cosmetic
import com.mm.astraisandroid.data.preferences.SessionManager
import com.mm.astraisandroid.data.repository.StoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StoreScreenState(
    val isLoading: Boolean = false,
    val items: List<Cosmetic> = emptyList(),
    val error: String? = null
)

sealed class StoreEvent {
    data class ShowToast(val message: String) : StoreEvent()
    object BuySuccess : StoreEvent()
}

@HiltViewModel
class StoreViewModel @Inject constructor(
    private val repository: StoreRepository
) : ViewModel() {

    private val _state = MutableStateFlow(StoreScreenState())
    val state: StateFlow<StoreScreenState> = _state.asStateFlow()

    private val _uiEvent = Channel<StoreEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    fun loadStore() {
        if (SessionManager.isGuest()) return
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val items = repository.getStoreItems()
                _state.update { it.copy(isLoading = false, items = items) }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message ?: "Error de conexión") }
            }
        }
    }

    fun buyItem(id: Int, onSuccess: () -> Unit) {
        if (SessionManager.isGuest()) return
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                repository.buyCosmetic(id)
                loadStore()
                onSuccess()
                _uiEvent.send(StoreEvent.BuySuccess)
                _uiEvent.send(StoreEvent.ShowToast("¡Compra realizada con éxito!"))
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false) }
                _uiEvent.send(StoreEvent.ShowToast(e.message ?: "Fondos insuficientes"))
            }
        }
    }

    fun equipItem(id: Int, onSuccess: () -> Unit) {
        if (SessionManager.isGuest()) return
        viewModelScope.launch {
            try {
                repository.equipCosmetic(id)
                loadStore()
                onSuccess()
                _uiEvent.send(StoreEvent.ShowToast("¡Equipado con éxito!"))
            } catch (e: Exception) {
                _uiEvent.send(StoreEvent.ShowToast(e.message ?: "Error al equipar"))
            }
        }
    }
}
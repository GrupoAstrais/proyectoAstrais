package com.mm.astraisandroid.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mm.astraisandroid.data.api.BackendRepository
import com.mm.astraisandroid.data.api.CosmeticResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class StoreUIState {
    object Loading : StoreUIState()
    object Success : StoreUIState()
    data class Error(val message: String) : StoreUIState()
}

class StoreViewModel : ViewModel() {
    private val _items = MutableStateFlow<List<CosmeticResponse>>(emptyList())
    val items: StateFlow<List<CosmeticResponse>> = _items

    private val _uiState = MutableStateFlow<StoreUIState>(StoreUIState.Loading)
    val uiState: StateFlow<StoreUIState> = _uiState

    fun loadStore() {
        viewModelScope.launch {
            _uiState.value = StoreUIState.Loading
            BackendRepository.getStoreItems()
                .onSuccess {
                    _items.value = it
                    _uiState.value = StoreUIState.Success
                }
                .onFailure {
                    _uiState.value = StoreUIState.Error(it.message ?: "Error de conexión")
                }
        }
    }

    fun buyItem(id: Int, onSuccess: () -> Unit) {
        viewModelScope.launch { BackendRepository.buyCosmetic(id).onSuccess { loadStore(); onSuccess() } }
    }

    fun equipItem(id: Int, onSuccess: () -> Unit) {
        viewModelScope.launch { BackendRepository.equipCosmetic(id).onSuccess { loadStore(); onSuccess() } }
    }
}
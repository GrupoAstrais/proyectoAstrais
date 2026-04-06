package com.mm.astraisandroid.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mm.astraisandroid.data.api.BackendRepository
import com.mm.astraisandroid.data.api.CosmeticResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class StoreUIState {
    object Loading : StoreUIState()
    object Success : StoreUIState()
    data class Error(val message: String) : StoreUIState()
}

@HiltViewModel
class StoreViewModel @Inject constructor(
    private val backendRepository: BackendRepository
) : ViewModel() {
    private val _items = MutableStateFlow<List<CosmeticResponse>>(emptyList())
    val items: StateFlow<List<CosmeticResponse>> = _items

    private val _uiState = MutableStateFlow<StoreUIState>(StoreUIState.Loading)
    val uiState: StateFlow<StoreUIState> = _uiState

    fun loadStore() {
        viewModelScope.launch {
            _uiState.value = StoreUIState.Loading
            try {
                val items = backendRepository.getStoreItems()
                _items.value = items
                _uiState.value = StoreUIState.Success
            } catch (e: Exception) {
                _uiState.value = StoreUIState.Error(e.message ?: "Error de conexión")
            }
        }
    }

    fun buyItem(id: Int, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try { backendRepository.buyCosmetic(id); loadStore(); onSuccess() } catch (e: Exception) {}
        }
    }

    fun equipItem(id: Int, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try { backendRepository.equipCosmetic(id); loadStore(); onSuccess() } catch (e: Exception) {}
        }
    }
}
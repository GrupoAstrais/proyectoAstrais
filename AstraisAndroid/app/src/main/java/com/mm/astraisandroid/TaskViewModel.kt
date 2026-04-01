package com.mm.astraisandroid

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mm.astraisandroid.api.BackendRepository
import com.mm.astraisandroid.api.CreateTareaRequest
import com.mm.astraisandroid.api.TareaResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class TaskUIState {
    object Idle    : TaskUIState()
    object Loading : TaskUIState()
    object Success : TaskUIState()
    data class Error(val message: String) : TaskUIState()
}

class TaskViewModel : ViewModel() {

    private val _tareas = MutableStateFlow<List<TareaResponse>>(emptyList())
    val tareas: StateFlow<List<TareaResponse>> = _tareas

    private val _uiState = MutableStateFlow<TaskUIState>(TaskUIState.Idle)
    val uiState: StateFlow<TaskUIState> = _uiState

    // Controla si el dialog de crear tarea está abierto
    var showCreateDialog by mutableStateOf(false)
        private set

    fun openCreateDialog()  { showCreateDialog = true }
    fun closeCreateDialog() { showCreateDialog = false }

    fun loadTareas(gid: Int) {
        viewModelScope.launch {
            _uiState.value = TaskUIState.Loading
            BackendRepository.getTareas(gid)
                .onSuccess { lista ->
                    _tareas.value = lista
                    _uiState.value = TaskUIState.Idle
                }
                .onFailure {
                    _uiState.value = TaskUIState.Error(it.message ?: "Error")
                }
        }
    }

    fun crearTarea(
        gid: Int,
        titulo: String,
        descripcion: String = "",
        tipo: String = "UNICO",
        prioridad: Int = 0,
        xp: Int = 0
    ) {
        viewModelScope.launch {
            _uiState.value = TaskUIState.Loading
            BackendRepository.createTarea(
                CreateTareaRequest(
                    gid = gid,
                    titulo = titulo,
                    descripcion = descripcion,
                    tipo = tipo,
                    prioridad = prioridad,
                )
            )
                .onSuccess {
                    _uiState.value = TaskUIState.Success
                    showCreateDialog = false
                    loadTareas(gid)
                }
                .onFailure {
                    _uiState.value = TaskUIState.Error(it.message ?: "Error")
                }
        }
    }

    fun completarTarea(tid: Int, gid: Int) {
        viewModelScope.launch {
            BackendRepository.completarTarea(tid)
                .onSuccess { loadTareas(gid) }
                .onFailure { _uiState.value = TaskUIState.Error(it.message ?: "Error") }
        }
    }

    fun resetState() { _uiState.value = TaskUIState.Idle }
}
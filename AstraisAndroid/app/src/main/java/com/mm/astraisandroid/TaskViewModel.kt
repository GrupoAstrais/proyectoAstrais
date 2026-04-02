package com.mm.astraisandroid

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mm.astraisandroid.api.BackendRepository
import com.mm.astraisandroid.api.CreateTareaRequest
import com.mm.astraisandroid.api.TareaResponse
import com.mm.astraisandroid.ui.tabs.Difficulty
import com.mm.astraisandroid.ui.tabs.TaskUIModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
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


    private val _isShowingCompleted = MutableStateFlow(false)
    val isShowingCompleted: StateFlow<Boolean> = _isShowingCompleted

    private val _selectedCategory = MutableStateFlow("ALL")
    val selectedCategory: StateFlow<String> = _selectedCategory

    fun openCreateDialog()  { showCreateDialog = true }
    fun closeCreateDialog() { showCreateDialog = false }

    val uiTasks: StateFlow<List<TaskUIModel>> = combine(
        _tareas,
        _isShowingCompleted,
        _selectedCategory
    ) { tareas, showCompleted, category ->
        val estadoBuscado = if (showCompleted) "COMPLETE" else "ACTIVE"

        tareas
            .filter { it.estado == estadoBuscado }
            .filter { category == "ALL" || it.tipo == category }
            .map { tarea ->
                TaskUIModel(
                    id = tarea.id,
                    title = tarea.titulo,
                    tipo = tarea.tipo,
                    difficulty = when (tarea.prioridad) {
                        0 -> Difficulty.EASY
                        1 -> Difficulty.MEDIUM
                        else -> Difficulty.HARD
                    },
                    xp = tarea.recompensaXp,
                    isCompleted = tarea.estado == "COMPLETE"
                )
            }
            .sortedByDescending { it.difficulty }

        // stateIn convierte el Flow normal en un StateFlow para Compose
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun toggleShowingCompleted(show: Boolean) {
        _isShowingCompleted.value = show
    }

    fun setCategory(category: String) {
        _selectedCategory.value = category
    }

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

    fun completarTarea(tid: Int, gid: Int, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            BackendRepository.completarTarea(tid)
                .onSuccess {
                    loadTareas(gid)
                    onSuccess()
                }
                .onFailure { _uiState.value = TaskUIState.Error(it.message ?: "Error") }
        }
    }

    fun resetState() { _uiState.value = TaskUIState.Idle }
}
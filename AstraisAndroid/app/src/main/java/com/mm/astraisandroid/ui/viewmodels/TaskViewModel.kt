package com.mm.astraisandroid.ui.viewmodels

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mm.astraisandroid.data.api.BackendRepository
import com.mm.astraisandroid.data.api.CreateTareaRequest
import com.mm.astraisandroid.data.local.dao.ActionDao
import com.mm.astraisandroid.data.local.dao.TareaDao
import com.mm.astraisandroid.data.local.entities.PendingAction
import com.mm.astraisandroid.data.local.entities.TareaEntity
import com.mm.astraisandroid.data.repository.TareaRepository
import com.mm.astraisandroid.sync.scheduleSync
import com.mm.astraisandroid.ui.tabs.Difficulty
import com.mm.astraisandroid.ui.tabs.TaskUIModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import javax.inject.Inject

sealed class TaskUIState {
    object Idle    : TaskUIState()
    object Loading : TaskUIState()
    object Success : TaskUIState()
    data class Error(val message: String) : TaskUIState()
}

@HiltViewModel
class TaskViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val backendRepository: BackendRepository,
    private val tareaDao: TareaDao,
    private val actionDao: ActionDao
) : ViewModel() {

    private val repository = TareaRepository(backendRepository, tareaDao)

    private val _uiState = MutableStateFlow<TaskUIState>(TaskUIState.Idle)
    val uiState: StateFlow<TaskUIState> = _uiState

    var showCreateDialog by mutableStateOf(false)
        private set

    private val _isShowingCompleted = MutableStateFlow(false)
    val isShowingCompleted: StateFlow<Boolean> = _isShowingCompleted

    private val _isOffline = MutableStateFlow(false)
    val isOffline: StateFlow<Boolean> = _isOffline

    private val _selectedCategory = MutableStateFlow("ALL")
    val selectedCategory: StateFlow<String> = _selectedCategory

    fun openCreateDialog()  { showCreateDialog = true }
    fun closeCreateDialog() { showCreateDialog = false }

    val uiTasks: StateFlow<List<TaskUIModel>> = combine(
        repository.allTareas,
        _isShowingCompleted,
        _selectedCategory
    ) { entidadesRoom, showCompleted, category ->
        val estadoBuscado = if (showCompleted) "COMPLETE" else "ACTIVE"

        entidadesRoom
            .filter { it.estado == estadoBuscado }
            .filter { category == "ALL" || it.tipo == category }
            .map { tarea ->
                TaskUIModel(
                    id = tarea.id,
                    title = tarea.titulo,
                    description = tarea.descripcion,
                    tipo = tarea.tipo,
                    difficulty = when (tarea.prioridad) {
                        0 -> Difficulty.EASY
                        1 -> Difficulty.MEDIUM
                        else -> Difficulty.HARD
                    },
                    xp = tarea.recompensaXp,
                    ludiones = tarea.recompensaLudion,
                    isCompleted = tarea.estado == "COMPLETE"
                )
            }
            .sortedByDescending { it.difficulty }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun toggleShowingCompleted(show: Boolean) { _isShowingCompleted.value = show }
    fun setCategory(category: String) { _selectedCategory.value = category }

    fun loadTareas(gid: Int) {
        viewModelScope.launch {
            _uiState.value = TaskUIState.Loading
            val result = repository.refreshTareas(gid)
            if (result.isSuccess) {
                _isOffline.value = false
            } else {
                _isOffline.value = true
            }
            _uiState.value = TaskUIState.Idle
        }
    }

    fun completarTarea(tid: Int, gid: Int, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            tareaDao.markAsCompleted(tid)
            try {
                backendRepository.completarTarea(tid)
                loadTareas(gid)
                onSuccess()
            } catch (e: Exception) {
                actionDao.addAction(PendingAction(type = "COMPLETE_TASK", data = "", targetId = tid))
                scheduleSync(context)
            }
        }
    }

    fun crearTarea(gid: Int, titulo: String, descripcion: String, tipo: String, prioridad: Int) {
        viewModelScope.launch {
            _uiState.value = TaskUIState.Loading
            val request = CreateTareaRequest(gid, titulo, descripcion, tipo, prioridad)
            try {
                backendRepository.createTarea(request)
                _uiState.value = TaskUIState.Success
                showCreateDialog = false
                loadTareas(gid)
            } catch (e: Exception) {
                val jsonRequest = Json.encodeToString(request)
                actionDao.addAction(PendingAction(type = "CREATE_TASK", data = jsonRequest))

                val tempId = -(System.currentTimeMillis() % 100000).toInt()
                val fakeTarea = TareaEntity(
                    id = tempId,
                    titulo = titulo,
                    descripcion = descripcion,
                    tipo = tipo,
                    estado = "ACTIVE",
                    prioridad = prioridad,
                    recompensaXp = 0,
                    recompensaLudion = 0,
                    isPendingSync = true
                )
                tareaDao.insertTareas(listOf(fakeTarea))
                scheduleSync(context)

                _uiState.value = TaskUIState.Success
                showCreateDialog = false
            }
        }
    }

    fun syncOfflineActions(gid: Int) {
        viewModelScope.launch {
            val pending = actionDao.getAllPending()
            if (pending.isEmpty()) {
                loadTareas(gid)
                return@launch
            }

            for (action in pending) {
                val isSuccess = try {
                    when (action.type) {
                        "COMPLETE_TASK" -> { backendRepository.completarTarea(action.targetId!!); true }
                        "CREATE_TASK" -> {
                            val request = Json.decodeFromString<CreateTareaRequest>(action.data)
                            backendRepository.createTarea(request)
                            true
                        }
                        else -> false
                    }
                } catch (e: Exception) { false }

                if (isSuccess) actionDao.removeAction(action)
            }
            loadTareas(gid)
        }
    }
}
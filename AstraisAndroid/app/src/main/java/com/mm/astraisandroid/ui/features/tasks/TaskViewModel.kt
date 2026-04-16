package com.mm.astraisandroid.ui.features.tasks

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mm.astraisandroid.data.api.CreateTareaHabitData
import com.mm.astraisandroid.data.api.CreateTareaRequest
import com.mm.astraisandroid.data.api.CreateTareaUniqueData
import com.mm.astraisandroid.data.api.HabitFrequency
import com.mm.astraisandroid.data.local.dao.ActionDao
import com.mm.astraisandroid.data.local.dao.TareaDao
import com.mm.astraisandroid.data.local.entities.PendingAction
import com.mm.astraisandroid.data.local.entities.TareaEntity
import com.mm.astraisandroid.data.models.TaskPriority
import com.mm.astraisandroid.data.models.TaskState
import com.mm.astraisandroid.data.models.TaskType
import com.mm.astraisandroid.data.repository.TaskRepository
import com.mm.astraisandroid.sync.scheduleSync
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import javax.inject.Inject

enum class TaskCategory { ALL, UNICO, HABITO, OBJETIVO }

sealed class TaskUIState {
    object Idle    : TaskUIState()
    object Loading : TaskUIState()
    object Success : TaskUIState()
    data class Error(val message: String) : TaskUIState()
}

data class TaskUIModel(
    val id: Int,
    val title: String,
    val description: String,
    val priority: TaskPriority,
    val xp: Int,
    val ludiones: Int,
    val tipo: String,
    var isCompleted: Boolean = false,
    val parentId: Int? = null
)

data class TaskScreenState(
    val isLoading: Boolean = false,
    val isOffline: Boolean = false,
    val isShowingCompleted: Boolean = false,
    val selectedCategory: String = "ALL",
    val showCreateDialog: Boolean = false,
    val parentIdForNewTask: Int? = null,
    val tasks: List<TaskUIModel> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class TaskViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: TaskRepository,
    private val actionDao: ActionDao,
    private val tareaDao: TareaDao
) : ViewModel() {

    private val _state = MutableStateFlow(TaskScreenState())

    val state: StateFlow<TaskScreenState> = combine(
        repository.allTareas,
        _state
    ) { tareasRoom, currentState ->
        val estadoBuscado = if (currentState.isShowingCompleted) TaskState.COMPLETE else TaskState.ACTIVE

        val uiTasks = tareasRoom
            .filter { it.state == estadoBuscado }
            .filter { currentState.selectedCategory == "ALL" || it.type.name == currentState.selectedCategory }
            .map { tarea ->
                TaskUIModel(
                    id = tarea.id,
                    title = tarea.title,
                    description = tarea.description,
                    tipo = tarea.type.name,
                    priority = tarea.taskPriority,
                    xp = tarea.xpReward,
                    ludiones = tarea.ludionReward,
                    isCompleted = tarea.state == TaskState.COMPLETE,
                    parentId = tarea.parentId
                )
            }
            .sortedByDescending { it.priority.ordinal }

        currentState.copy(tasks = uiTasks)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TaskScreenState())

    fun openCreateDialog(parentId: Int? = null) {
        _state.update { it.copy(showCreateDialog = true, parentIdForNewTask = parentId) }
    }

    fun closeCreateDialog() {
        _state.update { it.copy(showCreateDialog = false, parentIdForNewTask = null) }
    }

    fun toggleShowingCompleted(show: Boolean) {
        _state.update { it.copy(isShowingCompleted = show) }
    }
    fun setCategory(category: String) {
        _state.update { it.copy(selectedCategory = category) }
    }

    fun loadTareas(gid: Int) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val result = repository.refreshTareas(gid)
            _state.update { it.copy(isLoading = false, isOffline = result.isFailure) }
        }
    }

    fun completarTarea(tid: Int, gid: Int, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            tareaDao.markAsCompleted(tid)
            try {
                repository.completarTarea(tid)
                loadTareas(gid)
                onSuccess()
            } catch (e: Exception) {
                actionDao.addAction(PendingAction(type = "COMPLETE_TASK", data = "", targetId = tid))
                scheduleSync(context)
            }
        }
    }

    fun crearTarea(
        gid: Int,
        titulo: String,
        descripcion: String,
        tipo: TaskType,
        prioridad: TaskPriority,
        fechaLimite: String? = null
    ) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            try {
                repository.createNewTask(
                    gid = gid,
                    title = titulo,
                    description = descripcion,
                    type = tipo,
                    priority = prioridad,
                    dueDate = fechaLimite,
                    parentId = _state.value.parentIdForNewTask
                )

                _state.update { it.copy(showCreateDialog = false, isLoading = false) }
                loadTareas(gid)

            } catch (e: Exception) {
                val tempId = -(System.currentTimeMillis() % 100000).toInt()

                actionDao.addAction(PendingAction(
                    type = "CREATE_TASK",
                    targetId = tempId,
                    data = "{\"titulo\":\"$titulo\", \"tipo\":\"${tipo.name}\"}"
                ))

                tareaDao.insertTareas(listOf(
                    TareaEntity(
                        id = tempId,
                        titulo = titulo,
                        descripcion = descripcion,
                        tipo = tipo.name,
                        estado = "ACTIVE",
                        prioridad = prioridad.ordinal,
                        recompensaXp = 0,
                        recompensaLudion = 0,
                        isPendingSync = true
                    )
                ))

                scheduleSync(context)
                _state.update { it.copy(showCreateDialog = false, isLoading = false) }
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
                        "COMPLETE_TASK" -> {
                            repository.completarTarea(action.targetId!!)
                            true
                        }
                        "CREATE_TASK" -> {
                            val request = Json.decodeFromString<CreateTareaRequest>(action.data)
                            repository.createTareaDirect(request)
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
package com.mm.astraisandroid.ui.features.tasks

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mm.astraisandroid.data.api.CreateTareaHabitData
import com.mm.astraisandroid.data.api.CreateTareaRequest
import com.mm.astraisandroid.data.api.CreateTareaUniqueData
import com.mm.astraisandroid.data.api.EditTareaRequest
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
import android.util.Log

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
    val parentId: Int? = null,
    val habitFrequency: String? = null
)

data class TaskScreenState(
    val isLoading: Boolean = false,
    val isOffline: Boolean = false,
    val isShowingCompleted: Boolean = false,
    val selectedCategory: String = "ALL",
    val showCreateDialog: Boolean = false,
    val parentIdForNewTask: Int? = null,
    val tasks: List<TaskUIModel> = emptyList(),
    val allTasksCache: List<TaskUIModel> = emptyList(),
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

        val allUiTasks = tareasRoom.map { tarea ->
            TaskUIModel(
                id = tarea.id,
                title = tarea.title,
                description = tarea.description,
                tipo = tarea.type.name,
                priority = tarea.taskPriority,
                xp = tarea.xpReward,
                ludiones = tarea.ludionReward,
                isCompleted = tarea.state == TaskState.COMPLETE,
                parentId = tarea.parentId,
                habitFrequency = tarea.habitFrequency
            )
        }

        val filteredTasks = allUiTasks
            .filter {
                if (it.parentId == null) {
                    it.isCompleted == currentState.isShowingCompleted
                } else {
                    true
                }
            }
            .filter {
                currentState.selectedCategory == "ALL" ||
                        it.tipo == currentState.selectedCategory ||
                        it.parentId != null
            }
            .sortedByDescending { it.priority.ordinal }

        currentState.copy(
            tasks = filteredTasks,
            allTasksCache = allUiTasks
        )
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
            val tareaActual = state.value.allTasksCache.find { it.id == tid } ?: return@launch
            val parentId = tareaActual.parentId

            tareaDao.markAsCompleted(tid)

            var debeCompletarPadre = false

            if (parentId != null) {
                val subtareasPendientes = state.value.allTasksCache.filter {
                    it.parentId == parentId && it.id != tid && !it.isCompleted
                }

                if (subtareasPendientes.isEmpty()) {
                    debeCompletarPadre = true
                    tareaDao.markAsCompleted(parentId)
                }
            }

            onSuccess()

            if (com.mm.astraisandroid.data.preferences.SessionManager.isGuest()) return@launch

            try {
                repository.completarTarea(tid)
                Log.d("AstraisTasks", "ÉXITO en red: Tarea $tid completada en el servidor.")
                if (debeCompletarPadre) {
                    repository.completarTarea(parentId!!)
                }
            } catch (e: Exception) {
                Log.e("AstraisTasks", "FALLO en red (Modo Offline) al completar:", e)
                actionDao.addAction(PendingAction(type = "COMPLETE_TASK", data = "", targetId = tid))
                if (debeCompletarPadre) {
                    actionDao.addAction(PendingAction(type = "COMPLETE_TASK", data = "", targetId = parentId!!))
                }
                scheduleSync(context)
            }
        }
    }

    fun toggleTaskCompletion(tid: Int, gid: Int, isCurrentlyCompleted: Boolean, onSuccess: () -> Unit = {}) {
        if (isCurrentlyCompleted) {
            viewModelScope.launch {
                Log.d("AstraisTasks", "Intentando DESHACER tarea con ID: $tid")
                val tareaActual = state.value.allTasksCache.find { it.id == tid } ?: return@launch
                val parentId = tareaActual.parentId

                tareaDao.markAsActive(tid)

                var debeDeshacerPadre = false
                if (parentId != null) {
                    val padre = state.value.allTasksCache.find { it.id == parentId }
                    if (padre?.isCompleted == true) {
                        debeDeshacerPadre = true
                        tareaDao.markAsActive(parentId)
                    }
                }

                onSuccess()

                if (com.mm.astraisandroid.data.preferences.SessionManager.isGuest()) return@launch

                try {
                    repository.uncompleteTarea(tid)
                    Log.d("AstraisTasks", "ÉXITO en red: Tarea $tid deshecha en el servidor.")
                    if (debeDeshacerPadre) {
                        repository.uncompleteTarea(parentId!!)
                    }
                } catch (e: Exception) {
                    Log.e("AstraisTasks", "FALLO en red (Modo Offline): Guardando UNCOMPLETE_TASK para la tarea $tid en PendingActions.")
                    actionDao.addAction(PendingAction(type = "UNCOMPLETE_TASK", data = "", targetId = tid))
                    if (debeDeshacerPadre) {
                        actionDao.addAction(PendingAction(type = "UNCOMPLETE_TASK", data = "", targetId = parentId!!))
                    }
                    scheduleSync(context)
                }
            }
        } else {
            Log.d("AstraisTasks", "Derivando a completarTarea() para el ID: $tid")
            completarTarea(tid, gid, onSuccess)
        }
    }

    fun crearTarea(
        gid: Int,
        titulo: String,
        descripcion: String,
        tipo: TaskType,
        prioridad: TaskPriority,
        fechaLimite: String? = null,
        frecuencia: String? = null
    ) {
        val currentParentId = state.value.parentIdForNewTask

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            val habitFrequencyEnum = try {
                frecuencia?.let { HabitFrequency.valueOf(it) }
            } catch (e: Exception) { null }

            val extraUnico = if (tipo == TaskType.UNICO) {
                val safeDate = if (!fechaLimite.isNullOrBlank()) "${fechaLimite}T23:59:59Z" else "2026-12-31T23:59:59Z"
                CreateTareaUniqueData(fechaLimite = safeDate)
            } else null

            val extraHabito = if (tipo == TaskType.HABITO && habitFrequencyEnum != null) {
                CreateTareaHabitData(numeroFrecuencia = 1, frequency = habitFrequencyEnum)
            } else null

            val request = CreateTareaRequest(
                gid = gid,
                titulo = titulo,
                descripcion = descripcion,
                tipo = tipo.name,
                prioridad = prioridad.ordinal,
                extraUnico = extraUnico,
                extraHabito = extraHabito,
                idObjetivo = currentParentId
            )

            if (com.mm.astraisandroid.data.preferences.SessionManager.isGuest()) {
                val tempId = -(System.currentTimeMillis() % 100000).toInt()
                tareaDao.insertTareas(listOf(
                    TareaEntity(
                        id = tempId, titulo = titulo, descripcion = descripcion, tipo = tipo.name,
                        estado = "ACTIVE", prioridad = prioridad.ordinal, recompensaXp = 0, recompensaLudion = 0, isPendingSync = false,
                        idObjetivo = currentParentId
                    )
                ))
                _state.update { it.copy(showCreateDialog = false, isLoading = false) }
                return@launch
            }

            try {
                repository.createTareaDirect(request)

                _state.update { it.copy(showCreateDialog = false, isLoading = false) }
                loadTareas(gid)

            } catch (e: Exception) {
                Log.e("AstraisTasks", "FALLO en red (Modo Offline) al crear:", e)
                val tempId = -(System.currentTimeMillis() % 100000).toInt()

                val dataString = Json.encodeToString(CreateTareaRequest.serializer(), request)

                actionDao.addAction(PendingAction(
                    type = "CREATE_TASK", targetId = tempId,
                    data = dataString
                ))

                tareaDao.insertTareas(listOf(
                    TareaEntity(
                        id = tempId, titulo = titulo, descripcion = descripcion, tipo = tipo.name,
                        estado = "ACTIVE", prioridad = prioridad.ordinal, recompensaXp = 0, recompensaLudion = 0, isPendingSync = true,
                        idObjetivo = currentParentId
                    )
                ))
                scheduleSync(context)
                _state.update { it.copy(showCreateDialog = false, isLoading = false) }
            }
        }
    }

    fun eliminarTarea(tid: Int, gid: Int) {
        viewModelScope.launch {
            tareaDao.deleteTareaById(tid)

            if (com.mm.astraisandroid.data.preferences.SessionManager.isGuest()) return@launch

            try {
                repository.eliminarTarea(tid)
            } catch (e: Exception) {
                actionDao.addAction(PendingAction(
                    type = "DELETE_TASK",
                    data = "",
                    targetId = tid
                ))
                scheduleSync(context)
            }
        }
    }

    fun editarTarea(
        tid: Int,
        gid: Int,
        titulo: String,
        descripcion: String,
        prioridad: TaskPriority,
        fechaLimite: String? = null,
        frecuencia: String? = null
    ) {
        viewModelScope.launch {
            tareaDao.updateTareaDetails(titulo, descripcion, prioridad.ordinal, tid)

            val habitFrequencyEnum = try {
                frecuencia?.let { HabitFrequency.valueOf(it) }
            } catch (e: Exception) { null }

            val request = EditTareaRequest(
                titulo = titulo,
                descripcion = descripcion,
                prioridad = prioridad.ordinal,
                extraUnico = fechaLimite?.let { CreateTareaUniqueData(it) },
                extraHabito = habitFrequencyEnum?.let { CreateTareaHabitData(1, it) }
            )

            if (com.mm.astraisandroid.data.preferences.SessionManager.isGuest()) return@launch

            try {
                repository.editarTarea(tid, titulo, descripcion, prioridad, fechaLimite, habitFrequencyEnum)
            } catch (e: Exception) {
                Log.e("AstraisTasks", "FALLO en red (Modo Offline) al editar:", e)
                val dataString = Json.encodeToString(EditTareaRequest.serializer(), request)
                actionDao.addAction(PendingAction(
                    type = "EDIT_TASK",
                    data = dataString,
                    targetId = tid
                ))
                scheduleSync(context)
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

            Log.d("AstraisTasks", "Sincronización: Se han encontrado ${pending.size} acciones pendientes. Intentando procesar...")

            for (action in pending) {
                try {
                    Log.d("AstraisTasks", "Sincronizando: ${action.type} [ID: ${action.targetId}]")
                    when (action.type) {
                        "COMPLETE_TASK" -> {
                            repository.completarTarea(action.targetId!!)
                        }
                        "CREATE_TASK" -> {
                            val request = Json.decodeFromString<CreateTareaRequest>(action.data)
                            val newId = repository.createTareaDirect(request)

                            actionDao.updateTargetIds(oldId = action.targetId!!, newId = newId)
                            tareaDao.updateTareaId(oldId = action.targetId!!, newId = newId)
                            tareaDao.updateParentId(oldId = action.targetId!!, newId = newId)
                        }
                        "DELETE_TASK" -> {
                            repository.eliminarTarea(action.targetId!!)
                        }
                        "EDIT_TASK" -> {
                            val request = Json.decodeFromString<EditTareaRequest>(action.data)
                            val prio = request.prioridad?.let { TaskPriority.entries.getOrNull(it) } ?: TaskPriority.LOW
                            repository.editarTarea(action.targetId!!, request.titulo, request.descripcion, prio)
                        }
                        "UNCOMPLETE_TASK" -> {
                            repository.uncompleteTarea(action.targetId!!)
                        }
                    }
                } catch (e: Exception) {
                    Log.e("AstraisTasks", "Fallo letal en sincronización de la acción ${action.type}: ${e.message}", e)
                }

                actionDao.removeAction(action)
            }
            loadTareas(gid)
        }
    }

}
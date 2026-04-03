package com.mm.astraisandroid.ui.viewmodels

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mm.astraisandroid.data.api.BackendRepository
import com.mm.astraisandroid.data.api.CreateTareaRequest
import com.mm.astraisandroid.data.local.AstraisDb
import com.mm.astraisandroid.data.local.entities.PendingAction
import com.mm.astraisandroid.data.repository.TareaRepository
import com.mm.astraisandroid.ui.tabs.Difficulty
import com.mm.astraisandroid.ui.tabs.TaskUIModel
import com.mm.astraisandroid.sync.scheduleSync
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

sealed class TaskUIState {
    object Idle    : TaskUIState()
    object Loading : TaskUIState()
    object Success : TaskUIState()
    data class Error(val message: String) : TaskUIState()
}

class TaskViewModel(application: Application) : AndroidViewModel(application) {
    private val context = application.applicationContext
    private val db = AstraisDb.getInstance(application)

    private val repository = TareaRepository(BackendRepository, db.tareaDao())

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

    // Escuchamos a la base de datos.
    // Si Room cambia la UI se actualiza.
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
                _uiState.value = TaskUIState.Idle
            } else {
                _isOffline.value = true
                // He cambiado esto para que no mande Error, sino Idle porque Room ya tiene los datos cacheados
                _uiState.value = TaskUIState.Idle
            }
        }
    }

    fun completarTarea(tid: Int, gid: Int, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            db.tareaDao().markAsCompleted(tid)

            val result = BackendRepository.completarTarea(tid)

            if (result.isFailure) {
                // Cola de offline
                db.actionDao().addAction(PendingAction(type = "COMPLETE_TASK", data = "", targetId = tid))
                scheduleSync(context)
            } else {
                loadTareas(gid)
                onSuccess()
            }
        }
    }

    fun crearTarea(gid: Int, titulo: String, descripcion: String, tipo: String, prioridad: Int) {
        viewModelScope.launch {
            _uiState.value = TaskUIState.Loading
            val request = CreateTareaRequest(gid, titulo, descripcion, tipo, prioridad)
            val result = BackendRepository.createTarea(request)

            if (result.isSuccess) {
                _uiState.value = TaskUIState.Success
                showCreateDialog = false
                loadTareas(gid)
            } else {
                val jsonRequest = Json.encodeToString(request)
                db.actionDao().addAction(
                    PendingAction(type = "CREATE_TASK", data = jsonRequest)
                )

                val tempId = -(System.currentTimeMillis() % 100000).toInt()
                val fakeTarea = com.mm.astraisandroid.data.local.entities.TareaEntity(
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
                db.tareaDao().insertTareas(listOf(fakeTarea))

                scheduleSync(context)

                _uiState.value = TaskUIState.Success
                showCreateDialog = false
            }
        }
    }

    fun resetState() { _uiState.value = TaskUIState.Idle }

    /**
     * Vacia la cola de acciones pendientes y cuando termina, pide la lista al servidor.
     * Esto evita que el servidor machaque nuestros cambios locales antes de recibirlos.
     * Cuanta potencia tiene Git
     */
    fun syncOfflineActions(gid: Int) {
        viewModelScope.launch {
            val pending = db.actionDao().getAllPending()

            // Si no hay nada pendiente, simplemente cargamos las tareas normales
            if (pending.isEmpty()) {
                loadTareas(gid)
                return@launch
            }

            // Si hay tareas pendientes las enviamos a Ktor una por una
            for (action in pending) {
                val isSuccess = try {
                    when (action.type) {
                        "COMPLETE_TASK" -> {
                            BackendRepository.completarTarea(action.targetId!!).isSuccess
                        }
                        "CREATE_TASK" -> {
                            val request = Json.decodeFromString<CreateTareaRequest>(action.data)
                            BackendRepository.createTarea(request).isSuccess
                        }
                        else -> false
                    }
                } catch (e: Exception) {
                    false
                }

                // Si el servidor confirma que lo guardó, lo borramos de la cola
                if (isSuccess) {
                    db.actionDao().removeAction(action)
                }
            }

            loadTareas(gid)
        }
    }
}
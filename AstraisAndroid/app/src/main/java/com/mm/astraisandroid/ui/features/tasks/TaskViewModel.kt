package com.mm.astraisandroid.ui.features.tasks

import android.content.Context
import android.util.Log
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
import com.mm.astraisandroid.data.preferences.SessionManager
import com.mm.astraisandroid.data.repository.TaskRepository
import com.mm.astraisandroid.sync.scheduleSync
import com.mm.astraisandroid.ui.components.SnackbarManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

/**
 * Categorías de filtrado disponibles en la pantalla de lista de tareas.
 *
 * @property ALL Muestra todos los tipos de tarea sin filtrar.
 * @property UNICO Muestra únicamente las tareas de tipo único (con fecha límite).
 * @property HABITO Muestra únicamente los hábitos.
 * @property OBJETIVO Muestra únicamente los objetivos (con subtareas anidadas).
 */
enum class TaskCategory { ALL, UNICO, HABITO, OBJETIVO }

/**
 * Estado genérico para operaciones puntuales del ViewModel de tareas.
 * Se usa para operaciones que no requieren reflejar su resultado en [TaskScreenState].
 */
sealed class TaskUIState {
    /**
     * Estado inicial o neutro. No hay operación en curso.
     */
    object Idle : TaskUIState()
    /**
     * Hay una operación de red o de base de datos en curso.
     */
    object Loading : TaskUIState()
    /**
     * La última operación finalizó con éxito.
     */
    object Success : TaskUIState()
    /**
     * La última operación finalizó con error.
     * @property message Mensaje descriptivo del error.
     */
    data class Error(val message: String) : TaskUIState()
}

/**
 * Modelo de UI para una tarea individual, listo para ser consumido directamente por
 * los composables de la pantalla de tareas sin lógica de transformación adicional.
 *
 * @property id Identificador de la tarea. Los IDs negativos son temporales (modo invitado/offline).
 * @property title Título de la tarea.
 * @property description Descripción de la tarea.
 * @property priority Nivel de prioridad de la tarea.
 * @property xp Puntos de experiencia que otorga al completarse.
 * @property ludiones Ludiones (moneda del juego) que otorga al completarse.
 * @property tipo Tipo de tarea: `"UNICO"`, `"HABITO"` u `"OBJETIVO"`.
 * @property isCompleted `true` si la tarea está completada. Mutable para actualizaciones optimistas.
 * @property parentId ID de la tarea objetivo padre si es subtarea, o `null`.
 * @property habitFrequency Frecuencia del hábito como cadena, o `null` si no aplica.
 * @property dueDate Fecha límite para tareas únicas como cadena ISO, o `null` si no aplica.
 */
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
    val habitFrequency: String? = null,
    val dueDate: String? = null
)

/**
 * Estado de la pantalla de lista de tareas.
 *
 * @property isLoading `true` mientras hay una sincronización de red en curso.
 * @property isOffline `true` si el último intento de sincronización falló; la UI
 *   muestra datos del caché local.
 * @property isShowingCompleted `true` si la lista muestra tareas completadas en lugar
 *   de tareas activas.
 * @property selectedCategory Filtro de tipo de tarea activo (nombre del enum [TaskCategory]).
 * @property showCreateDialog `true` cuando el diálogo de creación rápida debe mostrarse.
 * @property parentIdForNewTask ID del objetivo padre para la nueva tarea (subtarea), o `null`
 *   si la tarea será de nivel superior.
 * @property tasks Lista de [TaskUIModel] filtrada y ordenada según la categoría y el estado
 *   de completado activos. Es la lista que renderiza la UI.
 * @property allTasksCache Lista completa de tareas sin filtrar, usada para calcular el estado
 *   del objetivo padre al completar/deshacer una subtarea.
 * @property error Mensaje de error de la última operación fallida, o `null`.
 */
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

/**
 * ViewModel de la pantalla de lista de tareas de Astrais.
 *
 * Gestiona el ciclo de vida de los datos de tareas para la UI, actúndo como puente
 * entre la capa de datos ([TaskRepository]) y los composables. Expone un [StateFlow]
 * inmutable ([state]) con el estado actual de la pantalla, combinando el [Flow] de
 * Room con el estado interno para que los cambios locales sean inmediatos.
 *
 * Funcionalidades clave:
 * - **Offline-first**: completa, deshace, crea, edita y elimina tareas en Room de
 *   inmediato y encola [PendingAction] para sincronizar con el servidor en segundo plano.
 * - **Modo invitado**: detecta si el usuario es invitado ([SessionManager.isGuest]) y
 *   nunca llama a la red; solo persiste acciones en cola para ser migradas al registrarse.
 * - **Sincronización de cola**: [syncOfflineActionsAwait] procesa la cola de acciones
 *   pendientes en orden, resolviendo la traducción de IDs temporales a IDs reales del servidor.
 * - **Cascada de objetivos**: al completar/deshacer una subtarea, comprueba automáticamente
 *   si el objetivo padre debe ser completado o reactivado.
 *
 * @property context Contexto de la aplicación, necesario para programar [SyncWorker].
 * @property repository Repositorio de tareas que actúa como fuente de verdad.
 * @property actionDao DAO de Room para encolar acciones pendientes de forma directa.
 * @property tareaDao DAO de Room para modificaciones optimistas de registros locales.
 * @property snackbarManager Gestor de snackbars para mostrar notificaciones transitorias al usuario.
 */
@HiltViewModel
class TaskViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: TaskRepository,
    private val actionDao: ActionDao,
    private val tareaDao: TareaDao,
    private val snackbarManager: SnackbarManager
) : ViewModel() {

    /**
     * Estado mutable interno; solo modificable desde este ViewModel.
     */
    private val _state = MutableStateFlow(TaskScreenState())

    /**
     * Estado público expuesto a la UI como flujo de solo lectura.
     * Combina el [Flow] reactivo de Room con [_state] para producir en cada emisión
     * una lista filtrada por [TaskScreenState.isShowingCompleted] y
     * [TaskScreenState.selectedCategory], ordenada por prioridad descendente.
     */
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
                habitFrequency = tarea.habitFrequency,
                dueDate = tarea.dueDate
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

    /**
     * Muestra el diálogo de creación rápida de tarea.
     *
     * @param parentId ID del objetivo padre si la nueva tarea será una subtarea, o `null`.
     */
    fun openCreateDialog(parentId: Int? = null) {
        _state.update { it.copy(showCreateDialog = true, parentIdForNewTask = parentId) }
    }

    /**
     * Cierra el diálogo de creación rápida y limpia el ID de objetivo padre almacenado.
     */
    fun closeCreateDialog() {
        _state.update { it.copy(showCreateDialog = false, parentIdForNewTask = null) }
    }

    /**
     * Cambia la vista entre tareas activas y tareas completadas.
     *
     * @param show `true` para mostrar tareas completadas; `false` para mostrar activas.
     */
    fun toggleShowingCompleted(show: Boolean) {
        _state.update { it.copy(isShowingCompleted = show) }
    }

    /**
     * Aplica un filtro de categoría a la lista de tareas.
     *
     * @param category Nombre del enum [TaskCategory] a activar (p. ej. `"HABITO"`, `"ALL"`).
     */
    fun setCategory(category: String) {
        _state.update { it.copy(selectedCategory = category) }
    }

    /**
     * Solicita una sincronización de tareas del grupo con el servidor.
     * Marca [TaskScreenState.isLoading] durante la operación.
     * Si falla, activa [TaskScreenState.isOffline] y los datos del caché local
     * siguen siendo visibles.
     * Los usuarios invitados son bloqueados antes de realizar la llamada de red.
     *
     * @param gid Identificador del grupo cuyas tareas se quieren cargar.
     */
    fun loadTareas(gid: Int) {
        viewModelScope.launch {
            // STRICT GUEST GUARD: never refresh from backend in guest mode
            if (SessionManager.isGuest()) {
                _state.update { it.copy(isLoading = false, isOffline = false) }
                return@launch
            }
            _state.update { it.copy(isLoading = true) }
            val result = repository.refreshTareas(gid)
            _state.update { it.copy(isLoading = false, isOffline = result.isFailure) }
        }
    }

    /**
     * Completa una tarea de forma optimista: actualiza Room de inmediato y muestra
     * un snackbar con las recompensas obtenidas. Luego intenta sincronizar con el
     * servidor; si falla, encola una [PendingAction] y programa [SyncWorker].
     * En modo invitado, solo encola la acción sin llamar a la red.
     * Si la tarea es subtarea y era la última pendiente, completa el objetivo padre
     * también automáticamente.
     *
     * @param tid Identificador de la tarea a completar.
     * @param gid Identificador del grupo (para refrescar si es necesario).
     * @param onSuccess Callback invocado tras la actualización optimista local, antes de
     *   la llamada de red.
     */
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
            snackbarManager.showMessage("¡Tarea completada! + ${tareaActual.xp} XP /  ${tareaActual.ludiones} L")
            if (SessionManager.isGuest()) {
                actionDao.addAction(PendingAction(type = "COMPLETE_TASK", data = "", targetId = tid))
                if (debeCompletarPadre) {
                    actionDao.addAction(PendingAction(type = "COMPLETE_TASK", data = "", targetId = parentId!!))
                }
                return@launch
            }

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

    /**
     * Alterna el estado de completado de una tarea.
     * Si la tarea está completada, la revierte a activa (deshace); si está activa,
     * la completa. Delega en [completarTarea] o en la lógica de descomplete según
     * corresponda. En modo invitado, solo encola la acción pendiente.
     *
     * @param tid Identificador de la tarea.
     * @param gid Identificador del grupo (para refrescar si es necesario).
     * @param isCurrentlyCompleted `true` si la tarea está actualmente completada.
     * @param onSuccess Callback invocado tras la actualización optimista local.
     */
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

                if (SessionManager.isGuest()) {
                    actionDao.addAction(PendingAction(type = "UNCOMPLETE_TASK", data = "", targetId = tid))
                    if (debeDeshacerPadre) {
                        actionDao.addAction(PendingAction(type = "UNCOMPLETE_TASK", data = "", targetId = parentId!!))
                    }
                    return@launch
                }

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

    /**
     * Crea una nueva tarea aplicando la estrategia correcta según el contexto:
     * - **Modo invitado**: inserta un [TareaEntity] con ID temporal negativo y encola
     *   una [PendingAction] de tipo `CREATE_TASK` para promoción posterior.
     * - **En línea con red disponible**: llama directamente al servidor y refresca.
     * - **En línea sin red**: inserta el registro temporal y encola la acción;
     *   programa [SyncWorker] para el reintento.
     * Cierra el diálogo de creación al terminar.
     *
     * @param gid Identificador del grupo en el que se crea la tarea.
     * @param titulo Título de la nueva tarea.
     * @param descripcion Descripción de la nueva tarea.
     * @param tipo Tipo de tarea ([TaskType]).
     * @param prioridad Prioridad de la tarea.
     * @param fechaLimite Fecha límite `YYYY-MM-DD` para tareas únicas, o `null`.
     * @param frecuencia Nombre del enum [HabitFrequency] para hábitos, o `null`.
     */
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

            if (SessionManager.isGuest()) {
                val tempId = -(System.currentTimeMillis() % 100000).toInt()
                val guestRequest = request.copy(gid = SessionManager.GUEST_GID)
                val entity = TareaEntity(
                    id = tempId,
                    titulo = titulo,
                    descripcion = descripcion,
                    tipo = tipo.name,
                    estado = "ACTIVE",
                    prioridad = prioridad.ordinal,
                    recompensaXp = 0,
                    recompensaLudion = 0,
                    isPendingSync = true,
                    idObjetivo = currentParentId,
                    extraUnicoFecha = extraUnico?.fechaLimite,
                    extraHabitoFrecuencia = habitFrequencyEnum?.name
                )
                val action = PendingAction(
                    type = "CREATE_TASK",
                    targetId = tempId,
                    data = Json.encodeToString(CreateTareaRequest.serializer(), guestRequest)
                )
                tareaDao.createTaskWithAction(entity, action, actionDao)
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

    /**
     * Elimina una tarea de forma optimista: borra el registro de Room de inmediato,
     * luego intenta eliminarla en el servidor. Si falla, encola una [PendingAction]
     * de tipo `DELETE_TASK` y programa [SyncWorker].
     * En modo invitado, solo encola la acción si el ID es positivo (tarea del servidor);
     * los IDs negativos (locales) no necesitan acción de red.
     *
     * @param tid Identificador de la tarea a eliminar.
     * @param gid Identificador del grupo (reservado para futuras validaciones).
     */
    fun eliminarTarea(tid: Int, gid: Int) {
        viewModelScope.launch {
            tareaDao.deleteTareaById(tid)

            if (SessionManager.isGuest()) {
                if (tid >= 0) {
                    actionDao.addAction(PendingAction(type = "DELETE_TASK", data = "", targetId = tid))
                }
                return@launch
            }

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

    /**
     * Edita una tarea de forma optimista: actualiza Room de inmediato y luego
     * sincroniza con el servidor. Si falla, encola una [PendingAction] de tipo
     * `EDIT_TASK` con el JSON del [EditTareaRequest] y programa [SyncWorker].
     * En modo invitado, solo encola la acción sin llamar a la red.
     *
     * @param tid Identificador de la tarea a editar.
     * @param gid Identificador del grupo (reservado para futuras validaciones).
     * @param titulo Nuevo título de la tarea.
     * @param descripcion Nueva descripción de la tarea.
     * @param prioridad Nueva prioridad de la tarea.
     * @param fechaLimite Nueva fecha límite para tareas únicas, o `null`.
     * @param frecuencia Nombre del enum [HabitFrequency] para hábitos, o `null`.
     */
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

            if (SessionManager.isGuest()) {
                val dataString = Json.encodeToString(EditTareaRequest.serializer(), request)
                actionDao.addAction(PendingAction(
                    type = "EDIT_TASK",
                    data = dataString,
                    targetId = tid
                ))
                return@launch
            }

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

    /**
     * Variante fire-and-forget de [syncOfflineActionsAwait]. Inicia la sincronización
     * de la cola de acciones pendientes sin bloquear al llamador.
     * Mantenida para los call-sites existentes que no necesitan esperar el resultado.
     *
     * @param gid Identificador del grupo personal del usuario (necesario para el refresco
     *   final de tareas).
     */
    fun syncOfflineActions(gid: Int) {
        viewModelScope.launch { syncOfflineActionsAwait(gid) }
    }

    /**
     * Procesa la cola completa de [PendingAction] en orden y las sincroniza con el
     * servidor. Resuelve la traducción de IDs temporales (negativos) a IDs reales del
     * servidor mediante un mapa `idMap` que se actualiza con cada `CREATE_TASK` exitoso.
     *
     * Flujo:
     * 1. Si la cola está vacía, refresca las tareas y retorna.
     * 2. Itera cada acción; si su `targetId` está en `idMap`, actualiza el ID antes
     *    de procesar.
     * 3. Para `CREATE_TASK`, registra el mapeo `tempId -> serverId` en `idMap` y
     *    actualiza las referencias en Room y en la cola.
     * 4. Si cualquier acción falla, programa [SyncWorker] y aborta; las acciones
     *    restantes serán reintentadas en el próximo ciclo.
     * 5. Al vaciar la cola, refresca las tareas desde el servidor.
     *
     * Los llamadores que necesitan aguardar la finalización (p. ej. para actualizar
     * XP tras la promoción de invitado) deben usar esta función suspendida.
     *
     * @param gid Identificador del grupo personal del usuario.
     */
    suspend fun syncOfflineActionsAwait(gid: Int) {
        val pending = actionDao.getAllPending().toMutableList()
        if (pending.isEmpty()) {
            loadTareas(gid)
            return
        }

        Log.d("AstraisTasks", "Sincronización: Se han encontrado ${pending.size} acciones pendientes. Intentando procesar...")

        val idMap = mutableMapOf<Int, Int>()

        for (i in pending.indices) {
            var action = pending[i]

            if (idMap.containsKey(action.targetId)) {
                action = action.copy(targetId = idMap[action.targetId])
                pending[i] = action
            }

            try {
                Log.d("AstraisTasks", "Sincronizando: ${action.type} [ID: ${action.targetId}]")
                when (action.type) {
                    "COMPLETE_TASK" -> {
                        repository.completarTarea(action.targetId!!)
                    }
                    "CREATE_TASK" -> {
                        val request = Json.decodeFromString<CreateTareaRequest>(action.data)
                        val updatedRequest = if (request.idObjetivo != null && idMap.containsKey(request.idObjetivo)) {
                            request.copy(idObjetivo = idMap[request.idObjetivo])
                        } else {
                            request
                        }

                        val newId = repository.createTareaDirect(updatedRequest)

                        idMap[action.targetId!!] = newId
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
                actionDao.removeAction(action)
            } catch (e: Exception) {
                Log.e("AstraisTasks", "Fallo en sincronización de la acción ${action.type}: ${e.message}", e)
                scheduleSync(context)
                return
            }
        }

        loadTareas(gid)
    }

}
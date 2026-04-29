package com.mm.astraisandroid.data.repository

import com.mm.astraisandroid.data.api.CreateTareaHabitData
import com.mm.astraisandroid.data.api.CreateTareaRequest
import com.mm.astraisandroid.data.api.CreateTareaUniqueData
import com.mm.astraisandroid.data.api.EditTareaRequest
import com.mm.astraisandroid.data.api.HabitFrequency
import com.mm.astraisandroid.data.api.services.TaskApi
import com.mm.astraisandroid.data.api.toEntity
import com.mm.astraisandroid.data.local.dao.ActionDao
import com.mm.astraisandroid.data.local.dao.TareaDao
import com.mm.astraisandroid.data.local.entities.PendingAction
import com.mm.astraisandroid.data.local.entities.TareaEntity
import com.mm.astraisandroid.data.local.entities.toDomain
import com.mm.astraisandroid.data.preferences.SessionManager
import com.mm.astraisandroid.data.models.Task
import com.mm.astraisandroid.data.models.TaskPriority
import com.mm.astraisandroid.data.models.TaskType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import javax.inject.Inject

/**
 * Repositorio de tareas de la capa de datos del cliente Android.
 *
 * Actúa como única fuente de verdad para los datos de tareas, coordinando las
 * operaciones de red ([TaskApi]) con el almacenamiento local ([TareaDao]) y la
 * cola de acciones pendientes ([ActionDao]) para soporte offline-first.
 *
 * Estrategia general:
 * - **Lectura**: se expone [allTareas] como [Flow] de Room, actualizando la UI
 *   de forma reactiva sin bloqueos.
 * - **Escritura en línea**: el método de red se llama directamente (sin caché).
 * - **Escritura optimista**: los métodos `*Optimistic` modifican Room de inmediato
 *   y encolan una [PendingAction] en [ActionDao] para que [SyncWorker] la envíe
 *   al servidor cuando haya conectividad.
 * - **Modo invitado**: las tareas con ID negativo son locales; [migrateGuestTasksToServer]
 *   las promueve al servidor al registrarse el usuario.
 *
 * Esta clase es inyectada por Hilt como singleton en el grafo de dependencias.
 *
 * @property api Servicio HTTP para comunicarse con la API de tareas del backend.
 * @property tareaDao DAO de Room para persistir y consultar tareas localmente.
 * @property actionDao DAO de Room para gestionar la cola de acciones pendientes de sincronización.
 */
class TaskRepository @Inject constructor(
    private val api: TaskApi,
    private val tareaDao: TareaDao,
    private val actionDao: ActionDao
) {
    /**
     * Flujo reactivo que emite la lista completa de tareas almacenadas localmente,
     * mapeadas al modelo de dominio [Task]. Cualquier cambio en Room se propaga
     * automáticamente a los colectores (normalmente el ViewModel).
     */
    val allTareas: Flow<List<Task>> = tareaDao.getAllTareas().map { entidades ->
        entidades.map { it.toDomain() }
    }

    /**
     * Sincroniza las tareas del grupo con el servidor usando estrategia MERGE.
     * Llama a `POST /tasks/{gid}` y actualiza Room con los nuevos datos,
     * preservando las tareas locales pendientes de sincronización y las tareas
     * de invitado (ID negativo) para no perder trabajo offline.
     *
     * @param gid Identificador del grupo cuyas tareas se quieren sincronizar.
     * @return [Result.success] si la sincronización fue exitosa; [Result.failure] si
     *   la petición de red falló.
     */
    suspend fun refreshTareas(gid: Int): Result<Unit> = runCatching {
        val remoteTasks = api.getTareas(gid)
        val remoteEntities = remoteTasks.map { it.toEntity() }
        tareaDao.syncRemoteTasks(remoteEntities)
    }

    /**
     * Obtiene directamente del servidor la lista de tareas de un grupo sin tocar Room.
     * Útil para pantallas de detalle (p. ej. [GroupDetailViewModel]) que necesitan
     * datos frescos de forma efímera.
     *
     * @param gid Identificador del grupo.
     * @return Lista de [TaskResponse] con las tareas del grupo.
     * @throws Exception Si la petición de red falla.
     */
    suspend fun getTasksRemote(gid: Int) = api.getTareas(gid)

    /**
     * Marca una tarea como completada directamente en el servidor.
     * El servidor otorga las recompensas de XP y Ludiones al usuario autenticado.
     * No modifica Room; el llamador debe refrescar el estado si es necesario.
     *
     * @param tid Identificador de la tarea a completar.
     * @throws Exception Si la petición de red falla.
     */
    suspend fun completarTarea(tid: Int) {
        api.completarTarea(tid)
    }

    /**
     * Crea una nueva tarea en el servidor construyendo el [CreateTareaRequest] apropiado
     * según el tipo de tarea. No persiste el resultado en Room; el llamador debe
     * invocar [refreshTareas] después para actualizar la UI.
     *
     * @param gid Identificador del grupo en el que se crea la tarea.
     * @param title Título de la nueva tarea.
     * @param description Descripción de la nueva tarea.
     * @param type Tipo de tarea ([TaskType.UNICO], [TaskType.HABITO] u [TaskType.OBJETIVO]).
     * @param priority Prioridad de la tarea.
     * @param dueDate Fecha límite en formato `YYYY-MM-DD` para tareas únicas, o `null`.
     * @param frecuencia Frecuencia para hábitos, o `null`.
     * @param parentId ID de la tarea objetivo padre si es subtarea, o `null`.
     * @throws Exception Si la petición de red falla.
     */
    suspend fun createNewTask(
        gid: Int,
        title: String,
        description: String,
        type: TaskType,
        priority: TaskPriority,
        dueDate: String? = null,
        frecuencia: HabitFrequency? = null,
        parentId: Int? = null
    ) {
        val networkPriority = priority.ordinal

        val extraUnico = if (type == TaskType.UNICO) {
            val safeDate = if (!dueDate.isNullOrBlank()) "${dueDate}T23:59:59Z" else "2026-12-31T23:59:59Z"
            CreateTareaUniqueData(fechaLimite = safeDate)
        } else null

        val extraHabito = if (type == TaskType.HABITO && frecuencia != null) {
            CreateTareaHabitData(
                frequency = frecuencia,
                numeroFrecuencia = 1
            )
        } else null

        val request = CreateTareaRequest(
            gid = gid,
            titulo = title,
            descripcion = description,
            tipo = type.name,
            prioridad = networkPriority,
            extraUnico = extraUnico,
            extraHabito = extraHabito,
            idObjetivo = parentId
        )

        api.createTarea(request)
    }

    /**
     * Crea una nueva tarea en el servidor enviando el [CreateTareaRequest] directamente.
     * Variante de bajo nivel usada por el ViewModel cuando ya tiene el request
     * completamente construido (p. ej. desde la cola de acciones pendientes).
     *
     * @param request Cuerpo completo de la petición de creación.
     * @return Identificador de la tarea asignado por el servidor.
     * @throws Exception Si la petición de red falla.
     */
    suspend fun createTareaDirect(request: CreateTareaRequest): Int {
        return api.createTarea(request)
    }

    /**
     * Elimina permanentemente una tarea del servidor.
     * No elimina el registro local; el llamador debe gestionar la consistencia de Room
     * (normalmente eliminando el registro antes de llamar a este método para una UI optimista).
     *
     * @param tid Identificador de la tarea a eliminar.
     * @throws Exception Si la petición de red falla.
     */
    suspend fun eliminarTarea(tid: Int) {
        api.deleteTarea(tid)
    }

    /**
     * Edita los campos de una tarea existente en el servidor de forma parcial.
     * Construye el [EditTareaRequest] adecuado y llama a `PATCH /tasks/{tid}/edit`.
     * Los campos `null` no se envían al servidor.
     *
     * @param tid Identificador de la tarea a editar.
     * @param titulo Nuevo título, o `null` para no modificarlo.
     * @param descripcion Nueva descripción, o `null` para no modificarla.
     * @param prioridad Nueva prioridad, o `null` para no modificarla.
     * @param dueDate Nueva fecha límite en formato `YYYY-MM-DD` (solo para `UNICO`), o `null`.
     * @param frecuencia Nueva frecuencia (solo para `HABITO`), o `null`.
     * @throws Exception Si la petición de red falla.
     */
    suspend fun editarTarea(
        tid: Int,
        titulo: String?,
        descripcion: String?,
        prioridad: TaskPriority?,
        dueDate: String? = null,
        frecuencia: HabitFrequency? = null
    ) {
        val extraUnico = if (dueDate != null) CreateTareaUniqueData(dueDate) else null
        val extraHabito = if (frecuencia != null) CreateTareaHabitData(1, frecuencia) else null

        val req = EditTareaRequest(
            titulo = titulo,
            descripcion = descripcion,
            prioridad = prioridad?.ordinal,
            extraUnico = extraUnico,
            extraHabito = extraHabito
        )
        api.editarTarea(tid, req)
    }

    /**
     * Revierte el estado de completado de una tarea en el servidor.
     * El servidor descuenta las recompensas al usuario y retrocede la racha
     * si es un hábito completado hoy.
     *
     * @param tid Identificador de la tarea a revertir.
     * @throws Exception Si la petición de red falla.
     */
    suspend fun uncompleteTarea(tid: Int) {
        api.uncompleteTarea(tid)
    }

    /**
     * Borra todos los datos locales de tareas y acciones pendientes.
     * Invocado durante el cierre de sesión para evitar que datos de un usuario
     * anterior sean visibles al siguiente usuario de la aplicación.
     */
    suspend fun clearLocalData() {
        tareaDao.clearAll()
        actionDao.clearAll()
    }

    /**
     * Completa una tarea de forma optimista: marca el registro de Room como completado
     * de inmediato y encola una [PendingAction] de tipo `COMPLETE_TASK` para sincronizar
     * con el servidor cuando haya conectividad.
     * Si todas las subtareas del objetivo padre han sido completadas, también completa
     * el objetivo automáticamente.
     *
     * @param tid Identificador de la tarea a completar.
     * @param parentId Identificador del objetivo padre si la tarea es una subtarea, o `null`.
     * @return [Result.success] siempre (la acción fallida será reintentada por [SyncWorker]).
     */
    suspend fun completeTaskOptimistic(tid: Int, parentId: Int?): Result<Unit> {
        val shouldCompleteParent = parentId?.let { pid ->
            tareaDao.getSubtasksForTask(pid).all { it.estado == "COMPLETE" || it.id == tid }
        } ?: false

        val action = PendingAction(
            type = "COMPLETE_TASK",
            targetId = tid,
            data = ""
        )
        tareaDao.completeTaskAndQueueAction(tid, action, actionDao)

        if (shouldCompleteParent && parentId != null) {
            val parentAction = PendingAction(
                type = "COMPLETE_TASK",
                targetId = parentId,
                data = ""
            )
            tareaDao.completeTaskAndQueueAction(parentId, parentAction, actionDao)
        }

        return Result.success(Unit)
    }

    /**
     * Revierte el estado de completado de una tarea de forma optimista: actualiza Room
     * de inmediato y encola una [PendingAction] de tipo `UNCOMPLETE_TASK`.
     * Si el objetivo padre estaba completado, lo reactiva también.
     *
     * @param tid Identificador de la tarea a revertir.
     * @param parentId Identificador del objetivo padre si la tarea es una subtarea, o `null`.
     * @return [Result.success] siempre.
     */
    suspend fun uncompleteTaskOptimistic(tid: Int, parentId: Int?): Result<Unit> {
        val shouldUncompleteParent = parentId?.let { pid ->
            tareaDao.getTaskById(pid)?.estado == "COMPLETE"
        } ?: false

        val action = PendingAction(type = "UNCOMPLETE_TASK", targetId = tid, data = "")
        tareaDao.uncompleteTaskAndQueueAction(tid, action, actionDao)

        if (shouldUncompleteParent && parentId != null) {
            val parentAction = PendingAction(type = "UNCOMPLETE_TASK", targetId = parentId, data = "")
            tareaDao.uncompleteTaskAndQueueAction(parentId, parentAction, actionDao)
        }

        return Result.success(Unit)
    }

    /**
     * Crea una tarea de forma optimista: inserta un registro temporal en Room con un ID
     * negativo (para distinguirlo de IDs del servidor), encola una [PendingAction] de tipo
     * `CREATE_TASK` y devuelve el ID temporal inmediatamente.
     * [SyncWorker] enviará la petición al servidor y actualizará el ID cuando sea posible.
     *
     * @param gid Identificador del grupo.
     * @param title Título de la nueva tarea.
     * @param description Descripción de la nueva tarea.
     * @param type Tipo de tarea.
     * @param priority Prioridad de la tarea.
     * @param dueDate Fecha límite para tareas únicas, o `null`.
     * @param frecuencia Frecuencia para hábitos, o `null`.
     * @param parentId ID del objetivo padre si es subtarea, o `null`.
     * @return [Result.success] con el ID temporal negativo asignado localmente.
     */
    suspend fun createTaskOptimistic(
        gid: Int,
        title: String,
        description: String,
        type: TaskType,
        priority: TaskPriority,
        dueDate: String? = null,
        frecuencia: HabitFrequency? = null,
        parentId: Int? = null
    ): Result<Int> {
        val tempId = -(System.currentTimeMillis() % 100000).toInt()
        val networkPriority = priority.ordinal

        val extraUnico = if (type == TaskType.UNICO) {
            val safeDate = if (!dueDate.isNullOrBlank()) "${dueDate}T23:59:59Z" else "2026-12-31T23:59:59Z"
            CreateTareaUniqueData(fechaLimite = safeDate)
        } else null

        val extraHabito = if (type == TaskType.HABITO && frecuencia != null) {
            CreateTareaHabitData(frequency = frecuencia, numeroFrecuencia = 1)
        } else null

        val request = CreateTareaRequest(
            gid = gid,
            titulo = title,
            descripcion = description,
            tipo = type.name,
            prioridad = networkPriority,
            extraUnico = extraUnico,
            extraHabito = extraHabito,
            idObjetivo = parentId
        )

        val entity = TareaEntity(
            id = tempId,
            titulo = title,
            descripcion = description,
            tipo = type.name,
            estado = "ACTIVE",
            prioridad = networkPriority,
            recompensaXp = 0,
            recompensaLudion = 0,
            isPendingSync = true,
            idObjetivo = parentId,
            extraUnicoFecha = extraUnico?.fechaLimite,
            extraHabitoFrecuencia = extraHabito?.frequency?.name
        )

        val action = PendingAction(
            type = "CREATE_TASK",
            targetId = tempId,
            data = Json.encodeToString(CreateTareaRequest.serializer(), request)
        )

        tareaDao.createTaskWithAction(entity, action, actionDao)
        return Result.success(tempId)
    }

    /**
     * Elimina una tarea de forma optimista: borra el registro de Room de inmediato y
     * encola una [PendingAction] de tipo `DELETE_TASK` para sincronizar con el servidor.
     *
     * @param tid Identificador de la tarea a eliminar.
     * @return [Result.success] siempre.
     */
    suspend fun deleteTaskOptimistic(tid: Int): Result<Unit> {
        tareaDao.deleteTareaById(tid)
        val action = PendingAction(type = "DELETE_TASK", targetId = tid, data = "")
        actionDao.addAction(action)
        return Result.success(Unit)
    }

    /**
     * Edita una tarea de forma optimista: actualiza los campos en Room de inmediato y
     * encola una [PendingAction] de tipo `EDIT_TASK` con el JSON de la petición para
     * sincronizar con el servidor cuando haya conectividad.
     *
     * @param tid Identificador de la tarea a editar.
     * @param titulo Nuevo título.
     * @param descripcion Nueva descripción.
     * @param prioridad Nueva prioridad.
     * @param dueDate Nueva fecha límite para tareas únicas, o `null`.
     * @param frecuencia Nueva frecuencia para hábitos, o `null`.
     * @return [Result.success] siempre.
     */
    suspend fun editTaskOptimistic(
        tid: Int,
        titulo: String,
        descripcion: String,
        prioridad: TaskPriority,
        dueDate: String? = null,
        frecuencia: HabitFrequency? = null
    ): Result<Unit> {
        tareaDao.updateTareaDetails(titulo, descripcion, prioridad.ordinal, tid)

        val extraUnico = dueDate?.let { CreateTareaUniqueData(it) }
        val extraHabito = frecuencia?.let { CreateTareaHabitData(1, it) }

        val request = EditTareaRequest(
            titulo = titulo,
            descripcion = descripcion,
            prioridad = prioridad.ordinal,
            extraUnico = extraUnico,
            extraHabito = extraHabito
        )

        val action = PendingAction(
            type = "EDIT_TASK",
            targetId = tid,
            data = Json.encodeToString(EditTareaRequest.serializer(), request)
        )
        actionDao.addAction(action)
        return Result.success(Unit)
    }

    /**
     * Migra las tareas creadas en modo invitado al servidor tras el registro del usuario.
     *
     * El proceso en dos pasos:
     * 1. **Re-empaquetado**: recorre las [PendingAction] de tipo `CREATE_TASK` existentes
     *    que referencian el GID de invitado (`GUEST_GID`) y las actualiza con el GID real
     *    del grupo del usuario recién registrado.
     * 2. **Encolado de huérfanos**: para las tareas locales con ID negativo que no tienen
     *    una acción pendiente asociada, crea nuevas [PendingAction] asegurando que los
     *    objetivos padre se encolan antes que sus subtareas para que [SyncWorker] pueda
     *    resolver la traducción de IDs en orden.
     *
     * @param gid El GID real del grupo personal del usuario recién registrado.
     */
    suspend fun migrateGuestTasksToServer(gid: Int) {
        // STEP 1: Re-pack existing pending actions to swap fake GID -> real GID
        val existingActions = actionDao.getAllPendingOrdered()
        val existingCreateTargetIds = mutableSetOf<Int>()

        for (action in existingActions) {
            if (action.type == "CREATE_TASK" && action.data.isNotBlank()) {
                runCatching {
                    val req = Json.decodeFromString<CreateTareaRequest>(action.data)
                    if (req.gid == SessionManager.GUEST_GID) {
                        val updated = req.copy(gid = gid)
                        val newJson = Json.encodeToString(CreateTareaRequest.serializer(), updated)
                        actionDao.updateActionData(action.actionId, newJson)
                    }
                }
                action.targetId?.let { existingCreateTargetIds.add(it) }
            }
        }

        val guestTareas = tareaDao.getGuestTareas().sortedByDescending { it.idObjetivo == null }
        if (guestTareas.isEmpty()) return

        for (tarea in guestTareas) {
            if (tarea.id in existingCreateTargetIds) continue

            val habitFrequencyEnum = runCatching {
                tarea.extraHabitoFrecuencia?.let { HabitFrequency.valueOf(it) }
            }.getOrNull()

            val request = CreateTareaRequest(
                gid = gid,
                titulo = tarea.titulo,
                descripcion = tarea.descripcion,
                tipo = tarea.tipo,
                prioridad = tarea.prioridad,
                extraUnico = if (tarea.tipo == "UNICO")
                    CreateTareaUniqueData(tarea.extraUnicoFecha ?: "2026-12-31T23:59:59Z") else null,
                extraHabito = if (tarea.tipo == "HABITO")
                    CreateTareaHabitData(1, habitFrequencyEnum ?: HabitFrequency.DAILY) else null,
                idObjetivo = tarea.idObjetivo
            )

            val dataString = Json.encodeToString(CreateTareaRequest.serializer(), request)

            tareaDao.createTaskWithAction(
                entity = tarea.copy(isPendingSync = true),
                action = PendingAction(
                    type = "CREATE_TASK",
                    targetId = tarea.id,
                    data = dataString
                ),
                actionDao = actionDao
            )
        }
    }
}

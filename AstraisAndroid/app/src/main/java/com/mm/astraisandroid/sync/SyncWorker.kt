package com.mm.astraisandroid.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.mm.astraisandroid.data.api.CreateTareaRequest
import com.mm.astraisandroid.data.api.EditTareaRequest
import com.mm.astraisandroid.data.api.services.TaskApi
import com.mm.astraisandroid.data.local.dao.ActionDao
import com.mm.astraisandroid.data.local.dao.TareaDao
import com.mm.astraisandroid.data.local.entities.PendingAction
import com.mm.astraisandroid.data.preferences.SessionManager
import com.mm.astraisandroid.util.logging.AppLogger
import com.mm.astraisandroid.util.logging.LogFeature
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.serialization.json.Json

/**
 * [CoroutineWorker] encargado de sincronizar las acciones pendientes de tareas
 * con el servidor en segundo plano.
 *
 * Procesa la cola de [PendingAction] en orden, resolviendo IDs temporales locales
 * a IDs reales del servidor y gestionando reintentos ante fallos transitorios.
 *
 * @property taskApi API de tareas para comunicación con el backend.
 * @property actionDao DAO de acciones pendientes en Room.
 * @property tareaDao DAO de tareas en Room para actualizar referencias de IDs.
 * @property sessionManager Gestor de sesión para verificar si el usuario es invitado.
 * @property logger Logger estructurado para trazabilidad.
 */
@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val taskApi: TaskApi,
    private val actionDao: ActionDao,
    private val tareaDao: TareaDao,
    private val sessionManager: SessionManager,
    private val logger: AppLogger
) : CoroutineWorker(appContext, params) {

    companion object {
        /** Número máximo de reintentos permitidos antes de descartar una acción. */
        const val MAX_RETRIES = 3
    }

    /**
     * Punto de entrada del worker. Itera todas las acciones pendientes y las
     * procesa, aplicando reintentos o descartes según el resultado.
     *
     * @return [Result.success] si todo se sincronizó o no había nada que hacer;
     *   [Result.retry] si al menos una acción falló de forma recuperable.
     */
    override suspend fun doWork(): Result {
        if (sessionManager.isGuest()) {
            logger.d(LogFeature.SYNC, "Skipping sync: user is in guest mode")
            return Result.success()
        }

        val pendingActions = actionDao.getAllPendingOrdered()
        if (pendingActions.isEmpty()) {
            return Result.success()
        }

        val idMap = mutableMapOf<Int, Int>()
        var hasRetryableFailures = false

        for (action in pendingActions) {
            val resolvedAction = action.targetId?.let { oldId ->
                idMap[oldId]?.let { newId -> action.copy(targetId = newId) } ?: action
            } ?: action

            val result = processAction(resolvedAction, idMap)

            when (result) {
                is ActionResult.Success -> {
                    actionDao.deleteAction(action.actionId)
                }
                is ActionResult.Retryable -> {
                    hasRetryableFailures = true
                    actionDao.incrementRetryCount(action.actionId, result.error)
                    logger.w(LogFeature.SYNC, "Retryable failure for ${action.type}: ${result.error}")
                }
                is ActionResult.PermanentFailure -> {
                    logger.e(LogFeature.SYNC, "Permanent failure for ${action.type}: ${result.error}")
                    actionDao.deleteAction(action.actionId)
                }
            }
        }

        actionDao.deleteExceededRetries(MAX_RETRIES)

        return if (hasRetryableFailures) Result.retry() else Result.success()
    }

    /**
     * Delega la acción pendiente al manejador correspondiente según su tipo.
     *
     * @param action Acción a procesar.
     * @param idMap Mapa de traducción de IDs temporales a IDs reales del servidor.
     * @return Resultado de la operación clasificado como éxito, reintentable o fallo permanente.
     */
    private suspend fun processAction(action: PendingAction, idMap: MutableMap<Int, Int>): ActionResult {
        return try {
            when (action.type) {
                "COMPLETE_TASK" -> handleComplete(action)
                "UNCOMPLETE_TASK" -> handleUncomplete(action)
                "CREATE_TASK" -> handleCreate(action, idMap)
                "DELETE_TASK" -> handleDelete(action)
                "EDIT_TASK" -> handleEdit(action)
                else -> ActionResult.PermanentFailure("Unknown action type: ${action.type}")
            }
        } catch (e: Exception) {
            classifyError(e)
        }
    }

    /**
     * Sincroniza una acción de completar tarea con el servidor.
     *
     * @param action Acción que contiene el ID de la tarea a completar.
     * @return [ActionResult.Success] si la operación remota tuvo éxito.
     */
    private suspend fun handleComplete(action: PendingAction): ActionResult {
        action.targetId?.let { taskApi.completarTarea(it) }
        return ActionResult.Success
    }

    /**
     * Sincroniza una acción de deshacer completado de tarea con el servidor.
     *
     * @param action Acción que contiene el ID de la tarea a deshacer.
     * @return [ActionResult.Success] si la operación remota tuvo éxito.
     */
    private suspend fun handleUncomplete(action: PendingAction): ActionResult {
        action.targetId?.let { taskApi.uncompleteTarea(it) }
        return ActionResult.Success
    }

    /**
     * Sincroniza una acción de crear tarea, resolviendo referencias de objetivo padre
     * y registrando el mapeo de ID temporal a ID real del servidor.
     *
     * @param action Acción que contiene el JSON serializado de [CreateTareaRequest].
     * @param idMap Mapa mutable donde se registra la traducción de IDs.
     * @return [ActionResult.Success] si la tarea fue creada en el servidor.
     */
    private suspend fun handleCreate(action: PendingAction, idMap: MutableMap<Int, Int>): ActionResult {
        val request = Json.decodeFromString<CreateTareaRequest>(action.data)

        val resolvedRequest = request.idObjetivo?.let { parentId ->
            idMap[parentId]?.let { newParentId -> request.copy(idObjetivo = newParentId) } ?: request
        } ?: request

        val newId = taskApi.createTarea(resolvedRequest)

        action.targetId?.let { oldId ->
            idMap[oldId] = newId
            tareaDao.updateIdAndParentReferences(oldId, newId)
            actionDao.updateTargetIds(oldId, newId)
        }

        return ActionResult.Success
    }

    /**
     * Sincroniza una acción de eliminar tarea con el servidor.
     *
     * Un error 404 se trata como éxito porque la tarea ya no existe en el servidor.
     *
     * @param action Acción que contiene el ID de la tarea a eliminar.
     * @return [ActionResult.Success] si la tarea fue eliminada o ya no existía.
     */
    private suspend fun handleDelete(action: PendingAction): ActionResult {
        return try {
            action.targetId?.let { taskApi.deleteTarea(it) }
            ActionResult.Success
        } catch (e: Exception) {
            if (extractHttpCode(e.message) == 404) ActionResult.Success else throw e
        }
    }

    /**
     * Sincroniza una acción de editar tarea con el servidor.
     *
     * @param action Acción que contiene el JSON serializado de [EditTareaRequest].
     * @return [ActionResult.Success] si la edición fue aplicada en el servidor.
     */
    private suspend fun handleEdit(action: PendingAction): ActionResult {
        val request = Json.decodeFromString<EditTareaRequest>(action.data)
        action.targetId?.let { taskApi.editarTarea(it, request) }
        return ActionResult.Success
    }

    /**
     * Clasifica una excepción en un [ActionResult] según el código HTTP contenido
     * en el mensaje de error.
     *
     * @param e Excepción capturada durante el procesamiento de una acción.
     * @return [ActionResult.Retryable] para errores de red o servidor;
     *   [ActionResult.PermanentFailure] para errores de cliente (4xx) o conflictos.
     */
    private fun classifyError(e: Exception): ActionResult {
        val msg = e.message ?: "Unknown error"
        return when (extractHttpCode(msg)) {
            400, 401, 403, 404, 409 -> ActionResult.PermanentFailure(msg)
            429, 500, 502, 503, 504 -> ActionResult.Retryable(msg)
            null -> ActionResult.Retryable(msg)
            else -> ActionResult.Retryable(msg)
        }
    }

    /**
     * Extrae un código HTTP de tres dígitos de un mensaje de error.
     *
     * @param msg Mensaje de error que puede contener un patrón como "HTTP 404".
     * @return El código HTTP como entero, o `null` si no se encontró.
     */
    private fun extractHttpCode(msg: String?): Int? {
        if (msg == null) return null
        val match = Regex("""HTTP\s+(\d{3})""").find(msg) ?: return null
        return match.groupValues[1].toIntOrNull()
    }

    /**
     * Resultado posible tras intentar procesar una acción pendiente.
     */
    sealed class ActionResult {
        /** La acción se completó correctamente en el servidor. */
        object Success : ActionResult()
        /** La acción falló pero puede reintentarse en el futuro (errores de red, 5xx, etc.).
         * @property error Mensaje descriptivo del fallo. */
        class Retryable(val error: String?) : ActionResult()
        /** La acción falló de forma irrecuperable (errores de cliente 4xx, datos inválidos, etc.).
         * @property error Mensaje descriptivo del fallo. */
        class PermanentFailure(val error: String?) : ActionResult()
    }
}
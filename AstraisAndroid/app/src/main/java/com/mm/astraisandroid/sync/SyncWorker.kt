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
        const val MAX_RETRIES = 3
    }

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

    private suspend fun handleComplete(action: PendingAction): ActionResult {
        action.targetId?.let { taskApi.completarTarea(it) }
        return ActionResult.Success
    }

    private suspend fun handleUncomplete(action: PendingAction): ActionResult {
        action.targetId?.let { taskApi.uncompleteTarea(it) }
        return ActionResult.Success
    }

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

    private suspend fun handleDelete(action: PendingAction): ActionResult {
        return try {
            action.targetId?.let { taskApi.deleteTarea(it) }
            ActionResult.Success
        } catch (e: Exception) {
            if (extractHttpCode(e.message) == 404) ActionResult.Success else throw e
        }
    }

    private suspend fun handleEdit(action: PendingAction): ActionResult {
        val request = Json.decodeFromString<EditTareaRequest>(action.data)
        action.targetId?.let { taskApi.editarTarea(it, request) }
        return ActionResult.Success
    }

    private fun classifyError(e: Exception): ActionResult {
        val msg = e.message ?: "Unknown error"
        return when (extractHttpCode(msg)) {
            400, 401, 403, 404, 409 -> ActionResult.PermanentFailure(msg)
            429, 500, 502, 503, 504 -> ActionResult.Retryable(msg)
            null -> ActionResult.Retryable(msg)
            else -> ActionResult.Retryable(msg)
        }
    }

    private fun extractHttpCode(msg: String?): Int? {
        if (msg == null) return null
        val match = Regex("""HTTP\s+(\d{3})""").find(msg) ?: return null
        return match.groupValues[1].toIntOrNull()
    }

    sealed class ActionResult {
        object Success : ActionResult()
        class Retryable(val error: String?) : ActionResult()
        class PermanentFailure(val error: String?) : ActionResult()
    }
}
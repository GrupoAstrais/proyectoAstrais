package com.mm.astraisandroid.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.mm.astraisandroid.data.api.CreateTareaRequest
import com.mm.astraisandroid.data.api.client
import com.mm.astraisandroid.data.api.services.TaskApi
import com.mm.astraisandroid.data.local.AstraisDb
import com.mm.astraisandroid.data.local.dao.ActionDao
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.serialization.json.Json

/**
 * Worker encargado de la sincronización de datos en segundo plano.
 * * Esta tarea es gestionada por Android WorkManager y se dispara automáticamente cuando
 * el dispositivo tiene conexión. Su función principal
 * es procesar la cola de acciones pendientes ([com.mm.astraisandroid.data.local.entities.PendingAction])
 * almacenadas en la base de datos local mientras el usuario estaba offline.
 *
 * @param appContext Contexto de la aplicación.
 * @param params Parámetros de configuración del worker.
 */
@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val taskApi: TaskApi,
    private val actionDao: ActionDao
) : CoroutineWorker(appContext, params) {

    /**
     * Ejecuta la lógica de sincronización.
     * * El proceso sigue estos pasos:
     * 1. Recupera todas las acciones pendientes de la base de datos.
     * 2. Itera sobre cada acción e intenta replicarla en el servidor mediante el [BackendRepository].
     * 3. Si el servidor confirma la operación, la acción se elimina de la cola local.
     * 4. Si una operación falla, se mantiene en la base de datos para reintentarse en la próxima ejecución.
     *
     * @return [Result.success] una vez se ha intentado procesar la cola completa,
     * independientemente de si algunas acciones individuales fallaron (para evitar bucles infinitos (ha pasado)).
     */
    override suspend fun doWork(): Result {
        val pendingActions = actionDao.getAllPending()

        if (pendingActions.isEmpty()) {
            return Result.success()
        }

        for (action in pendingActions) {
            val isSuccess = try {
                when (action.type) {
                    "COMPLETE_TASK" -> {
                        taskApi.completarTarea(action.targetId!!)
                        true
                    }
                    "CREATE_TASK" -> {
                        val request = Json.decodeFromString<CreateTareaRequest>(action.data)
                        taskApi.createTarea(request)
                        true
                    }
                    else -> false
                }
            } catch (e: Exception) {
                false
            }

            if (isSuccess) {
                actionDao.removeAction(action)
            }
        }

        return Result.success()
    }
}
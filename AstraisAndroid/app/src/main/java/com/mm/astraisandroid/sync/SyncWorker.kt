package com.mm.astraisandroid.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.mm.astraisandroid.data.api.BackendRepository
import com.mm.astraisandroid.data.api.CreateTareaRequest
import com.mm.astraisandroid.data.local.AstraisDb
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
class SyncWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {

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
        // Se inicializa la base de datos para acceder a la cola de acciones.
        val db = AstraisDb.getInstance(applicationContext)
        val actionDao = db.actionDao()

        val pendingActions = actionDao.getAllPending()

        for (action in pendingActions) {
            val isSuccess = try {
                when (action.type) {
                    "COMPLETE_TASK" -> {
                        // El usuario marcó una tarea como completada offline.
                        BackendRepository.completarTarea(action.targetId!!).isSuccess
                    }
                    "CREATE_TASK" -> {
                        // El usuario creó una tarea offline.
                        // Recuperamos la petición original deserializando el JSON guardado.
                        val request = Json.decodeFromString<CreateTareaRequest>(action.data)
                        BackendRepository.createTarea(request).isSuccess
                    }
                    else -> false
                }
            } catch (e: Exception) {
                // Si el backend da error, la acción permanece en la cola.
                false
            }

            // Si el backend confirma el éxito, limpiamos la acción de la cola local.
            if (isSuccess) {
                actionDao.removeAction(action)
            }
        }

        return Result.success()
    }
}
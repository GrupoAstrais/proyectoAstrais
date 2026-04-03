package com.mm.astraisandroid.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.mm.astraisandroid.data.api.BackendRepository
import com.mm.astraisandroid.data.api.CreateTareaRequest
import com.mm.astraisandroid.data.local.AstraisDb
import kotlinx.serialization.json.Json

/**
 * SyncWorker es una tarea en segundo plano gestionada por Android WorkManager.
 * Su objetivo es ejecutarse automáticamente en el momento en que el dispositivo
 * recupera la conexión a internet para "vaciar" la cola de acciones pendientes.
 * Está esclavizado vaya
 */
class SyncWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        // Se inicializa la base de datos. (Nos hace falta para el worker esclavo)
        // Manuel tenemos que mirar Hilt o Dagger para esto por favo
        val db = AstraisDb.getInstance(applicationContext)
        val actionDao = db.actionDao()

        val pendingActions = actionDao.getAllPending()

        for (action in pendingActions) {
            val isSuccess = try {
                when (action.type) {
                    "COMPLETE_TASK" -> {
                        // El usuario marcó una tarea como completada offline
                        BackendRepository.completarTarea(action.targetId!!).isSuccess
                    }
                    "CREATE_TASK" -> {
                        // El usuario creó una tarea offline
                        // Recuperamos la petición original deserializando el JSON guardado.
                        val request = Json.decodeFromString<CreateTareaRequest>(action.data)
                        BackendRepository.createTarea(request).isSuccess
                    }
                    else -> false
                }
            } catch (e: Exception) {
                // Si el backend da error devolvemos false.
                // La acción no se borrará y se intentará en la próxima sincronización. jEJEJEJEJEJEJ
                false
            }

            // Si el backend nos dio un OK la petición se
            // procesó correctamente y podemos borrarla de la cola local.
            if (isSuccess) {
                actionDao.removeAction(action)
            }
        }

        return Result.success()
    }
}
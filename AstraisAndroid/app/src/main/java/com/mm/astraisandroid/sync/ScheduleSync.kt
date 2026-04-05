package com.mm.astraisandroid.sync

import android.content.Context
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

/**
 * Configura y encola una solicitud de trabajo único para el [SyncWorker].
 *
 * Esta función utiliza la API de **Jetpack WorkManager** para programar la sincronización
 * de datos pendientes con el servidor. La tarea se añade a la cola
 * del sistema y se ejecutará de forma asíncrona incluso si la aplicación se cierra.
 *
 * ### Restricciones (Constraints):
 * Para optimizar los recursos del sistema, la tarea solo se iniciará cuando el dispositivo
 * tenga una conexión de red activa ([NetworkType.CONNECTED]).
 *
 * @param context El contexto de la aplicación necesario para inicializar el servicio de [WorkManager].
 */
fun scheduleSync(context: Context) {
    // Se definen las condiciones bajo las cuales se permite la ejecución.
    val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    // Se crea la petición de trabajo de una sola ejecución (OneTime).
    val syncWorkRequest = OneTimeWorkRequestBuilder<SyncWorker>()
        .setConstraints(constraints)
        .build()

    // Se registra la petición en el gestor de trabajos de Android.
    WorkManager.getInstance(context).enqueue(syncWorkRequest)
}
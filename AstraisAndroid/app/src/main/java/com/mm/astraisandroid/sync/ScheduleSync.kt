package com.mm.astraisandroid.sync

import android.content.Context
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

/**
 * Programa el SyncWorker para que Android lo ejecute en segundo plano.
 */
fun scheduleSync(context: Context) {
    // Esto son las condiciones. Ahora mismo voy a poner solo cuando haya red conectada :D
    val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    // Se crea la petición de trabajo
    val syncWorkRequest = OneTimeWorkRequestBuilder<SyncWorker>()
        .setConstraints(constraints)
        .build()

    // Se pone en la cola de android
    WorkManager.getInstance(context).enqueue(syncWorkRequest)
}
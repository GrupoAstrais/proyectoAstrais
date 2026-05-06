package com.mm.astraisandroid

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.mm.astraisandroid.BuildConfig
import dagger.hilt.android.HiltAndroidApp
import io.ktor.client.HttpClient
import timber.log.Timber
import javax.inject.Inject

/**
 * Clase Application de Astrais, configurada con Hilt para inyección de dependencias.
 *
 * Inicializa Timber para logging en modo debug, provee un [Configuration.Provider] para WorkManager
 * con [HiltWorkerFactory] inyectado por Hilt, y gestiona el ciclo de vida del [HttpClient] compartido.
 */
@HiltAndroidApp
class AstraisApp : Application(), Configuration.Provider {
    /** Fábrica inyectada por Hilt para crear workers de WorkManager con inyección de dependencias. */
    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    /** Instancia compartida del cliente HTTP Ktor para todas las peticiones de red. */
    @Inject
    lateinit var httpClient: HttpClient

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }

    override fun onTerminate() {
        super.onTerminate()
        httpClient.close()
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
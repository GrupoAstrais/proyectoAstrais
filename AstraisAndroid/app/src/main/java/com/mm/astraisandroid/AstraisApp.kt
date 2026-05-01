package com.mm.astraisandroid

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.mm.astraisandroid.BuildConfig
import dagger.hilt.android.HiltAndroidApp
import io.ktor.client.HttpClient
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class AstraisApp : Application(), Configuration.Provider {
    @Inject
    lateinit var workerFactory: HiltWorkerFactory

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
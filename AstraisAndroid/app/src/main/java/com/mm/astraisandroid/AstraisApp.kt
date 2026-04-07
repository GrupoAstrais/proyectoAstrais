package com.mm.astraisandroid

import android.app.Application
import com.mm.astraisandroid.data.preferences.SessionManager
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class AstraisApp : Application() {
    override fun onCreate() {
        super.onCreate()
        SessionManager.init(this)
    }
}
package com.mm.astraisandroid.util.logging

interface AppLogger {
    fun d(feature: LogFeature, message: String, throwable: Throwable? = null)
    fun i(feature: LogFeature, message: String, throwable: Throwable? = null)
    fun w(feature: LogFeature, message: String, throwable: Throwable? = null)
    fun e(feature: LogFeature, message: String, throwable: Throwable? = null)
}

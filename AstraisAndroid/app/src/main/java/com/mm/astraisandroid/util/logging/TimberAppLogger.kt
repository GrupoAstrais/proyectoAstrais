package com.mm.astraisandroid.util.logging

import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TimberAppLogger @Inject constructor() : AppLogger {
    override fun d(feature: LogFeature, message: String, throwable: Throwable?) {
        if (throwable != null) Timber.tag(feature.tag).d(throwable, message)
        else Timber.tag(feature.tag).d(message)
    }

    override fun i(feature: LogFeature, message: String, throwable: Throwable?) {
        if (throwable != null) Timber.tag(feature.tag).i(throwable, message)
        else Timber.tag(feature.tag).i(message)
    }

    override fun w(feature: LogFeature, message: String, throwable: Throwable?) {
        if (throwable != null) Timber.tag(feature.tag).w(throwable, message)
        else Timber.tag(feature.tag).w(message)
    }

    override fun e(feature: LogFeature, message: String, throwable: Throwable?) {
        if (throwable != null) Timber.tag(feature.tag).e(throwable, message)
        else Timber.tag(feature.tag).e(message)
    }
}

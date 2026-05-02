package com.mm.astraisandroid.util.logging

import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementación de [AppLogger] que utiliza Timber como backend de logging.
 *
 * Esta clase está anotada como Singleton para ser inyectada mediante Hilt,
 * asegurando una única instancia en toda la aplicación.
 */
@Singleton
class TimberAppLogger @Inject constructor() : AppLogger {

    /**
     * Envía un log de nivel DEBUG a través de Timber.
     *
     * @param feature Característica asociada al log.
     * @param message Mensaje a registrar.
     * @param throwable Excepción opcional a adjuntar.
     */
    override fun d(feature: LogFeature, message: String, throwable: Throwable?) {
        if (throwable != null) Timber.tag(feature.tag).d(throwable, message)
        else Timber.tag(feature.tag).d(message)
    }

    /**
     * Envía un log de nivel INFO a través de Timber.
     *
     * @param feature Característica asociada al log.
     * @param message Mensaje a registrar.
     * @param throwable Excepción opcional a adjuntar.
     */
    override fun i(feature: LogFeature, message: String, throwable: Throwable?) {
        if (throwable != null) Timber.tag(feature.tag).i(throwable, message)
        else Timber.tag(feature.tag).i(message)
    }

    /**
     * Envía un log de nivel WARNING a través de Timber.
     *
     * @param feature Característica asociada al log.
     * @param message Mensaje a registrar.
     * @param throwable Excepción opcional a adjuntar.
     */
    override fun w(feature: LogFeature, message: String, throwable: Throwable?) {
        if (throwable != null) Timber.tag(feature.tag).w(throwable, message)
        else Timber.tag(feature.tag).w(message)
    }

    /**
     * Envía un log de nivel ERROR a través de Timber.
     *
     * @param feature Característica asociada al log.
     * @param message Mensaje a registrar.
     * @param throwable Excepción opcional a adjuntar.
     */
    override fun e(feature: LogFeature, message: String, throwable: Throwable?) {
        if (throwable != null) Timber.tag(feature.tag).e(throwable, message)
        else Timber.tag(feature.tag).e(message)
    }
}

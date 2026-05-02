package com.mm.astraisandroid.util.logging

/**
 * Interfaz para el sistema de logging de la aplicación.
 *
 * Define los niveles de log estándar (debug, info, warning, error) agrupados por [LogFeature].
 * Permite abstraer la implementación concreta del logger.
 */
interface AppLogger {
    /**
     * Envía un mensaje de nivel DEBUG.
     *
     * @param feature Característica o módulo al que pertenece el log.
     * @param message Mensaje descriptivo.
     * @param throwable Excepción opcional asociada al log.
     */
    fun d(feature: LogFeature, message: String, throwable: Throwable? = null)

    /**
     * Envía un mensaje de nivel INFO.
     *
     * @param feature Característica o módulo al que pertenece el log.
     * @param message Mensaje descriptivo.
     * @param throwable Excepción opcional asociada al log.
     */
    fun i(feature: LogFeature, message: String, throwable: Throwable? = null)

    /**
     * Envía un mensaje de nivel WARNING.
     *
     * @param feature Característica o módulo al que pertenece el log.
     * @param message Mensaje descriptivo.
     * @param throwable Excepción opcional asociada al log.
     */
    fun w(feature: LogFeature, message: String, throwable: Throwable? = null)

    /**
     * Envía un mensaje de nivel ERROR.
     *
     * @param feature Característica o módulo al que pertenece el log.
     * @param message Mensaje descriptivo.
     * @param throwable Excepción opcional asociada al log.
     */
    fun e(feature: LogFeature, message: String, throwable: Throwable? = null)
}

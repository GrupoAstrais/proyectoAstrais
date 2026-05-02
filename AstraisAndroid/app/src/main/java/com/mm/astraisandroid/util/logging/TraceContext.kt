package com.mm.astraisandroid.util.logging

import java.util.UUID

/**
 * Contexto de trazabilidad para el seguimiento de operaciones dentro de la aplicación.
 *
 * Genera un identificador único por instancia que permite correlacionar logs
 * y eventos relacionados con una misma operación o flujo de trabajo.
 */
class TraceContext {
    private val traceId: String = UUID.randomUUID().toString().take(8)

    /**
     * Obtiene el identificador de traza generado.
     *
     * @return Cadena de 8 caracteres que representa el identificador único.
     */
    fun getTraceId(): String = traceId

    /**
     * Prefixa un mensaje con el identificador de traza.
     *
     * @param message Mensaje original.
     * @return Mensaje formateado con el traceId al inicio.
     */
    fun withTrace(message: String): String = "[$traceId] $message"
}

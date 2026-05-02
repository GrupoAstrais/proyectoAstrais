package com.mm.astraisandroid.util.logging

import java.util.UUID

class TraceContext {
    private val traceId: String = UUID.randomUUID().toString().take(8)

    fun getTraceId(): String = traceId

    fun withTrace(message: String): String = "[$traceId] $message"
}

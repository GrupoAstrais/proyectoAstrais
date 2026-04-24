package com.mm.astraisandroid.data.repository

import android.util.Log
import com.mm.astraisandroid.data.api.BASE_URL
import com.mm.astraisandroid.data.preferences.SessionManager
import io.ktor.client.HttpClient
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.prepareGet
import io.ktor.client.statement.bodyAsChannel
import io.ktor.client.plugins.HttpTimeout
import io.ktor.utils.io.readUTF8Line
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.isActive
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SseRepository @Inject constructor(
    private val client: HttpClient,
    private val taskRepository: TaskRepository,
    private val storeRepository: StoreRepository
) {
    private var sseJob: Job? = null

    fun startListening(scope: CoroutineScope) {
        if (sseJob?.isActive == true) return

        sseJob = scope.launch(Dispatchers.IO) {
            while (true) {
                try {
                    val token = SessionManager.getAccessToken()
                    if (token.isNullOrBlank()) {
                        delay(2000)
                        continue
                    }

                    Log.d("SseRepository", "Connecting to SSE...")
                    
                    client.prepareGet("$BASE_URL/events/user") {
                        bearerAuth(token)
                    }.execute { response ->
                        Log.d("SseRepository", "Connected to SSE stream")
                        val channel = response.bodyAsChannel()
                        
                        var currentEvent = ""
                        var currentData = ""

                        while (isActive && !channel.isClosedForRead) {
                            val line = channel.readUTF8Line() ?: break
                            
                            if (line.isEmpty()) {
                                // Dispatch the event
                                if (currentEvent.isNotEmpty() && currentData.isNotEmpty()) {
                                    Log.d("SseRepository", "Received SSE: $currentEvent - $currentData")
                                    when (currentEvent) {
                                        "RELOAD.STORE" -> { }
                                        "ADDED.TASK" -> {
                                            val gid = SessionManager.getPersonalGid()
                                            if (gid != null) {
                                                taskRepository.refreshTareas(gid)
                                            }
                                        }
                                        "SIGN.OFF" -> {
                                            SessionManager.clear()
                                        }
                                    }
                                }
                                currentEvent = ""
                                currentData = ""
                            } else if (line.startsWith("event:")) {
                                currentEvent = line.substringAfter("event:").trim()
                            } else if (line.startsWith("data:")) {
                                currentData = line.substringAfter("data:").trim()
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("SseRepository", "SSE Connection error: ${e.message}")
                }
                
                // Retry after delay
                delay(3000)
            }
        }
    }

    fun stopListening() {
        sseJob?.cancel()
        sseJob = null
    }
}

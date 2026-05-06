package com.mm.astraisandroid.data.api

import com.mm.astraisandroid.data.preferences.SessionManager
import com.mm.astraisandroid.util.logging.AppLogger
import com.mm.astraisandroid.util.logging.LogFeature
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.post
import io.ktor.http.encodedPath
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/** URL base del servidor. */
const val BASE_URL = "http://192.168.1.133:5684"

/**
 * Crea y configura un cliente HTTP de Ktor con todas las capacidades necesarias.
 *
 * Instala los siguientes plugins:
 * - [ContentNegotiation]: Serialización/deserialización JSON con kotlinx.serialization.
 * - [Logging]: Registro de peticiones y respuestas a través de [AppLogger].
 * - [HttpTimeout]: Timeout de 15 segundos para todas las peticiones.
 * - [Auth]: Autenticación Bearer con renovación automática de tokens mediante refresh token.
 *
 * @param sessionManager Gestor de sesiones para obtener y guardar tokens JWT.
 * @param json Configuración de serialización JSON compartida.
 * @param appLogger Sistema de logging estructurado para registrar actividad de red.
 * @return [HttpClient] configurado y listo para realizar peticiones autenticadas.
 */
fun createHttpClient(sessionManager: SessionManager, json: Json, appLogger: AppLogger): HttpClient {
    val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(json)
        }
        install(Logging) {
            level = LogLevel.BODY
            logger = object : Logger {
                override fun log(message: String) {
                    appLogger.d(LogFeature.NETWORK, message)
                }
            }
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 15_000
        }
        install(Auth) {
            bearer {
                loadTokens {
                    BearerTokens(
                        accessToken = sessionManager.getAccessToken() ?: "",
                        refreshToken = sessionManager.getRefreshToken() ?: ""
                    )
                }

                refreshTokens {
                    val refreshToken = sessionManager.getRefreshToken() ?: return@refreshTokens null
                    try {
                        val response: RegenAccessResponse = client.post("${BASE_URL}/auth/regenAccess") {
                            bearerAuth(refreshToken)
                            markAsRefreshTokenRequest()
                        }.body()

                        sessionManager.saveTokens(response.newAccessToken, refreshToken)

                        BearerTokens(
                            accessToken = response.newAccessToken,
                            refreshToken = refreshToken
                        )
                    } catch (e: Exception) {
                        appLogger.e(LogFeature.NETWORK, "Error renovando token: $e", e)
                        sessionManager.clear()
                        null
                    }
                }

                sendWithoutRequest { request ->
                    val path = request.url.encodedPath
                    !path.contains("/auth/login") &&
                            !path.contains("/auth/register") &&
                            !path.contains("/auth/regenAccess")
                }
            }
        }
    }
    return client
}

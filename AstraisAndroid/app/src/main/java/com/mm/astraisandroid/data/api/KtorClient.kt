package com.mm.astraisandroid.data.api

import android.util.Log
import com.mm.astraisandroid.data.preferences.SessionManager
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

val client = HttpClient(Android) {
    install(ContentNegotiation){
        json(Json {
            ignoreUnknownKeys = true
            isLenient = true
        })
    }
    install(Logging) {
        level = LogLevel.BODY
        logger = object : Logger {
            override fun log(message: String) {
                Log.d("KtorClient", message)
            }
        }
    }
    install(HttpTimeout){
        requestTimeoutMillis = 15_000
    }
    install(Auth) {
        bearer {
            loadTokens {
                BearerTokens(
                    accessToken = SessionManager.getAccessToken() ?: "",
                    refreshToken = SessionManager.getRefreshToken() ?: ""
                )
            }

            refreshTokens {
                val refreshToken = SessionManager.getRefreshToken() ?: return@refreshTokens null
                try {
                    val response: RegenAccessResponse = client.post("${BASE_URL}/auth/regenAccess") {
                        bearerAuth(refreshToken)
                        markAsRefreshTokenRequest()
                    }.body()

                    SessionManager.saveTokens(response.newAccessToken, refreshToken)

                    BearerTokens(
                        accessToken  = response.newAccessToken,
                        refreshToken = refreshToken
                    )
                } catch (e: Exception) {
                    Log.e("KtorClient", "Error renovando token: $e")
                    SessionManager.clear()
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
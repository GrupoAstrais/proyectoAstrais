package com.mm.astrais_android

import com.mm.astrais_android.LoginRequest
import com.mm.astrais_android.LoginResponse
import com.mm.astrais_android.RegisterRequest
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.util.network.UnresolvedAddressException
import kotlinx.serialization.SerializationException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class ApiService {

    private val client = KtorClient.instance

    suspend fun login(request: LoginRequest): Result<LoginResponse> {
        return try {
            val response: HttpResponse = client.post("auth/login") {
                setBody(request)
            }

            val body = response.body<LoginResponse>()
            Result.success(body)

        } catch (e: SerializationException) {
            Result.failure(Exception("Error de formato: ${e.message}"))
        } catch (e: UnknownHostException) {
            Result.failure(Exception("Servidor no encontrado"))
        } catch (e: SocketTimeoutException) {
            Result.failure(Exception("Tiempo de espera agotado"))
        } catch (e: UnresolvedAddressException) {
            Result.failure(Exception("Error de conexión: verifica tu IP/red"))
        } catch (e: Exception) {
            Result.failure(Exception("Error: ${e.message}"))
        }
    }

    suspend fun register(request: RegisterRequest): Result<String> {
        return try {
            val response: HttpResponse = client.post("auth/register") {
                setBody(request)
            }

            val body = response.body<String>()
            Result.success(body)

        } catch (e: Exception) {
            Result.failure(Exception("Error de registro: ${e.message}"))
        }
    }
}
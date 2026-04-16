package com.mm.astraisandroid.data.api.services

import com.mm.astraisandroid.data.api.*
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.http.HttpStatusCode
import javax.inject.Inject

class StoreApi @Inject constructor(private val client: HttpClient) {

    suspend fun getStoreItems(): List<CosmeticResponse> {
        val req = client.get("$BASE_URL/store/items")
        if (req.status != HttpStatusCode.OK) {
            val errResponse = req.body<ServerErrorResponse>()
            error(errResponse.errorText ?: errResponse.error ?: "Error desconocido")
        }
        return req.body<List<CosmeticResponse>>()
    }

    suspend fun buyCosmetic(id: Int) {
        val req = client.post("$BASE_URL/store/buy/$id")
        if (req.status != HttpStatusCode.OK) error("Fondos insuficientes")
    }

    suspend fun equipCosmetic(id: Int) {
        val req = client.post("$BASE_URL/store/equip/$id")
        if (req.status != HttpStatusCode.OK) error("Error al equipar")
    }
}
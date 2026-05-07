package com.mm.astraisandroid.data.api.services

import com.mm.astraisandroid.data.api.*
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.http.HttpStatusCode
import javax.inject.Inject

/**
 * Cliente HTTP para la API de la tienda de Astrais.
 *
 * Encapsula las llamadas de red para obtener artículos de la tienda, comprar cosméticos
 * y equipar cosméticos adquiridos.
 *
 * @property client Cliente HTTP Ktor configurado con autenticación JWT.
 */
class StoreApi @Inject constructor(private val client: HttpClient) {

    /**
     * Obtiene todos los cosméticos disponibles en la tienda.
     *
     * @return Lista de [CosmeticResponse] que representan los artículos de la tienda.
     * @throws IllegalStateException Si el servidor devuelve un estado HTTP no exitoso.
     */
    suspend fun getStoreItems(): List<CosmeticResponse> {
        val req = client.get("$BASE_URL/store/items")
        if (req.status != HttpStatusCode.OK) {
            val errResponse = req.body<ServerErrorResponse>()
            error(errResponse.errorText ?: errResponse.error ?: "Error desconocido")
        }
        return req.body<List<CosmeticResponse>>()
    }

    /**
     * Compra un cosmético usando el saldo de Ludiones del usuario autenticado.
     *
     * @param id Identificador del cosmético a comprar.
     * @throws IllegalStateException Si el servidor devuelve un estado HTTP no exitoso (p. ej., fondos insuficientes).
     */
    suspend fun buyCosmetic(id: Int) {
        val req = client.post("$BASE_URL/store/buy/$id")
        if (req.status != HttpStatusCode.OK) error("Fondos insuficientes")
    }

    /**
     * Equipa un cosmético previamente adquirido para el usuario autenticado.
     *
     * @param id Identificador del cosmético a equipar.
     * @throws IllegalStateException Si el servidor devuelve un estado HTTP no exitoso.
     */
    suspend fun equipCosmetic(id: Int) {
        val req = client.post("$BASE_URL/store/equip/$id")
        if (req.status != HttpStatusCode.OK) error("Error al equipar")
    }
}
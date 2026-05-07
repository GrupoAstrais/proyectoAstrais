package com.mm.astraisandroid.data.repository

import com.mm.astraisandroid.data.api.services.StoreApi
import com.mm.astraisandroid.data.api.toDomain
import com.mm.astraisandroid.data.models.Cosmetic
import javax.inject.Inject

/**
 * Repositorio encargado de la gestión de la tienda y cosméticos.
 *
 * Coordina las llamadas a la API de tienda ([StoreApi]) para obtener el catálogo,
 * comprar cosméticos y equipar los adquiridos.
 *
 * @property api Servicio HTTP para operaciones de tienda.
 */
class StoreRepository @Inject constructor(
    private val api: StoreApi
) {
    /**
     * Obtiene el catálogo completo de cosméticos disponibles en la tienda.
     *
     * @return Lista de modelos de dominio [Cosmetic] con la información de cada ítem.
     * @throws Exception Si la petición de red falla.
     */
    suspend fun getStoreItems(): List<Cosmetic> {
        return api.getStoreItems().map { it.toDomain() }
    }

    /**
     * Compra un cosmético utilizando los Ludiones del usuario autenticado.
     *
     * @param id Identificador del cosmético a comprar.
     * @throws Exception Si la petición de red falla (fondos insuficientes, etc.).
     */
    suspend fun buyCosmetic(id: Int) {
        api.buyCosmetic(id)
    }

    /**
     * Equipa un cosmético previamente adquirido por el usuario.
     *
     * @param id Identificador del cosmético a equipar.
     * @throws Exception Si la petición de red falla.
     */
    suspend fun equipCosmetic(id: Int) {
        api.equipCosmetic(id)
    }
}
package com.mm.astraisandroid.data.repository

import com.mm.astraisandroid.data.api.services.StoreApi
import com.mm.astraisandroid.data.api.toDomain
import com.mm.astraisandroid.data.models.Cosmetic
import javax.inject.Inject

class StoreRepository @Inject constructor(
    private val api: StoreApi
) {
    suspend fun getStoreItems(): List<Cosmetic> {
        return api.getStoreItems().map { it.toDomain() }
    }

    suspend fun buyCosmetic(id: Int) {
        api.buyCosmetic(id)
    }

    suspend fun equipCosmetic(id: Int) {
        api.equipCosmetic(id)
    }
}
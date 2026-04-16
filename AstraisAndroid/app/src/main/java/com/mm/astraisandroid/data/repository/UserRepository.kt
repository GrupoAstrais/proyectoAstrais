package com.mm.astraisandroid.data.repository

import com.mm.astraisandroid.data.api.services.UserApi
import com.mm.astraisandroid.data.api.toDomain
import com.mm.astraisandroid.data.models.User
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val api: UserApi
) {
    suspend fun getMe(): User {
        return api.getMe().toDomain()
    }
}
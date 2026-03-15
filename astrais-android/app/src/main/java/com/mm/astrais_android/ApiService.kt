package com.mm.astrais_android

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("auth/register")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>
}
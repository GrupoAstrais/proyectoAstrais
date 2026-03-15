package com.mm.astrais_android

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val email: String,
    val passwd: String,
)

@Serializable
data class RegisterRequest(
    val name: String,
    val email: String,
    val passwd: String,
    val lang: String,
    val utcOffset: Float = 0f
)

@Serializable
data class LoginResponse(
    var success: Boolean,
    val message: String,
    val token: String? = null
)
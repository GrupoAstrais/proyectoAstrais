package com.mm.astrais_android

data class LoginRequest(
    val name: String,
    val email: String,
    val passwd: String,
    val lang: String
)

data class LoginResponse(
    val success: Boolean,
    val message: String,
    val token: String? = null
)
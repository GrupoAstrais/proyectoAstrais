package com.mm.astraisandroid.data.api

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(val email : String, val passwd : String)

@Serializable
data class RegisterRequest(
    val name : String,
    val email: String,
    val passwd: String,
    val lang : String,
    val utcOffset : Float = 0f
)

@Serializable
data class MailVerifierRequest(val email: String, val code : String)

@Serializable
data class LoginResponse(val jwtAccessToken : String, val jwtRefreshToken : String)

@Serializable
data class RegenAccessResponse(val newAccessToken : String)
package com.mm.astraisandroid.api

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(val email : String, val passwd : String)
@Serializable
data class LoginResponse(val jwtAccessToken : String, val jwtRefreshToken : String)

@Serializable
data class RegisterRequest(val name : String, val email: String, val passwd: String, val lang : String, val utcOffset : Float = 0f)

@Serializable
data class MailVerifierRequest(val email: String, val code : String)

@Serializable
data class RegenAccessResponse(val newAccessToken : String)

@Serializable
data class ServerErrorResponse(
    val errorCode: Int? = null,
    val errorText: String? = null,
    val error: String? = null
)

@Serializable
data class CreateTareaRequest(
    val gid: Int,
    val titulo: String,
    val descripcion: String = "",
    val tipo: String = "UNICO",
    val prioridad: Int = 0,
    val recompensaXp: Int = 0,
    val recompensaLudion: Int = 0
)

@Serializable
data class TareaResponse(
    val id: Int,
    val titulo: String,
    val descripcion: String,
    val tipo: String,
    val estado: String,
    val prioridad: Int,
    val recompensaXp: Int,
    val recompensaLudion: Int
)

@Serializable
data class UserMeResponse(
    val id: Int,
    val nombre: String,
    val nivel: Int,
    val xpActual: Int,
    val xpTotal: Int,
    val personalGid: Int?
)
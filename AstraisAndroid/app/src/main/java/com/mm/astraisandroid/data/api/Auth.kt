package com.mm.astraisandroid.data.api

import kotlinx.serialization.Serializable

/**
 * Datos de autenticación para el inicio de sesión con email y contraseña.
 *
 * @property email Dirección de correo electrónico del usuario.
 * @property passwd Contraseña en texto plano.
 */
@Serializable
data class LoginRequest(val email : String, val passwd : String)

/**
 * Datos necesarios para registrar un nuevo usuario en el sistema.
 *
 * @property name Nombre visible del usuario.
 * @property email Dirección de correo electrónico.
 * @property passwd Contraseña en texto plano.
 * @property lang Código de idioma preferido por el usuario.
 * @property utcOffset Desplazamiento horario del usuario respecto a UTC.
 */
@Serializable
data class RegisterRequest(
    val name : String,
    val email: String,
    val passwd: String,
    val lang : String,
    val utcOffset : Float = 0f
)

/**
 * Datos para la verificación del código de confirmación de email.
 *
 * @property email Dirección de correo electrónico a verificar.
 * @property code Código numérico de verificación recibido por email.
 */
@Serializable
data class MailVerifierRequest(val email: String, val code : String)

/**
 * Respuesta del servidor tras un inicio de sesión exitoso.
 *
 * @property jwtAccessToken Token de acceso JWT para autenticar peticiones posteriores.
 * @property jwtRefreshToken Token de refresco para renovar el token de acceso cuando expire.
 */
@Serializable
data class LoginResponse(val jwtAccessToken : String, val jwtRefreshToken : String)

/**
 * Respuesta del servidor al renovar un token de acceso.
 *
 * @property newAccessToken Nuevo token de acceso JWT.
 */
@Serializable
data class RegenAccessResponse(val newAccessToken : String)
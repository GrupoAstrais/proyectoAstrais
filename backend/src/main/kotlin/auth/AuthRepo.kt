package com.astrais.auth

interface AuthRepo {
    /**
     * Hace un login basico, generando un nuevo token JWT.
     * @param loginRequest La request del usuario para loguearse
     * @return El token JWT, null si no se pudo loguear
     */
    suspend fun performBasicLogin(loginRequest: LoginRequest) : LoginResponse?

    /**
     * Registra a un usuario en la base de datos, con las comprobaciones pertinentes.
     * @param registerRequest La request de registro del cliente
     * @return Booleano diciendo que si se pudo registrar
     */
    suspend fun performBasicRegister(registerRequest: RegisterRequest) : Boolean

    /**
     * Borra un usuario con el ID
     */
    suspend fun deleteUser(uid : Int) : Boolean

    /**
     * Intenta regenerar el token de acceso dado un ID de usuario.
     * @param id El ID del usuario a generar el token de acceso
     * @return El token de acceso, NULL si no se pudo
     */
    suspend fun regenAccessToken(id : Int) : String?
}

fun getAuthRepoImpl() :AuthRepo {
    return AuthRepoImpl()
}
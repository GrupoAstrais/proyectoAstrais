package com.astrais.auth

import com.astrais.db.AuthProvider
import com.astrais.db.EntidadUsuario

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
     * @param uid ID del usuario a borrar
     */
    suspend fun deleteUser(uid : Int) : Boolean

    /**
     * Intenta regenerar el token de acceso dado un ID de usuario.
     * @param id El ID del usuario a generar el token de acceso
     * @return El token de acceso, NULL si no se pudo
     */
    suspend fun regenAccessToken(id : Int) : String?

    /**
     * Intenta loguear o registrar un nuevo usuario con las credenciales de OAuth indicadas
     * @param provider_uid El UID que el proveedor OAuth nos otorga
     * @param auth El tipo de provedor oauth que hace la operacion
     * @return Un par con el uid (-1 si hubo error) y el otro booleano que dice si se tuvo que registrar un usuario
     */
    suspend fun tryLoginOrRegisterOauth(provider_uid : String, auth: AuthProvider) : Pair<Int, Boolean>
}

fun getAuthRepoImpl() :AuthRepo {
    return AuthRepoImpl()
}
package com.astrais.auth

import com.astrais.db.AuthProvider
import auth.types.*

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

    /**
     * Intenta editar datos poco relevantes del usuario como la contraseña o el nombre
     * @param uid ID de usuario
     * @param data Los datos mandados con la request
     */
    suspend fun editUserData(uid : Int, data : EditUserResponse) : EditUserReturn

    /**
     * Hace que el usuario se pueda loguear por un nuevo mail, se tendria que hacer mejor sinceramente el proceso.
     * @param uid ID de usuario
     * @param email Mail de usuario, si nulo, no cambiara el correo.
     * @param rawPassword La contraseña sin hashear, si nulo, no se cambiara la contraseña.
     */
    suspend fun setUserMailLogin(uid: Int, email : String?, rawPassword : String?) : Boolean
}

fun getAuthRepoImpl() :AuthRepo {
    return AuthRepoImpl()
}
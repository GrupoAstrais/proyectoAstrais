package com.mm.astraisandroid.data.repository

import com.mm.astraisandroid.data.api.services.UserApi
import com.mm.astraisandroid.data.api.toDomain
import com.mm.astraisandroid.data.models.User
import javax.inject.Inject

/**
 * Repositorio encargado de la gestión del perfil de usuario.
 *
 * Coordina las llamadas a la API de usuario ([UserApi]) para obtener y actualizar
 * la información del perfil autenticado.
 *
 * @property api Servicio HTTP para operaciones de perfil de usuario.
 */
class UserRepository @Inject constructor(
    private val api: UserApi
) {
    /**
     * Obtiene la información del perfil del usuario autenticado desde el servidor.
     *
     * @return Modelo de dominio [User] con los datos del perfil.
     * @throws Exception Si la petición de red falla.
     */
    suspend fun getMe(): User {
        return api.getMe().toDomain()
    }

    /**
     * Actualiza el nombre de usuario del perfil autenticado.
     *
     * @param uid Identificador del usuario.
     * @param newName Nuevo nombre de usuario.
     * @throws Exception Si la petición de red falla.
     */
    suspend fun updateUsername(uid: Int, newName: String) {
        api.updateUsername(uid, newName)
    }

    /**
     * Actualiza el perfil completo del usuario (nombre e idioma).
     *
     * @param uid Identificador del usuario.
     * @param newName Nuevo nombre de usuario.
     * @param language Nuevo código de idioma.
     * @throws Exception Si la petición de red falla.
     */
    suspend fun updateProfile(uid: Int, newName: String, language: String) {
        api.updateProfile(uid, newName, language)
    }
}
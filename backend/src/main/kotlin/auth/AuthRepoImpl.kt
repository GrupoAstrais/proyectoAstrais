package com.astrais.auth

import com.astrais.db.DatabaseDAO
import com.astrais.db.getDatabaseDaoImpl
import kotlinx.datetime.toKotlinLocalDate
import org.jetbrains.exposed.v1.exceptions.ExposedSQLException
import org.slf4j.LoggerFactory

private val log = LoggerFactory.getLogger(AuthRepoImpl::class.java)

class AuthRepoImpl : AuthRepo{
    override suspend fun performBasicLogin(loginRequest: LoginRequest) : LoginResponse?{
        try {
            val user = getDatabaseDaoImpl().getUsuario(loginRequest.email)
            if (user?.contrasenia != null && checkPassword(loginRequest.passwd, user.contrasenia!!)){
                getDatabaseDaoImpl().setUserLastLogin(user)
                log.debug("Usuario ${user.nombre} conectado, Generando tokens...")
                // Genera JWT
                return LoginResponse(jwtAccessToken = generateAccessToken(user), jwtRefreshToken = generateRefreshToken(user))
            }
        } catch (e : ExposedSQLException){
            log.error("Error trying to log user ${loginRequest.email}! Message: ${e.message}")
        }
        return null
    }

    override suspend fun performBasicRegister(registerRequest: RegisterRequest) : Boolean{
        try {
            val dao : DatabaseDAO = getDatabaseDaoImpl()
            val existeUser = getDatabaseDaoImpl().getUsuario(registerRequest.email) != null

            if (!existeUser){
                val hashContrasenia = hashPassword(registerRequest.passwd)
                dao.createUser(registerRequest.name, registerRequest.email, hashContrasenia, registerRequest.lang)
                log.debug("Usuario ${registerRequest.name} registrado")
                return true
            }
        } catch (e : ExposedSQLException){
            log.error("Error trying to register user ${registerRequest.name} with mail ${registerRequest.email}! Message: ${e.message}")
        }
        return false
    }

    override suspend fun regenAccessToken(id: Int): String? {
        try {
            val user = getDatabaseDaoImpl().getUsuarioByID(id)
            if (user != null){
                return generateAccessToken(user)
            }
        } catch (e : ExposedSQLException){
            log.error("Error trying regen access token for $id! Message: ${e.message}")
        }
        return null
    }
}
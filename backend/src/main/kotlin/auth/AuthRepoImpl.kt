package com.astrais.auth

import com.astrais.db.DatabaseDAO
import com.astrais.db.getDatabaseDaoImpl
import kotlinx.datetime.toKotlinLocalDate
import org.slf4j.LoggerFactory

private val log = LoggerFactory.getLogger(AuthRepoImpl::class.java)

class AuthRepoImpl : AuthRepo{
    override suspend fun performBasicLogin(loginRequest: LoginRequest) : LoginResponse?{
        val user = getDatabaseDaoImpl().getUsuario(loginRequest.email)
        if (user?.contrasenia != null && checkPassword(loginRequest.passwd, user.contrasenia!!)){
            getDatabaseDaoImpl().setUserLastLogin(user)
            log.debug("Usuario ${user.nombre} conectado, Generando tokens...")
            // Genera JWT
            return LoginResponse(jwtAccessToken = generateAccessToken(user), jwtRefreshToken = generateRefreshToken(user))
        }else{
            return null
        }
    }

    override suspend fun performBasicRegister(registerRequest: RegisterRequest) : Boolean{
        val dao : DatabaseDAO = getDatabaseDaoImpl()
        val existeUser = getDatabaseDaoImpl().getUsuario(registerRequest.email) != null

        if (!existeUser){
            val hashContrasenia = hashPassword(registerRequest.passwd)
            dao.createUser(registerRequest.name, registerRequest.email, hashContrasenia, registerRequest.lang)
            log.debug("Usuario ${registerRequest.name} registrado")
            return true
        }
        return false
    }

    override suspend fun regenAccessToken(id: Int): String? {
        val user = getDatabaseDaoImpl().getUsuarioByID(id)
        if (user != null){
            return generateAccessToken(user)
        }else{
            return null
        }
    }
}
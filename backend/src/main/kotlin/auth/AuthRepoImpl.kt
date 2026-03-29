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
                log.debug("User ${user.nombre} (${user.id.value}) connected.")

                // Genera JWT
                val i = LoginResponse(jwtAccessToken = generateAccessToken(user), jwtRefreshToken = generateRefreshToken(user))
                log.debug("Created tokens for ${user.nombre} (${user.id.value})")

                return i
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
                val uid = dao.createUser(registerRequest.name, registerRequest.email, hashContrasenia, registerRequest.lang)
                log.debug("User ${registerRequest.name} (${uid}) registered")

                // Crea grupo personal
                dao.createGroup(uid, "${registerRequest.name}", "", true)
                log.debug("Created the ${registerRequest.name}'s (${uid}) user space")
                return true
            }
        } catch (e : ExposedSQLException){
            log.error("Error trying to register user ${registerRequest.name} with mail ${registerRequest.email}! Message: ${e.message}")
        }
        return false
    }

    override suspend fun deleteUser(uid: Int): Boolean {
        try {
            // TODO: Al implementar SSE, se debera enviar un mensaje de cerrar sesion al borrarlo.
            return getDatabaseDaoImpl().deleteUsuario(uid)
        }catch (e : ExposedSQLException){
            log.error("Error while trying to delete the user with ID $uid")
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
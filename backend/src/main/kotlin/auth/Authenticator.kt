package com.astrais.auth

import at.favre.lib.crypto.bcrypt.BCrypt
import com.astrais.db.getDatabaseDaoImpl
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(val email : String, val passwd : String)
@Serializable
data class LoginResponse(val jwtAccessToken : String, val jwtRefreshToken : String)

@Serializable
data class RegisterRequest(val name : String, val email: String, val passwd: String, val lang : String, val utcOffset : Float = 0f)

@Serializable
data class RegenAccessResponse(val newAccessToken : String)

fun Application.installAuth(){
    install(Authentication){
        this@installAuth.initOauth(this)
        this@installAuth.initJWT(this)
    }
}

fun Route.authRoutes(){
    post("/auth/login") {
        // Se hace el login normal y devuelve su JWT
        try {
            val request = call.receive<LoginRequest>()

            // Comprobacion si alguno de los campos esta vacio
            if (request.email.isBlank() || request.passwd.isBlank()){
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "One of the strings is blank"))
                return@post
            }

            val jwt = getAuthRepoImpl().performBasicLogin(request)
            if (jwt == null){
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "The user does not exist"))
            }else{
                call.respond(HttpStatusCode.OK, jwt)
            }

        } catch (e : BadRequestException){
            call.respond(HttpStatusCode.BadRequest, mapOf("error" to "The data sent by the client was not in the accepted format"))
        }
    }

    post("/auth/register") {
        // Se hace el registro
        try {
            val request = call.receive<RegisterRequest>()

            // Comprobacion si alguno de los campos esta vacio
            if (request.email.isBlank() || request.name.isBlank() || request.passwd.isBlank() || request.lang.isBlank()){
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "One of the strings is blank"))
                return@post
            }

            // Comprueba si el lenguaje es correcto
            if (request.lang.length != 3){
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Language is not following ISO 639-2"))
                return@post
            }

            // Registra al usuario
            if (getAuthRepoImpl().performBasicRegister(request)){
                call.respond(HttpStatusCode.OK)
            }else{
                call.respond(HttpStatusCode.Conflict)
            }

        } catch (e : BadRequestException){
            call.respond(HttpStatusCode.BadRequest, mapOf("error" to "The data sent by the client was not in the accepted format"))
        }
    }

    // Se protege la ruta requiriendo el refresh token para poder entrar
    authenticate("refresh-jwt") {
        post("/auth/regenAccess") {
            // Regenera un AccessToken
            try {
                val token = call.principal<JWTPrincipal>()
                if (token == null){
                    // No deberia ser null, pero se hace la comprobacion por si acaso
                    call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid/Missing refresh token"))
                    return@post
                }

                val user = getAuthRepoImpl().regenAccessToken(token.payload.subject.toInt())
                if (user == null){
                    call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Error regenerating the access token!"))
                }else{
                    call.respond(HttpStatusCode.OK, RegenAccessResponse(user))
                }

            } catch (e : BadRequestException){
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "The data sent by the client was not in the accepted format"))
            } catch (e : NumberFormatException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "The subject of the token is invalid"))
            }
        }
    }

    post("/auth/google") {
        // Se loguea con google y devuelve su JWT
        // TODO: Implementar Oauth
        call.respond(HttpStatusCode.BadGateway, mapOf("error" to "The Oauth wasn't implemented yet"))
    }
}

fun hashPassword(passwd : String) : String{
    return BCrypt.withDefaults().hashToString(8, passwd.toCharArray())
}

fun checkPassword(passwd: String, hash : String) : Boolean {
    return BCrypt.verifyer().verify(passwd.toCharArray(), hash).verified
}

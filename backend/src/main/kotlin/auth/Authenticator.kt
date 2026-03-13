package com.astrais.auth

import at.favre.lib.crypto.bcrypt.BCrypt
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(val email : String, val passwd : String)

fun Application.installAuth(){
    install(Authentication){
        this@installAuth.initOauth(this)
        this@installAuth.initJWT(this)
    }
}

fun Route.authRoutes(){
    post("/auth/login") {
        // Se hace el login normal y devuelve su JWT
        val request = call.receive<LoginRequest>()
    }

    post("/auth/google") {
        // Se loguea con google y devuelve su JWT
    }
}

fun hashPassword(passwd : String) : String{
    return BCrypt.withDefaults().hashToString(8, passwd.toCharArray())
}
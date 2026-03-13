package com.astrais.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import java.util.Date

// https://ktor.io/docs/server-jwt.html

// El nombre del creador del token, se usa en la validacion
private lateinit var jwtIssuer : String
// Para quien es el token. Se podria cambiar
private lateinit var jwtAudience : String
// Se usa de 'firma' del token, para autentificar que es bueno
private lateinit var jwtSecret : String
// MS hasta que expire el token
private var jwtExpiration : Long = 0
// Algoritmo ya prehecho con los datos
private lateinit var jwtAlgorithm : Algorithm


public fun generateUserToken(userID : String) : String{
    return JWT.create()
        .withAudience(jwtAudience)
        .withIssuer(jwtIssuer)
        .withClaim("uid", userID)
        .withExpiresAt(Date(System.currentTimeMillis()+ jwtExpiration))
        .sign(jwtAlgorithm)
}

public fun createVerifier() : JWTVerifier{
    return JWT.require(jwtAlgorithm)
        .withIssuer(jwtIssuer)
        .withAudience(jwtAudience)
        .build()
}

public fun validateUserToken(cred : JWTCredential) : Boolean{
    // TODO: Una validacion real
    return true
}

public fun Application.initJWT(authenticationConfig: AuthenticationConfig) {
    // Cargado de datos
    val conf = environment.config
    jwtIssuer = conf.property("jwt.issuer").getString()
    jwtAudience = conf.property("jwt.audience").getString()
    jwtSecret = conf.property("jwt.secret").getString()
    jwtExpiration = conf.property("jwt.expiration").getString().toLong()
    jwtAlgorithm = Algorithm.HMAC256(jwtSecret)


    authenticationConfig.jwt("auth-jwt") {
        // Pone verificador de tokens.
        verifier(createVerifier())

        // Y ahora valida el token
        validate { cred->
            if (validateUserToken(cred)){
                JWTPrincipal(cred.payload)
            }else{
                null
            }
        }

        // Si tiene error, responde con ese texto
        challenge { _, _ ->
            call.respond("Invalid/expired token",null)
        }
    }
}


package com.astrais.auth

import com.astrais.ErrorCodes
import com.astrais.Errors
import com.astrais.db.EntidadUsuario
import com.astrais.db.getDatabaseDaoImpl
import com.astrais.mainlogger
import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import java.util.Date

// https://ktor.io/docs/server-jwt.html

// El nombre del creador del token, se usa en la validacion
private lateinit var jwtIssuer: String
private lateinit var jwtAudience: String
// MS hasta que expire el token
private var jwtAccessTokenExpiration: Long = 10 * 60 * 1000 // 1 minuto (creo)
private var jwtRefreshTokenExpiration: Long = 7 * 24 * 60 * 60 * 1000
// Algoritmo ya prehecho con los datos
private lateinit var jwtAlgorithmAccess: Algorithm
private lateinit var jwtAlgorithmRefresh: Algorithm

enum class TokenType {
    ACCESS,
    REFRESH
}

public fun generateAccessToken(user: EntidadUsuario): String {
    return JWT.create()
            .withAudience(jwtAudience)
            .withIssuer(jwtIssuer)
            .withSubject(user.id.value.toString())
            .withClaim("tokenType", TokenType.ACCESS.name)
            .withExpiresAt(Date(System.currentTimeMillis() + jwtAccessTokenExpiration))
            .sign(jwtAlgorithmAccess)
}

public fun generateRefreshToken(user: EntidadUsuario): String {
    return JWT.create()
            .withAudience(jwtAudience)
            .withIssuer(jwtIssuer)
            .withSubject(user.id.value.toString())
            .withClaim("tokenType", TokenType.REFRESH.name)
            .withExpiresAt(Date(System.currentTimeMillis() + jwtRefreshTokenExpiration))
            .sign(jwtAlgorithmRefresh)
}

public fun createAccessVerifier(): JWTVerifier {
    return JWT.require(jwtAlgorithmAccess)
            .withAudience(jwtAudience)
            .withIssuer(jwtIssuer)
            .withClaim("tokenType", TokenType.ACCESS.name)
            .build()
}

public fun createRefreshVerifier(): JWTVerifier {
    return JWT.require(jwtAlgorithmRefresh)
            .withAudience(jwtAudience)
            .withIssuer(jwtIssuer)
            .withClaim("tokenType", TokenType.REFRESH.name)
            .build()
}

public suspend fun validateAccessToken(cred: JWTCredential): Boolean {
    try {
        // Comprueba que el usuario de verdad existe
        //val uid = cred.subject?.toInt() ?: 0
        //return getDatabaseDaoImpl().getUsuarioByID(uid) != null
        val uid = cred.subject?.toIntOrNull()
        val claim = cred.payload.getClaim("tokenType").asString()
        println("Claim: ${claim}")
        return uid != null && uid > 0
    } catch (e: NumberFormatException) {
        return false
    }
}

public suspend fun validateRefreshToken(cred: JWTCredential): Boolean {
    // Ahora es lo mismo, pero se deberian de meter condicionales adicionales
    //return validateAccessToken(cred)
    try {
        val uid = cred.subject?.toIntOrNull()
        mainlogger.info("Refresh token with uid: $uid. Is token type: ${cred.payload.getClaim("tokenType").asString()}")
        return uid != null && uid > 0
    } catch (e : NumberFormatException){
        return false
    }
}

public fun Application.initJWT(authenticationConfig: AuthenticationConfig) {
    // Cargado de datos
    val conf = environment.config
    jwtIssuer = conf.property("jwt.issuer").getString()
    jwtAudience = conf.property("jwt.audience").getString()

    val accessSecret = conf.property("jwt.accessSecret").getString()
    jwtAlgorithmAccess = Algorithm.HMAC256(accessSecret)

    val refreshSecret = conf.property("jwt.refreshSecret").getString()
    jwtAlgorithmRefresh = Algorithm.HMAC256(refreshSecret)

    authenticationConfig.jwt("access-jwt") {
        // Pone verificador de tokens.
        verifier(createAccessVerifier())

        // Y ahora valida el token
        validate { cred ->
            println("ACCESS TOKEN CLAIMS: ${cred.payload.claims}")
            if (validateAccessToken(cred)) {
                JWTPrincipal(cred.payload)
            } else {
                null
            }
        }

        // Si tiene error, responde con ese texto
        challenge { _, _ ->
            call.respond(
                    HttpStatusCode.Unauthorized,
                    Errors(ErrorCodes.ERR_INVALIDTOKEN.ordinal, "Invalid/expired token")
            )
        }
    }

    authenticationConfig.jwt("refresh-jwt") {
        verifier(createRefreshVerifier())

        validate { cred ->
            println("REFRESH TOKEN CLAIMS: ${cred.payload.claims}")
            if (validateRefreshToken(cred)) {
                JWTPrincipal(cred.payload)
            } else {
                null
            }
        }

        // Si tiene error, responde con ese texto
        challenge { defaultScheme, realm ->
            mainlogger.severe("Err! $defaultScheme! $realm")


            call.respond(
                    HttpStatusCode.Unauthorized,
                    Errors(ErrorCodes.ERR_INVALIDTOKEN.ordinal, "Invalid/expired token")
            )
        }
    }
}

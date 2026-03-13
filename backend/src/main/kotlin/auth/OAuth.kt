package com.astrais.auth

import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.util.collections.*

fun Application.initOauth(authenticationConfig: AuthenticationConfig){
    authenticationConfig.oauth("oauth-google") {

    }
}
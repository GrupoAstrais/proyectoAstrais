package com.astrais

import com.astrais.auth.authRoutes
import com.astrais.auth.installAuth
import com.astrais.db.initDatabase
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json

const val APP_PORT = 8759
const val POSTGRES_PORT = 5432

fun main(args: Array<String>) {
    initSampleServer()
}

// Inicio un servidor de forma sencilla
fun initSampleServer(){
    embeddedServer(Netty, port = APP_PORT) {
        module()
    }.start(wait = true)
}

fun Application.module() {
    initDatabase()
    installAuth()

    // Instala la serializacion de JSON
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = false
            
        })
    }

    routing {
        authRoutes()
    }
}
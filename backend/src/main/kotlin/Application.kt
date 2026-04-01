package com.astrais

import com.astrais.auth.authRoutes
import com.astrais.auth.installAuth
import com.astrais.db.initDatabase
import com.astrais.groups.groupRoutes
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json

const val POSTGRES_PORT = "5432"

fun main(args: Array<String>) {
    initSampleServer()
}

// Inicio un servidor de forma sencilla
fun initSampleServer(){
    embeddedServer(Netty, port = System.getenv("ktor.deployment.port").toInt()) {
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
            isLenient = true
            ignoreUnknownKeys = true
        })
    }

    // Instala politica CORS
    install(CORS) {
        anyHost()
        allowCredentials = true
        allowNonSimpleContentTypes = true
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Get)
    }

    routing {
        authRoutes()
        groupRoutes()
        tareaRoutes()
    }
}
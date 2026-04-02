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
import io.ktor.server.plugins.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
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


    install(StatusPages) {
        exception<BadRequestException> { call, _ ->
            call.respond(HttpStatusCode.BadRequest, Errors(ErrorCodes.ERR_MALFORMEDMESSAGE.ordinal, "The data sent by the client was not in the accepted format"))
        }

        exception<NumberFormatException> { call, _ ->
            call.respond(HttpStatusCode.BadRequest, Errors(ErrorCodes.ERR_MALFORMEDMESSAGE.ordinal, "Couldn't parse to int (Likely the UID)"))
        }

        exception<Exception> { call, except ->
            call.respond(HttpStatusCode.InternalServerError, Errors(ErrorCodes.ERR_INTERNALERROR.ordinal, "Unknown exception happened while processing. Message: ${except.message}"))
        }
    }

    routing {
        authRoutes()
        groupRoutes()
        tareaRoutes()
    }
}
package com.astrais

import admin.adminRoutes
import avatar.avatarRoute
import avatar.loadInitialCosmetics
import com.astrais.auth.authRoutes
import com.astrais.auth.installAuth
import com.astrais.auth.oauthRoutes
import com.astrais.db.DatabaseController
import com.astrais.db.TablaUsuario
import com.astrais.db.initDatabase
import com.astrais.groups.groupRoutes
import com.auth0.jwt.exceptions.TokenExpiredException
import installSSE
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.http.content.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import sseRoutes
import storeRoutes
import tasks.tareaRoutes
import java.io.File
import java.util.logging.Logger

const val POSTGRES_PORT = "5432"

fun main(args: Array<String>) {
    initSampleServer()
}

// Inicio un servidor de forma sencilla
fun initSampleServer() {
    embeddedServer(Netty, port = System.getenv("ktor.deployment.port").toInt()) { module() }
            .start(wait = true)
}

val mainlogger = Logger.getLogger("Main")

fun Application.module() {
    initDatabase()
    installAuth()

    // Instala la serializacion de JSON
    install(ContentNegotiation) {
        json(
                Json {
                    prettyPrint = false
                    isLenient = true
                    ignoreUnknownKeys = true
                }
        )
    }

    // Instala politica CORS
    install(CORS) {
        anyHost()

        allowCredentials = true
        allowNonSimpleContentTypes = true

        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)

        anyMethod()
    }


    install(StatusPages) {
        exception<com.auth0.jwt.exceptions.JWTVerificationException> { call, cause ->
            println("JWT FAILED: ${cause.javaClass.simpleName}")
            println("MESSAGE: ${cause.message}")

            call.respond(HttpStatusCode.Unauthorized)
        }

        exception<TokenExpiredException> { call, cause ->
            //mainlogger.info("Token exception? ${cause.message}")
            call.respond(
                HttpStatusCode.Unauthorized,
                Errors(ErrorCodes.ERR_INVALIDTOKEN.ordinal, "Invalid/expired token")
            )
        }

        exception<BadRequestException> { call, except ->
            mainlogger.severe("Bad request exception! Message: ${except.message}")
            call.respond(HttpStatusCode.BadRequest, Errors(ErrorCodes.ERR_MALFORMEDMESSAGE.ordinal, "The data sent by the client was not in the accepted format"))
        }

        exception<NumberFormatException> { call, _ ->
            call.respond(HttpStatusCode.BadRequest, Errors(ErrorCodes.ERR_MALFORMEDMESSAGE.ordinal, "Couldn't parse to int (Likely the UID)"))
        }

        /*exception<Exception> { call, except ->
            val msg = "Unknown exception happened while processing. Message: ${except.message}. Except type: ${except.javaClass.name}"
            mainlogger.severe(msg)
            call.respond(HttpStatusCode.InternalServerError, Errors(ErrorCodes.ERR_INTERNALERROR.ordinal, msg))
        }

        exception<Throwable> { call, cause ->
            cause.printStackTrace()

            call.respond(
                HttpStatusCode.InternalServerError,
                mapOf("errorText" to "Fallo crítico en Backend: ${cause.message}")
            )
        }*/
    }

    installSSE()

    runBlocking {
        if (DatabaseController.isFresh()){
            loadInitialCosmetics()
        }
    }

    routing {
        get("/.well-known/assetlinks.json") {
            call.respondText(
                """[{"relation":["delegate_permission/common.handle_all_urls"],"target":{"namespace":"android_app","package_name":"com.mm.astraisandroid","sha256_cert_fingerprints":["3F:59:F5:0F:81:FF:A5:D3:49:A1:9F:C2:AD:D5:16:FC:82:09:E5:10:F0:B9:6C:1C:7B:10:28:26:A5:9D:6E:38"]}}]""",
                ContentType.Application.Json
            )
        }

        authRoutes()
        adminRoutes()
        avatarRoute()
        oauthRoutes()
        groupRoutes()
        tareaRoutes()
        storeRoutes()
        sseRoutes()

        staticResources("/static", "static") {}
        staticResources("/admin", "admin") {
            default("index.html")
        }

        val externalUploadsDir = System.getenv("UPLOAD_DIR") ?: "uploads"
        staticFiles("/assets", File(externalUploadsDir))
    }
}
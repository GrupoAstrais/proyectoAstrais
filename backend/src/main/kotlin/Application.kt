package com.astrais

import io.ktor.server.application.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureSecurity()
    configureHTTP()
    configureSockets()
    configureSerialization()
    configureDatabases()
    configureRouting()
}

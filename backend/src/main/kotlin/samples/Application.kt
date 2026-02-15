package com.astrais.samples

import io.ktor.server.application.*
import samples.*

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

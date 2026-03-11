package com.astrais

import com.astrais.db.initDatabase
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

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
    initSecurity()
}

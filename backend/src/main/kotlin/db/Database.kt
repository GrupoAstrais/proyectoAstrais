package com.astrais.db

import com.astrais.POSTGRES_PORT
import io.ktor.server.application.*
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

const val POSTGRES_DB = "db"
const val POSTGRES_USER = "root"
const val POSTGRES_PASSWORD = "root"

object DatabaseController {
    private var database : Database? = null

    public fun init(){
        if (isConnected()){
            initConnection()
        }
    }

    public fun close(){
        database?.connector?.invoke()?.close()
        database = null
    }

    public fun isConnected():Boolean{
        return try {
            transaction {
                exec("SELECT 1")
            }
            true
        } catch (e : Exception){
            false
        }
    }

    private fun initConnection(){
        database = Database.connect(
            url = "jdbc:postgresql://localhost:$POSTGRES_PORT/$POSTGRES_DB",
            driver = "org.postgresql.Driver",
            user = POSTGRES_USER,
            password = POSTGRES_PASSWORD
        )
    }
}

fun Application.initDatabase(){
    DatabaseController.init()
}
package com.astrais

import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database

const val POSTGRES_DB = "db"
const val POSTGRES_USER = "root"
const val POSTGRES_PASSWORD = "root"

object DatabaseController {
    private var database : Database? = null

    public fun setDatabase(database: Database){
        this.database = database
    }
    public fun getDatabase() : Database?{
        return database
    }
}

fun Application.initDatabase() {
    val dt = Database.connect(
        url = "jdbc:postgresql://localhost:$POSTGRES_PORT/$POSTGRES_DB",
        driver = "org.postgresql.Driver",
        user = POSTGRES_USER,
        password = POSTGRES_PASSWORD
    )
    DatabaseController.setDatabase(dt)
}
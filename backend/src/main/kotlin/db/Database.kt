package com.astrais.db

import com.astrais.POSTGRES_PORT
import io.ktor.server.application.*
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

private var dbname : String = ""
private var dbuser  : String = ""
private var dbpassword : String = ""

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
        if (database == null){
            return false
        }

        return try {
            // Ejecuta una consulta simple para ver si se hace, sino es que la conexion se fue.
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
            url = "jdbc:postgresql://localhost:$POSTGRES_PORT/$dbname",
            driver = "org.postgresql.Driver",
            user = dbuser,
            password = dbpassword
        )

        // Creacion de tablas si no existen
        transaction {
            SchemaUtils.create(TablaUsuario, TablaCredencialesAuth, inBatch = true)
        }
    }
}

fun Application.initDatabase(){
    dbname = System.getenv("db.dbname")
    dbuser = System.getenv("db.user")
    dbpassword = System.getenv("db.password")
    
    DatabaseController.init()
}
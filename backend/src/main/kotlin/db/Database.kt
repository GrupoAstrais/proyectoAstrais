package com.astrais.db

import com.astrais.POSTGRES_PORT
import com.astrais.auth.hashPassword
import com.astrais.db.TablaCosmetico
import com.astrais.db.TablaInventario
import io.ktor.server.application.*
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

private var dbname : String = ""
private var dbuser  : String = ""
private var dbpassword : String = ""

object DatabaseController {
    private var database : Database? = null

    public fun init(){
        if (!isConnected()){
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
        val url = "jdbc:postgresql://database:$POSTGRES_PORT/$dbname"
        println(" Connecting to PostgreSQL: $url") // DEBUG LOG
        println(" User: $dbuser, DB: $dbname")

        database = Database.connect(
            url = "jdbc:postgresql://database:$POSTGRES_PORT/$dbname",
            driver = "org.postgresql.Driver",
            user = dbuser,
            password = dbpassword
        )

        // Creacion de tablas si no existen
        transaction {
            SchemaUtils.create(
                TablaUsuario,
                TablaCredencialesAuth,
                TablaConfirmacionUsuario,
                TablaGrupo,
                TablaGrupoUsuario,
                TablaTarea,
                TablaTareaUnica,
                TablaTareaObjetivo,
                TablaTareaHabito,
                TablaCosmetico,
                TablaInventario,
                inBatch = true
            )
        }

        // Crea un usuario admin si no hay nada en la tabla
        transaction {
            if (TablaUsuario.selectAll().empty()){
                val a = EntidadUsuario.new {
                    nombre = "Admin"
                    rol = UserRoles.ADMIN_USER

                    // Credenciales de prueba
                    email = "admin@astrais.com"
                    contrasenia = hashPassword("1234")
                    esta_confirmado = 1

                    idioma = "ENG"
                    zona_horaria = 0f
                }
                EntidadGrupo.new {
                    nombre = "Admin's group"
                    descripcion = "Admin group"
                    owner = a.id
                    es_grupo_personal = true
                }
            }
        }
    }
}

fun Application.initDatabase(){
    dbname = environment.config.propertyOrNull("db.dbname")?.getString() ?: "db"
    dbuser = environment.config.propertyOrNull("db.user")?.getString() ?: "root"
    dbpassword = environment.config.propertyOrNull("db.password")?.getString() ?: "root"
	
	print(dbname + " " + dbuser + " " + dbpassword)
	
    DatabaseController.init()
}
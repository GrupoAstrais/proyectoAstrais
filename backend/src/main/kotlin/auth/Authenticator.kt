package com.astrais.auth

import at.favre.lib.crypto.bcrypt.BCrypt
import com.astrais.ErrorCodes
import com.astrais.Errors
import OK_MESSAGE_RESPONSE
import auth.types.*
import com.astrais.db.EntidadCosmetico
import com.astrais.db.getDatabaseDaoImpl
import supportedLanguages
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

fun Application.installAuth() {
    install(Authentication) {
        this@installAuth.initOauth(this)
        this@installAuth.initJWT(this)
    }
}

fun Route.authRoutes() {
    post("/auth/login") {
        // Se hace el login normal y devuelve su JWT
        try {
            val request = call.receive<LoginRequest>()

            // Comprobacion si alguno de los campos esta vacio
            if (request.email.isBlank() || request.passwd.isBlank()) {
                call.respond(
                        HttpStatusCode.BadRequest,
                        Errors(ErrorCodes.ERR_BLANKVALUE.ordinal, "One of the strings is blank")
                )
                return@post
            }

            val isConfirmed = getDatabaseDaoImpl().isUserConfirmed(request.email)
            if (!isConfirmed) {
                call.respond(
                    HttpStatusCode.Forbidden,
                    Errors(ErrorCodes.EER_FORBIDDEN.ordinal, "Please, verify your email before logging in.")
                )
                return@post
            }

            val jwt = getAuthRepoImpl().performBasicLogin(request)
            if (jwt == null) {
                call.respond(
                        HttpStatusCode.BadRequest,
                        Errors(ErrorCodes.ERR_RESOURCEMISSING.ordinal, "The user does not exist")
                )
            } else {
                call.respond(HttpStatusCode.OK, jwt)
            }
        } catch (e: BadRequestException) {
            call.respond(
                    HttpStatusCode.BadRequest,
                    Errors(
                            ErrorCodes.ERR_MALFORMEDMESSAGE.ordinal,
                            "The data sent by the client was not in the accepted format"
                    )
            )
        }
    }

    post("/auth/verify") {
        try {
            val request = call.receive<MailVerifierRequest>()
            if (request.email.isBlank() || request.code.isBlank()) {
                call.respond(HttpStatusCode.BadRequest, Errors(ErrorCodes.ERR_BLANKVALUE.ordinal, "Data missing"))
                return@post
            }

            val success = getDatabaseDaoImpl().verifyConfirmationCode(request.email, request.code)
            if (success) {
                call.respond(HttpStatusCode.OK, OK_MESSAGE_RESPONSE)
            } else {
                call.respond(HttpStatusCode.BadRequest, Errors(ErrorCodes.ERR_BADVALUE.ordinal, "Code incorrect or expired"))
            }
        } catch (e: Exception) {
            call.respond(HttpStatusCode.BadRequest, Errors(ErrorCodes.ERR_MALFORMEDMESSAGE.ordinal, "Format error"))
        }
    }

    post("/auth/register") {
        // Se hace el registro
        try {
            val request = call.receive<RegisterRequest>()

            // Comprobacion si alguno de los campos esta vacio
            if (request.email.isBlank() ||
                            request.name.isBlank() ||
                            request.passwd.isBlank() ||
                            request.lang.isBlank()
            ) {
                call.respond(
                        HttpStatusCode.BadRequest,
                        Errors(ErrorCodes.ERR_BLANKVALUE.ordinal, "One of the strings is blank")
                )
                return@post
            }

            // Comprueba si el lenguaje es correcto
            if (request.lang.length != 3) {
                call.respond(
                        HttpStatusCode.BadRequest,
                        Errors(
                                ErrorCodes.ERR_MALFORMEDMESSAGE.ordinal,
                                "Language is not following ISO 639-2"
                        )
                )
                return@post
            } else if (!supportedLanguages.contains(request.lang)) {
                call.respond(
                        HttpStatusCode.BadRequest,
                        Errors(
                                ErrorCodes.ERR_BADVALUE.ordinal,
                                "Language ${request.lang} is not supported"
                        )
                )
                return@post
            }

            // Registra al usuario
            if (getAuthRepoImpl().performBasicRegister(request)) {
                // Se envia eso ya que no me acuerdo en que frontend, pero descartaba los mensajes
                // sin cuerpo.
                call.respond(HttpStatusCode.OK, OK_MESSAGE_RESPONSE)
            } else {
                call.respond(
                        HttpStatusCode.Conflict,
                        Errors(ErrorCodes.ERR_RESOURCEALREADYEXISTS.ordinal, "User already exists")
                )
            }
        } catch (e: BadRequestException) {
            call.respond(
                    HttpStatusCode.BadRequest,
                    Errors(
                            ErrorCodes.ERR_MALFORMEDMESSAGE.ordinal,
                            "The data sent by the client was not in the accepted format"
                    )
            )
        }
    }

    // Se protege la ruta requiriendo el refresh token para poder entrar
    authenticate("refresh-jwt") {
        post("/auth/regenAccess") {
            // Regenera un AccessToken
            try {
                val token = call.principal<JWTPrincipal>()
                if (token == null) {
                    // No deberia ser null, pero se hace la comprobacion por si acaso
                    call.respond(
                            HttpStatusCode.Unauthorized,
                            Errors(
                                    ErrorCodes.ERR_INVALIDTOKEN.ordinal,
                                    "Invalid/Missing refresh token"
                            )
                    )
                    return@post
                }

                val user = getAuthRepoImpl().regenAccessToken(token.subject?.toInt() ?: 0)
                if (user == null) {
                    call.respond(
                            HttpStatusCode.Unauthorized,
                            Errors(
                                    ErrorCodes.ERR_RESOURCEMISSING.ordinal,
                                    "Error regenerating the access token!"
                            )
                    )
                } else {
                    call.respond(HttpStatusCode.OK, RegenAccessResponse(user))
                }
            } catch (e: BadRequestException) {
                call.respond(
                        HttpStatusCode.BadRequest,
                        Errors(
                                ErrorCodes.ERR_MALFORMEDMESSAGE.ordinal,
                                "The data sent by the client was not in the accepted format"
                        )
                )
            } catch (e: NumberFormatException) {
                call.respond(
                        HttpStatusCode.BadRequest,
                        Errors(
                                ErrorCodes.ERR_MALFORMEDMESSAGE.ordinal,
                                "The subject of the token is invalid"
                        )
                )
            }
        }
    }

    authenticate("access-jwt") {
        delete("/auth/deleteUser") {
            try {
                val token = call.principal<JWTPrincipal>()
                if (token == null) {
                    // No deberia ser null, pero se hace la comprobacion por si acaso
                    call.respond(
                            HttpStatusCode.Unauthorized,
                            Errors(
                                    ErrorCodes.ERR_INVALIDTOKEN.ordinal,
                                    "Invalid/Missing refresh token"
                            )
                    )
                    return@delete
                }

                if (getAuthRepoImpl().deleteUser(token.subject?.toInt() ?: 0)) {
                    call.respond(HttpStatusCode.OK, arrayOf("aknowledged" to true))
                } else {
                    call.respond(
                            HttpStatusCode.BadRequest,
                            Errors(ErrorCodes.ERR_INVALIDTOKEN.ordinal, "Couldn't delete the user")
                    )
                }
            } catch (e: BadRequestException) {
                call.respond(
                        HttpStatusCode.BadRequest,
                        Errors(
                                ErrorCodes.ERR_MALFORMEDMESSAGE.ordinal,
                                "The data sent by the client was not in the accepted format"
                        )
                )
            } catch (e: NumberFormatException) {
                call.respond(
                        HttpStatusCode.BadRequest,
                        Errors(
                                ErrorCodes.ERR_MALFORMEDMESSAGE.ordinal,
                                "The subject of the token is invalid"
                        )
                )
            }
        }
    }

    authenticate("access-jwt") {
        get("/auth/me") {
            val uid =
                call.principal<JWTPrincipal>()!!.subject?.toInt()
                ?: return@get call.respond(HttpStatusCode.Unauthorized, Errors(ErrorCodes.ERR_INVALIDTOKEN.ordinal, "That UID is not valid!"))

            val user =
                getDatabaseDaoImpl().getUsuarioByID(uid)
                ?: return@get call.respond(HttpStatusCode.NotFound)

            val grupos = getDatabaseDaoImpl().getGroupsOfUser(uid)
            val gidPersonal = grupos.firstOrNull { it.es_grupo_personal }?.id?.value
            val mascotaEquipadaAsset = transaction {
                user.id_mascota_equipada?.let { cosmeticoId ->
                    EntidadCosmetico.findById(cosmeticoId)?.assetRef
                }
            }

            call.respond(
                    HttpStatusCode.OK,
                    UserMeResponse(
                            id = user.id.value,
                            nombre = user.nombre,
                            nivel = user.nivel,
                            xpActual = user.xp_actual,
                            xpTotal = user.xp_total,
                            ludiones = user.ludiones,
                            personalGid = gidPersonal,
                            equippedPetRef = mascotaEquipadaAsset,
                            themeColors = user.themeColors
                    )
            )
        }

        patch("/auth/editUser") {
            val uid =
                call.principal<JWTPrincipal>()!!.subject?.toInt()
                ?: return@patch call.respond(HttpStatusCode.Unauthorized, Errors(ErrorCodes.ERR_INVALIDTOKEN.ordinal, "That UID is not valid!"))

            val data = call.receive<EditUserResponse>()
            val resp = getAuthRepoImpl().editUserData(uid, data)

            when (resp) {
                EditUserReturn.OK -> call.respond(HttpStatusCode.OK, OK_MESSAGE_RESPONSE)
                EditUserReturn.ERROR -> call.respond(HttpStatusCode.InternalServerError, Errors(ErrorCodes.ERR_RESOURCENOTMODIFIED.ordinal, "Couldn't edit user for unknown reasons."))
                EditUserReturn.INVALID_LANGUAGE -> call.respond(HttpStatusCode.BadRequest, Errors(ErrorCodes.ERR_BADVALUE.ordinal, "Selected language is not supported"))
            }
        }

        patch("/auth/setEmailLogin") {
            val uid =
                call.principal<JWTPrincipal>()!!.subject?.toInt()
                    ?: return@patch call.respond(HttpStatusCode.Unauthorized, Errors(ErrorCodes.ERR_INVALIDTOKEN.ordinal, "That UID is not valid!"))

            val data = call.receive<SetMailUserRequest>()

            if (getAuthRepoImpl().setUserMailLogin(uid = uid, email = data.email, rawPassword = data.passwd)){
                call.respond(HttpStatusCode.OK, OK_MESSAGE_RESPONSE)
            }else{
                call.respond(HttpStatusCode.Conflict, Errors(ErrorCodes.ERR_RESOURCENOTMODIFIED.ordinal, "Couldn't edit mail login"))
            }
        }
    }
}

fun hashPassword(passwd: String): String {
    return BCrypt.withDefaults().hashToString(8, passwd.toCharArray())
}

fun checkPassword(passwd: String, hash: String): Boolean {
    return BCrypt.verifyer().verify(passwd.toCharArray(), hash).verified
}

@Serializable
data class UserMeResponse(
        val id: Int,
        val nombre: String,
        val nivel: Int,
        val xpActual: Int,
        val xpTotal: Int,
        val ludiones: Int,
        val personalGid: Int?,
        val equippedPetRef: String?,
        val themeColors: String? = null
)

package com.astrais.auth

import OK_MESSAGE_RESPONSE
import auth.types.*
import com.astrais.ErrorCodes
import com.astrais.Errors
import com.astrais.db.AuthProvider
import com.astrais.db.BuyCosmeticResponse
import com.astrais.db.getDatabaseDaoImpl
import com.astrais.mainlogger
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.*
import io.ktor.client.engine.apache.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.cookies.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.receive
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.sessions.*
import kotlinx.serialization.json.Json
import java.net.InetAddress
import java.util.Base64


fun Application.initOauth(authenticationConfig: AuthenticationConfig){
    authenticationConfig.oauth("oauth-google") {
        urlProvider = { "http://localhost:5684/auth/google/callback" }
        providerLookup = {
            OAuthServerSettings.OAuth2ServerSettings(
                name = "google",
                authorizeUrl = "https://accounts.google.com/o/oauth2/auth",
                accessTokenUrl = "https://oauth2.googleapis.com/token",
                requestMethod = HttpMethod.Post,
                clientId = environment.config.property("google.clientId").getString(),
                clientSecret = environment.config.property("google.secretId").getString(),
                defaultScopes = listOf("https://www.googleapis.com/auth/userinfo.profile"),
            )
        }
        client = HttpClient(Apache)
    }
}

fun Route.oauthRoutes() {
    authenticate("oauth-google") {
        get("/auth/google/login") {
            // Nada...
        }

        get("/auth/google/callback"){
            val principal: OAuthAccessTokenResponse.OAuth2? = call.principal()
            if (principal?.accessToken == null){
                call.respond(HttpStatusCode.InternalServerError, Errors(ErrorCodes.ERR_INTERNALERROR.ordinal, "Couldn't get data correctly from google"))
                return@get
            }

            val googleInfo = kotlin.runCatching { getGoogleInfo(principal.accessToken) }.onFailure {
                println(it.message)
                call.respond(HttpStatusCode.InternalServerError, Errors(ErrorCodes.ERR_INTERNALERROR.ordinal, "Unknown google error"))
            }.getOrNull()
            if (googleInfo == null){
                return@get
            }
            val user = getAuthRepoImpl().tryLoginOrRegisterOauth(googleInfo.sub, AuthProvider.GOOGLE)
            if (user.first == -1){
                call.respond(HttpStatusCode.InternalServerError, Errors(ErrorCodes.ERR_INTERNALERROR.ordinal, "Couldn't do oauth"))
            } else {
                val dbUser = getDatabaseDaoImpl().getUsuarioByID(user.first)
                if (dbUser != null) {
                    val loginResponse = OauthLoginResponse(
                        uid = user.first,
                        hadToRegister = user.second,
                        jwtAccessToken = generateAccessToken(dbUser),
                        jwtRefreshToken = generateRefreshToken(dbUser)
                    )
                    getDatabaseDaoImpl().setUserLastLogin(dbUser)

                    println("OAUTH? En esta economia??")

                    //val targetOrigin = call.parameters["frontendOrigin"] ?: "http://localhost:8080"
                    val targetOrigin = if (System.getenv("IS_DEV").equals("0")) "8080" else "5684"

                    /*call.response.header("Cross-Origin-Opener-Policy", "same-origin-allow-popups")
                    call.respondText(
                        text = sendPopup(true,Json.encodeToString(loginResponse), targetOrig = "http://localhost:$targetOrigin"),
                        contentType = ContentType.Text.Html,
                        status = HttpStatusCode.OK
                    )*/
                    call.respondRedirect("http://${InetAddress.getLocalHost().hostAddress}:$targetOrigin/oauthCallback?accessToken=${loginResponse.jwtAccessToken}&refreshToken=${loginResponse.jwtRefreshToken}&hadToRegister=${loginResponse.hadToRegister}")
                } else {
                    call.respond(HttpStatusCode.InternalServerError, Errors(ErrorCodes.ERR_RESOURCEMISSING.ordinal, "Missing user account"))
                }
            }
        }

        get("/auth/testHTML") {
            call.respondText("<h1>Hola mundo!</h1><br><a href=\"/auth/google/login\">Presiona para oauth de google</a>", ContentType.Text.Html)
        }
    }

    post("/auth/google/androidlogin") {
        val req = call.receive<AndroidGoogleLoginRequest>()

        // verificar el token con la librería de google
        val verifier = GoogleIdTokenVerifier.Builder(NetHttpTransport(), GsonFactory.getDefaultInstance())
            .setAudience(listOf(environment.config.property("google.clientId").getString()))
            .build()

        val idToken = verifier.verify(req.idToken)
        if (idToken != null) {
            val payload = idToken.payload
            val userId = payload.subject

            // registrar o loguear al usuario
            val user = getAuthRepoImpl().tryLoginOrRegisterOauth(userId, AuthProvider.GOOGLE)

            if (user.first == -1) {
                call.respond(HttpStatusCode.InternalServerError, Errors(ErrorCodes.ERR_INTERNALERROR.ordinal, "Error creating account"))
            } else {
                // generar jwt
                val dbUser = getDatabaseDaoImpl().getUsuarioByID(user.first)
                if(dbUser != null) {
                    val loginResponse = LoginResponse(
                        jwtAccessToken = generateAccessToken(dbUser),
                        jwtRefreshToken = generateRefreshToken(dbUser)
                    )
                    getDatabaseDaoImpl().setUserLastLogin(dbUser)

                    call.respond(HttpStatusCode.OK, loginResponse)
                } else {
                    call.respond(HttpStatusCode.InternalServerError, Errors(ErrorCodes.ERR_RESOURCEMISSING.ordinal, "Missing user account"))
                }
            }
        } else {
            call.respond(HttpStatusCode.Unauthorized, Errors(ErrorCodes.ERR_INVALIDTOKEN.ordinal, "Invalid Google token"))
        }
    }

    authenticate("access-jwt") {

        // TODO: Esto esta mal, en el setOauth deberia llevar a la misma que /auth/{prov}/login para mas seguridad, y que el callback llame a addOauthToAccount.

        post("/auth/setOauth"){
            val uid =
                call.principal<JWTPrincipal>()!!.subject?.toInt()
                    ?: return@post call.respond(HttpStatusCode.Unauthorized, Errors(ErrorCodes.ERR_INVALIDTOKEN.ordinal, "That UID is not valid!"))

            val data = call.receive<SetOauthResponse>()

            try {
                val out = getDatabaseDaoImpl().addOauthToAccount(
                    uid = uid,
                    provider_uid = data.providerUid,
                    auth = AuthProvider.valueOf(data.authProvider)
                )

                when (out) {
                    BuyCosmeticResponse.OKAY -> call.respond(HttpStatusCode.OK, OK_MESSAGE_RESPONSE)
                    BuyCosmeticResponse.USER_NOT_FOUND -> call.respond(HttpStatusCode.Unauthorized, Errors(ErrorCodes.ERR_FORBIDDEN.ordinal, "That UID is not valid!"))
                    BuyCosmeticResponse.ALREADY_HAS_OBJECT -> call.respond(HttpStatusCode.NotModified, Errors(ErrorCodes.ERR_RESOURCEALREADYEXISTS.ordinal, "Error! There's an oauth of the same provided linked."))
                    else -> call.respond("what")
                }

            } catch (e : IllegalArgumentException) {
                mainlogger.severe("Error! User $uid tried to register auth ${data.authProvider}, and it couldn't be parsed")
                call.respond(HttpStatusCode.BadRequest, Errors(ErrorCodes.ERR_MALFORMEDMESSAGE.ordinal, "${data.authProvider} isn't a permited auth provider."))
            }
        }

        post("/auth/deleteOauth") {
            val uid =
                call.principal<JWTPrincipal>()!!.subject?.toInt()
                    ?: return@post call.respond(HttpStatusCode.Unauthorized, Errors(ErrorCodes.ERR_INVALIDTOKEN.ordinal, "That UID is not valid!"))

            val data = call.receive<DeleteOauthResponse>()

            try {
                val out = getDatabaseDaoImpl().deleteOauthFromAccount(
                    uid = uid,
                    auth = AuthProvider.valueOf(data.authProvider)
                )

                when (out) {
                    BuyCosmeticResponse.OKAY -> call.respond(HttpStatusCode.OK, OK_MESSAGE_RESPONSE)
                    BuyCosmeticResponse.USER_NOT_FOUND -> call.respond(HttpStatusCode.Unauthorized, Errors(ErrorCodes.ERR_FORBIDDEN.ordinal, "That UID is not valid!"))
                    BuyCosmeticResponse.NO_METHOD_REMAIN -> call.respond(HttpStatusCode.NotModified, Errors(ErrorCodes.ERR_RESOURCEMISSING.ordinal, "If the method was deleted, the account would be orphan!"))
                    else -> call.respond("what")
                }

            } catch (e : IllegalArgumentException) {
                mainlogger.severe("Error! User $uid tried to delete auth ${data.authProvider}, and it couldn't be parsed")
                call.respond(HttpStatusCode.BadRequest, Errors(ErrorCodes.ERR_MALFORMEDMESSAGE.ordinal, "${data.authProvider} isn't a permited auth provider."))
            }
        }
    }
}

val jsonOauth = Json { ignoreUnknownKeys = true }

suspend fun getGoogleInfo(accessToken : String) : GoogleUserInfo{
    val client = HttpClient(Apache)
    val req = client.get("https://openidconnect.googleapis.com/v1/userinfo") {
        headers {
            append("Authorization", "Bearer $accessToken")
        }
    }

    //mainlogger.info("Received: ${req.bodyAsText(Charsets.UTF_8)}")

    return jsonOauth.decodeFromString(req.bodyAsText(Charsets.UTF_8)) //req.body<GoogleUserInfo>()
}

suspend fun sendPopup(authDone : Boolean, message : String, targetOrig : String) : String{
    return """
        <!DOCTYPE html>
        <html>
        <body>
            <script>
                // Enviamos los datos a la ventana que nos abrió
                window.opener.postMessage({
                    type: "${if (authDone) "AUTH_SUCCESS" else "AUTH_FAIL"}",
                    payload: $message
                }, "$targetOrig");
                
                console.log("$targetOrig");
                
                //window.close();
            </script>
        </body>
        </html>
    """.trimIndent()
}
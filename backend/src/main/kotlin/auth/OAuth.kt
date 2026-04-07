package com.astrais.auth

import com.astrais.ErrorCodes
import com.astrais.Errors
import com.astrais.OK_MESSAGE_RESPONSE
import com.astrais.db.AuthProvider
import com.astrais.db.getDatabaseDaoImpl
import com.astrais.mainlogger
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.apache.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

@Serializable
data class GoogleUserInfo(
    val sub: String,
    val name: String? = null,
    val given_name: String? = null,
    val family_name: String? = null,
    val picture: String? = null
)

@Serializable
data class OauthLoginResponse(
    val uid : Int,
    val hadToRegister : Boolean
)

fun Application.initOauth(authenticationConfig: AuthenticationConfig){
    authenticationConfig.oauth("oauth-google") {
        urlProvider = { "http://localhost:5684/auth/googlecallback" }
        providerLookup = {
            OAuthServerSettings.OAuth2ServerSettings(
                name = "google",
                authorizeUrl = "https://accounts.google.com/o/oauth2/auth",
                accessTokenUrl = "https://oauth2.googleapis.com/token",
                requestMethod = HttpMethod.Post,
                clientId = environment.config.property("google.clientId").getString(),
                clientSecret = environment.config.property("google.secretId").getString(),
                defaultScopes = listOf("https://www.googleapis.com/auth/userinfo.profile")
            )
        }
        client = HttpClient(Apache)
    }
}

fun Route.oauthRoutes() {
    authenticate("oauth-google") {
        get("/auth/googlelogin"){
            // Se supone que se redirige a authorizeUrl
        }
        get("/auth/googlecallback"){
            val principal: OAuthAccessTokenResponse.OAuth2? = call.principal()
            if (principal?.accessToken == null){
                call.respond(HttpStatusCode.InternalServerError, Errors(ErrorCodes.ERR_INTERNALERROR.ordinal, "Couldn't get data correctly from google"))
                return@get
            }

            val googleInfo = getGoogleInfo(principal.accessToken)
            val user = getAuthRepoImpl().tryLoginOrRegisterOauth(googleInfo.sub, AuthProvider.GOOGLE)
            if (user.first == -1){
                call.respond(HttpStatusCode.InternalServerError, Errors(ErrorCodes.ERR_INTERNALERROR.ordinal, "Couldn't do oauth"))
            } else {
                call.respond(HttpStatusCode.OK, OauthLoginResponse(uid = user.first, hadToRegister = user.second))
            }
        }

        // TODO: Que android haga su oauth y le mande los datos al servidor
        post("/auth/google/android") {

        }

        get("/auth/testHTML") {
            call.respondText("<h1>Hola mundo!</h1><br><a href=\"/auth/googlelogin\">Presiona para oauth de google</a>", ContentType.Text.Html)
        }
    }
}

suspend fun getGoogleInfo(accessToken : String) : GoogleUserInfo{
    val client = HttpClient(Apache)
    val req = client.get("https://openidconnect.googleapis.com/v1/userinfo") {
        headers {
            append("Authorization", "Bearer $accessToken")
        }
    }

    //mainlogger.info("Received: ${req.bodyAsText(Charsets.UTF_8)}")

    return req.body()
}
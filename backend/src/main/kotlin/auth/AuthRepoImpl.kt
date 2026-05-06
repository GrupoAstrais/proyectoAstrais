package com.astrais.auth

import OK_MESSAGE_RESPONSE
import UserBusSSE
import auth.types.*
import com.astrais.ErrorCodes
import com.astrais.Errors
import com.astrais.db.AuthProvider
import com.astrais.db.DatabaseDAO
import com.astrais.db.getDatabaseDaoImpl
import io.ktor.http.*
import io.ktor.server.response.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.mail.DefaultAuthenticator
import org.apache.commons.mail.HtmlEmail
import org.jetbrains.exposed.v1.exceptions.ExposedSQLException
import org.slf4j.LoggerFactory
import supportedLanguages

private val log = LoggerFactory.getLogger(AuthRepoImpl::class.java)

class AuthRepoImpl : AuthRepo {
    override suspend fun performBasicLogin(loginRequest: LoginRequest) : LoginResponse?{
        try {
            val user = getDatabaseDaoImpl().getUsuario(loginRequest.email)

            if (user?.contrasenia != null && user.esta_confirmado != 0 && checkPassword(loginRequest.passwd, user.contrasenia!!)){
                getDatabaseDaoImpl().setUserLastLogin(user)
                log.info("User ${user.nombre} (${user.id.value}) connected.")

                // Genera JWT
                val i = LoginResponse(jwtAccessToken = generateAccessToken(user), jwtRefreshToken = generateRefreshToken(user))
                log.info("Created tokens for ${user.nombre} (${user.id.value})")

                return i
            }
        } catch (e : ExposedSQLException){
            log.error("Error trying to log user ${loginRequest.email}! Message: ${e.message}")
        }
        return null
    }

    override suspend fun performBasicRegister(registerRequest: RegisterRequest): Boolean {
        try {
            val dao: DatabaseDAO = getDatabaseDaoImpl()
            val user = dao.getUsuario(registerRequest.email)

            if (user == null) {
                // Usuario nuevo
                val hashContrasenia = hashPassword(registerRequest.passwd)
                val uid = dao.createUser(registerRequest.name, registerRequest.email, hashContrasenia, registerRequest.lang)

                dao.createGroup(uid, registerRequest.name, "", true)

                val code = (100000..999999).random().toString()
                dao.saveConfirmationCode(uid, code)
                sendEmail(registerRequest.email, code)

                log.info("User ${registerRequest.name} ($uid) registered. Code sent.")
                return true

            } else if (user.esta_confirmado == 0) {
                // El usuario existe pero no está confirmado
                log.info("User ${registerRequest.email} is not confirmed. Resending new code.")

                // dao.updateUserPassword(user.id.value, hashPassword(registerRequest.passwd))

                val code = (100000..999999).random().toString()
                dao.saveConfirmationCode(user.id.value, code)
                sendEmail(registerRequest.email, code)

                return true

            } else {
                log.warn("Attempt to register already registered user: ${registerRequest.email}")
                return false
            }
        } catch (e: ExposedSQLException) {
            log.error("Error trying to register... Message: ${e.message}")
        }
        return false
    }

    private suspend fun sendEmail(toAddress: String, code: String) {
        withContext(Dispatchers.IO) {
            val mailUser1 = System.getenv("SMTP_EMAIL") ?: ""
            val mailPass1 = System.getenv("SMTP_PASSWORD") ?: ""

            val htmlStream = this@AuthRepoImpl::class.java.getResourceAsStream("/templates/verification.html")
            val htmlText = htmlStream?.bufferedReader()?.use { it.readText() }
                ?.replace("{{CODE}}", code) ?: "<h1>Codigo: ${code} </h1>"

            if (!sendMailWith(
                    mailUser = mailUser1,
                    mailPass = mailPass1,
                    toAddress = toAddress,
                    htmlText = htmlText,
                    textContent = code
                )) {
                val mailUser2 = System.getenv("SMTP_EMAIL_2") ?: ""
                val mailPass2 = System.getenv("SMTP_PASSWORD_2") ?: ""

                sendMailWith(mailUser = mailUser2,
                    mailPass = mailPass2,
                    toAddress = toAddress,
                    htmlText = htmlText,
                    textContent = code
                )
            }
        }
    }

    override suspend fun deleteUser(uid: Int): Boolean {
        try {
            // TODO: Al implementar SSE, se debera enviar un mensaje de cerrar sesion al borrarlo.
            return getDatabaseDaoImpl().deleteUsuario(uid)
        }catch (e : ExposedSQLException){
            log.error("Error while trying to delete the user with ID $uid")
        }
        return false
    }

    override suspend fun regenAccessToken(id: Int): String? {
        try {
            val user = getDatabaseDaoImpl().getUsuarioByID(id)
            if (user != null){
                return generateAccessToken(user)
            }
        } catch (e : ExposedSQLException){
            log.error("Error trying regen access token for $id! Message: ${e.message}")
        }
        return null
    }

    override suspend fun tryLoginOrRegisterOauth(provider_uid : String, auth : AuthProvider) : Pair<Int, Boolean> {
        try {
            val out = getDatabaseDaoImpl().logOrCreateOauthUser(provider_uid = provider_uid, auth = auth)
            return out
        }
        catch (e : ExposedSQLException){
            log.error("Couldn't create account for $provider_uid (${auth.name})! Message: ${e.message}")
            return Pair(-1, false)
        }
    }

    override suspend fun editUserData(uid: Int, data: EditUserResponse) : EditUserReturn {
        if (data.lang != null && !supportedLanguages.contains(data.lang)){
            return EditUserReturn.INVALID_LANGUAGE
        }

        val res = getDatabaseDaoImpl().editUser(
            uid = uid,
            nombreusu = data.nombreusu,
            lang = data.lang,
            utcOffset = data.utcOffset
        )

        if (res){
            return EditUserReturn.OK
        }else{
            return EditUserReturn.INVALID_LANGUAGE
        }
    }

    override suspend fun setUserMailLogin(uid: Int, email: String?, rawPassword: String?) : Boolean {
        val passwd = if (rawPassword.isNullOrEmpty()){
            null
        } else {
            hashPassword(rawPassword)
        }

        val resp = getDatabaseDaoImpl().setupUserEmail(
            uid = uid,
            newEmail = email,
            newPassword = passwd
        )

        if (resp) {
            if (email != null) {
                val code = (100000..999999).random().toString()
                getDatabaseDaoImpl().saveConfirmationCode(uid, code)
                sendEmail(email, code)
            }

            // Pide que te desloguees
            UserBusSSE.publishSignOff(uid)
        }
        return resp
    }
}


fun sendMailWith(mailUser : String, mailPass : String, toAddress : String, htmlText : String, textContent : String) : Boolean{
    try {

        val email = HtmlEmail()
        email.hostName = "smtp.gmail.com"
        email.setAuthenticator(DefaultAuthenticator(mailUser, mailPass))
        email.isSSLOnConnect = true

        email.setFrom(mailUser, "Registro Astrais")
        email.subject = "Código de verificación"

        if (htmlText != null) {
            email.setHtmlMsg(htmlText)
            email.setTextMsg(textContent)
        } else {
            email.setMsg(textContent)
        }

        email.addTo(toAddress)
        email.send()

        return true
    } catch (e: Exception) {
        log.error("Error al enviar correo: ${e.message}")
        return false
    }
}
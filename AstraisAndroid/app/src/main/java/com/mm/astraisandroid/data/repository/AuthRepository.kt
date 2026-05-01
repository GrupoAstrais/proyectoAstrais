package com.mm.astraisandroid.data.repository


import com.mm.astraisandroid.data.api.LoginRequest
import com.mm.astraisandroid.data.api.MailVerifierRequest
import com.mm.astraisandroid.data.api.RegisterRequest
import com.mm.astraisandroid.data.api.services.AuthApi
import com.mm.astraisandroid.data.api.services.UserApi
import com.mm.astraisandroid.data.preferences.SessionManager
import com.mm.astraisandroid.util.logging.AppLogger
import com.mm.astraisandroid.util.logging.LogFeature
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val api: AuthApi,
    private val userApi: UserApi,
    private val sessionManager: SessionManager,
    private val sessionOrchestrator: SessionOrchestrator,
    private val logger: AppLogger
) {
    private fun needsOnboardingForName(name: String): Boolean {
        return name.contains("@") || name == "Viajero" || name == "NUEVO_USUARIO"
    }

    suspend fun needsOnboarding(): Boolean {
        val me = userApi.getMe()
        return needsOnboardingForName(me.name)
    }

    suspend fun logout() {
        sessionOrchestrator.logout()
    }

    suspend fun register(request: RegisterRequest) {
        api.performRegister(request)
    }

    suspend fun verifyEmail(email: String, code: String, pass: String): Boolean {
        api.verifyEmail(MailVerifierRequest(email, code))
        return login(LoginRequest(email, pass))
    }

    suspend fun login(request: LoginRequest): Boolean {
        val wasGuest = sessionManager.isGuest()

        val response = api.performLogin(request)
        sessionManager.saveTokens(response.jwtAccessToken, response.jwtRefreshToken)

        val me = userApi.getMe()
        val needsOnboarding = needsOnboardingForName(me.name)
        logger.d(LogFeature.AUTH, "Login success: name=${me.name}, needsOnboarding=$needsOnboarding")

        me.personalGid?.let { gid ->
            sessionManager.savePersonalGid(gid)
            sessionOrchestrator.onLoginSuccess(wasGuest, gid)
        }
        return needsOnboarding
    }


    suspend fun loginWithGoogle(idToken: String): Boolean {
        val wasGuest = sessionManager.isGuest()

        val response = api.performGoogleLogin(idToken)
        sessionManager.saveTokens(response.jwtAccessToken, response.jwtRefreshToken)

        val me = userApi.getMe()
        val needsOnboarding = needsOnboardingForName(me.name)

        me.personalGid?.let { gid ->
            sessionManager.savePersonalGid(gid)
            sessionOrchestrator.onLoginSuccess(wasGuest, gid)
        }
        return needsOnboarding
    }
}

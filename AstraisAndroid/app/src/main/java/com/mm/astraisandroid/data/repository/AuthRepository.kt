package com.mm.astraisandroid.data.repository


import com.mm.astraisandroid.data.api.LoginRequest
import com.mm.astraisandroid.data.api.MailVerifierRequest
import com.mm.astraisandroid.data.api.RegisterRequest
import com.mm.astraisandroid.data.api.services.AuthApi
import com.mm.astraisandroid.data.api.services.UserApi
import com.mm.astraisandroid.data.preferences.SessionManager
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val api: AuthApi,
    private val userApi: UserApi,
) {
    suspend fun register(request: RegisterRequest) {
        api.performRegister(request)
    }

    suspend fun verifyEmail(email: String, code: String) {
        api.verifyEmail(MailVerifierRequest(email, code))
    }

    suspend fun login(request: LoginRequest) {
        val response = api.performLogin(request)
        SessionManager.saveTokens(response.jwtAccessToken, response.jwtRefreshToken)
        val me = userApi.getMe()
        me.personalGid?.let { SessionManager.savePersonalGid(it) }
    }

    suspend fun loginWithGoogle(idToken: String) {
        val response = api.performGoogleLogin(idToken)
        SessionManager.saveTokens(response.jwtAccessToken, response.jwtRefreshToken)
        val me = userApi.getMe()
        me.personalGid?.let { SessionManager.savePersonalGid(it) }
    }
}
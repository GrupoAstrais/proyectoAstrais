package com.mm.astraisandroid.data.repository


import android.content.Context
import com.mm.astraisandroid.data.api.LoginRequest
import com.mm.astraisandroid.data.api.MailVerifierRequest
import com.mm.astraisandroid.data.api.RegisterRequest
import com.mm.astraisandroid.data.api.services.AuthApi
import com.mm.astraisandroid.data.api.services.UserApi
import com.mm.astraisandroid.data.preferences.SessionManager
import com.mm.astraisandroid.sync.scheduleSync
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val api: AuthApi,
    private val userApi: UserApi,
    private val taskRepository: TaskRepository,
    private val groupRepository: GroupRepository,
    @ApplicationContext private val context: Context
) {
    private fun needsOnboardingForName(name: String): Boolean {
        return name.contains("@") || name == "Viajero" || name == "NUEVO_USUARIO"
    }

    suspend fun needsOnboarding(): Boolean {
        val me = userApi.getMe()
        return needsOnboardingForName(me.name)
    }

    suspend fun logout() {
        SessionManager.clear()
        taskRepository.clearLocalData()
        groupRepository.clearLocalData()
    }

    suspend fun register(request: RegisterRequest) {
        api.performRegister(request)
    }

    suspend fun verifyEmail(email: String, code: String, pass: String): Boolean {
        api.verifyEmail(MailVerifierRequest(email, code))
        return login(LoginRequest(email, pass))
    }

    suspend fun login(request: LoginRequest): Boolean {
        val wasGuest = SessionManager.isGuest()

        val response = api.performLogin(request)
        SessionManager.saveTokens(response.jwtAccessToken, response.jwtRefreshToken)

        val me = userApi.getMe()
        val needsOnboarding = needsOnboardingForName(me.name)
        android.util.Log.d("AUTH_DEBUG", "User name: ${me.name}, Needs onboarding: $needsOnboarding")

        me.personalGid?.let { gid ->
            SessionManager.savePersonalGid(gid)

            if (wasGuest) {
                taskRepository.migrateGuestTasksToServer(gid)
            }

            scheduleSync(context)
        }
        return needsOnboarding
    }


    suspend fun loginWithGoogle(idToken: String): Boolean {
        val wasGuest = SessionManager.isGuest()

        val response = api.performGoogleLogin(idToken)
        SessionManager.saveTokens(response.jwtAccessToken, response.jwtRefreshToken)

        val me = userApi.getMe()
        val needsOnboarding = needsOnboardingForName(me.name)

        me.personalGid?.let { gid ->
            SessionManager.savePersonalGid(gid)

            if (wasGuest) {
                taskRepository.migrateGuestTasksToServer(gid)
            }

            scheduleSync(context)
        }
        return needsOnboarding
    }
}
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

/**
 * Repositorio encargado de la gestión de autenticación del usuario.
 *
 * Coordina las llamadas a la API de autenticación ([AuthApi]), la obtención del perfil
 * ([UserApi]) y la gestión de sesión ([SessionManager]). Determina si un usuario necesita
 * completar el onboarding basándose en su nombre.
 *
 * @property api Servicio HTTP para login, registro y verificación de email.
 * @property userApi Servicio HTTP para obtener el perfil del usuario.
 * @property sessionManager Gestor de sesión para almacenar y consultar tokens.
 * @property sessionOrchestrador Coordinador de operaciones post-login y logout.
 * @property logger Sistema de logging estructurado.
 */
class AuthRepository @Inject constructor(
    private val api: AuthApi,
    private val userApi: UserApi,
    private val sessionManager: SessionManager,
    private val sessionOrchestrator: SessionOrchestrator,
    private val logger: AppLogger
) {
    /**
     * Determina si el nombre de usuario indica que necesita completar el onboarding.
     *
     * @param name Nombre del usuario a evaluar.
     * @return `true` si el nombre contiene `@`, es `Viajero` o `NUEVO_USUARIO`.
     */
    private fun needsOnboardingForName(name: String): Boolean {
        return name.contains("@") || name == "Viajero" || name == "NUEVO_USUARIO"
    }

    /**
     * Verifica si el usuario autenticado necesita completar el onboarding.
     * Consulta el perfil del servidor y evalúa el nombre.
     *
     * @return `true` si el usuario necesita onboarding.
     * @throws Exception Si la petición de red falla.
     */
    suspend fun needsOnboarding(): Boolean {
        val me = userApi.getMe()
        return needsOnboardingForName(me.name)
    }

    /**
     * Cierra la sesión del usuario actual, limpiando tokens, datos locales y programación de sync.
     */
    suspend fun logout() {
        sessionOrchestrator.logout()
    }

    /**
     * Registra un nuevo usuario en el sistema.
     *
     * @param request Datos de registro (email, contraseña, nombre, idioma).
     * @throws Exception Si la petición de red falla.
     */
    suspend fun register(request: RegisterRequest) {
        api.performRegister(request)
    }

    /**
     * Verifica el código de email y, si es válido, inicia sesión automáticamente.
     *
     * @param email Dirección de correo electrónico del usuario.
     * @param code Código de verificación recibido por email.
     * @param pass Contraseña del usuario para iniciar sesión tras la verificación.
     * @return `true` si el login fue exitoso y necesita onboarding.
     * @throws Exception Si la verificación o el login fallan.
     */
    suspend fun verifyEmail(email: String, code: String, pass: String): Boolean {
        api.verifyEmail(MailVerifierRequest(email, code))
        return login(LoginRequest(email, pass))
    }

    /**
     * Inicia sesión con credenciales de email y contraseña.
     * Almacena los tokens, obtiene el perfil y gestiona la migración si era sesión invitada.
     *
     * @param request Datos de acceso (email y contraseña).
     * @return `true` si el usuario necesita completar onboarding tras el login.
     * @throws Exception Si la petición de red falla.
     */
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


    /**
     * Inicia sesión mediante autenticación de Google (OAuth).
     * Almacena los tokens, obtiene el perfil y gestiona la migración si era sesión invitada.
     *
     * @param idToken Token de identidad proporcionado por Google Sign-In.
     * @return `true` si el usuario necesita completar onboarding tras el login.
     * @throws Exception Si la petición de red falla.
     */
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

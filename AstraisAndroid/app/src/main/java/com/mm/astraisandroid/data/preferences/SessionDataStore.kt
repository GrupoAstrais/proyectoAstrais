package com.mm.astraisandroid.data.preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object SessionManager {
    private var sharedPreferences: SharedPreferences? = null

    private const val ACCESS_KEY = "access_token"
    private const val REFRESH_KEY = "refresh_token"
    private const val GID_KEY = "personal_gid"

    private val _isSessionActive = MutableStateFlow(true)
    val isSessionActive: StateFlow<Boolean> = _isSessionActive.asStateFlow()

    private val _isGuestSession = MutableStateFlow(false)
    val isGuestSession: StateFlow<Boolean> = _isGuestSession.asStateFlow()

    fun init(context: Context) {
        if (sharedPreferences != null) return

        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        sharedPreferences = EncryptedSharedPreferences.create(
            context.applicationContext,
            "secure_session_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        _isGuestSession.value = sharedPreferences?.getBoolean("is_guest", false) ?: false
        _isSessionActive.value = hasAnySession()
    }

    fun saveTokens(access: String, refresh: String) {
        sharedPreferences?.edit()?.apply {
            putString(ACCESS_KEY, access)
            putString(REFRESH_KEY, refresh)
            putBoolean("is_guest", false)
            apply()
        }

        _isGuestSession.value = false
        _isSessionActive.value = true
    }

    fun startGuestSession() {
        sharedPreferences?.edit()?.putBoolean("is_guest", true)?.apply()
        _isGuestSession.value = true
        _isSessionActive.value = true
    }

    fun savePersonalGid(gid: Int) {
        sharedPreferences?.edit()?.putInt(GID_KEY, gid)?.apply()
    }

    fun getAccessToken(): String? = sharedPreferences?.getString(ACCESS_KEY, null)
    fun getRefreshToken(): String? = sharedPreferences?.getString(REFRESH_KEY, null)

    fun getPersonalGid(): Int? {
        val gid = sharedPreferences?.getInt(GID_KEY, -1)
        return if (gid != -1) gid else null
    }

    fun hasSession(): Boolean = getAccessToken() != null
    fun isGuest(): Boolean = _isGuestSession.value
    fun hasAnySession(): Boolean = hasSession() || isGuest()

    fun clear() {
        sharedPreferences?.edit()?.clear()?.apply()
        _isGuestSession.value = false
        _isSessionActive.value = false
    }
}
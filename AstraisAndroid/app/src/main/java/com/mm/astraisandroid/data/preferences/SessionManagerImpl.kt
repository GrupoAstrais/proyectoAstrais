package com.mm.astraisandroid.data.preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManagerImpl @Inject constructor(
    @ApplicationContext context: Context
) : SessionManager {

    private val sharedPreferences: SharedPreferences

    companion object {
        private const val ACCESS_KEY = "access_token"
        private const val REFRESH_KEY = "refresh_token"
        private const val GID_KEY = "personal_gid"
        private const val PENDING_DEEP_LINK = "pending_deep_link"
    }

    private val _isSessionActive = MutableStateFlow(true)
    override val isSessionActive: StateFlow<Boolean> = _isSessionActive.asStateFlow()

    private val _isGuestSession = MutableStateFlow(false)
    override val isGuestSession: StateFlow<Boolean> = _isGuestSession.asStateFlow()

    init {
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

        _isGuestSession.value = sharedPreferences.getBoolean("is_guest", false)
        _isSessionActive.value = hasAnySession()
    }

    override fun getEffectiveGid(): Int = getPersonalGid() ?: SessionManager.GUEST_GID

    override fun saveTokens(access: String, refresh: String) {
        sharedPreferences.edit()?.apply {
            putString(ACCESS_KEY, access)
            putString(REFRESH_KEY, refresh)
            putBoolean("is_guest", false)
            apply()
        }

        _isGuestSession.value = false
        _isSessionActive.value = true
    }

    override fun startGuestSession() {
        sharedPreferences.edit()?.putBoolean("is_guest", true)?.apply()
        _isGuestSession.value = true
        _isSessionActive.value = true
    }

    override fun savePersonalGid(gid: Int) {
        sharedPreferences.edit()?.putInt(GID_KEY, gid)?.apply()
    }

    override fun getAccessToken(): String? = sharedPreferences.getString(ACCESS_KEY, null)
    override fun getRefreshToken(): String? = sharedPreferences.getString(REFRESH_KEY, null)

    override fun getPersonalGid(): Int? {
        val gid = sharedPreferences.getInt(GID_KEY, -1)
        return if (gid != -1) gid else null
    }

    override fun savePendingDeepLink(url: String) {
        sharedPreferences.edit()?.putString(PENDING_DEEP_LINK, url)?.apply()
    }

    override fun consumePendingDeepLink(): String? {
        val value = sharedPreferences.getString(PENDING_DEEP_LINK, null)
        if (value != null) {
            sharedPreferences.edit()?.remove(PENDING_DEEP_LINK)?.apply()
        }
        return value
    }

    override fun hasSession(): Boolean = getAccessToken() != null
    override fun isGuest(): Boolean = _isGuestSession.value
    override fun hasAnySession(): Boolean = hasSession() || isGuest()

    override fun clear() {
        sharedPreferences.edit()?.clear()?.apply()
        _isGuestSession.value = false
        _isSessionActive.value = false
    }

    override fun promoteGuestToUser(access: String, refresh: String) {
        sharedPreferences.edit()?.apply {
            putString(ACCESS_KEY, access)
            putString(REFRESH_KEY, refresh)
            putBoolean("is_guest", false)
            apply()
        }

        _isGuestSession.value = false
        _isSessionActive.value = true
    }
}

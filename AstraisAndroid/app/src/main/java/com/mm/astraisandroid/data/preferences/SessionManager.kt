package com.mm.astraisandroid.data.preferences

import kotlinx.coroutines.flow.StateFlow

interface SessionManager {
    val isSessionActive: StateFlow<Boolean>
    val isGuestSession: StateFlow<Boolean>

    fun getAccessToken(): String?
    fun getRefreshToken(): String?
    fun getPersonalGid(): Int?
    fun getEffectiveGid(): Int

    fun saveTokens(access: String, refresh: String)
    fun startGuestSession()
    fun savePersonalGid(gid: Int)
    fun promoteGuestToUser(access: String, refresh: String)

    fun savePendingDeepLink(url: String)
    fun consumePendingDeepLink(): String?

    fun hasSession(): Boolean
    fun isGuest(): Boolean
    fun hasAnySession(): Boolean

    fun clear()

    companion object {
        const val GUEST_GID: Int = -1
    }
}

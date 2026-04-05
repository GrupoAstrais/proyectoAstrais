package com.mm.astraisandroid.data.preferences

object TokenHolder {
    private var accessToken:  String? = null
    private var refreshToken: String? = null
    private var personalGid:  Int?    = null

    var sessionDataStore: SessionDataStore? = null

    fun setAccessToken(token: String)  { accessToken = token }
    fun setRefreshToken(token: String) { refreshToken = token }
    fun setPersonalGid(gid: Int?)      { personalGid = gid }
    fun getAccessToken()  = accessToken
    fun getRefreshToken() = refreshToken
    fun getPersonalGid()  = personalGid

    suspend fun saveTokensToDisk(access: String, refresh: String) {
        sessionDataStore?.saveTokens(access, refresh)
    }

    fun clear() {
        accessToken = null
        refreshToken = null
        personalGid = null
    }
}
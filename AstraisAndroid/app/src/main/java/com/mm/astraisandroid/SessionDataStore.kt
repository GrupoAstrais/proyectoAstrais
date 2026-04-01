package com.mm.astraisandroid

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

val Context.dataStore by preferencesDataStore(name = "session")


class SessionDataStore(private val context: Context) {

    private val ACCESS_KEY  = stringPreferencesKey("access_token")
    private val REFRESH_KEY = stringPreferencesKey("refresh_token")
    private val GID_KEY     = intPreferencesKey("personal_gid")

    suspend fun saveTokens(access: String, refresh: String) {
        context.dataStore.edit {
            it[ACCESS_KEY]  = access
            it[REFRESH_KEY] = refresh
        }
    }

    suspend fun savePersonalGid(gid: Int) {
        context.dataStore.edit {
            it[GID_KEY] = gid
        }
    }

    suspend fun loadTokens(): Pair<String?, String?> {
        val prefs = context.dataStore.data.first()
        return Pair(prefs[ACCESS_KEY], prefs[REFRESH_KEY])
    }

    suspend fun loadSession(): Triple<String?, String?, Int?> {
        val prefs = context.dataStore.data.first()
        return Triple(prefs[ACCESS_KEY], prefs[REFRESH_KEY], prefs[GID_KEY])
    }

    suspend fun clear() {
        context.dataStore.edit { it.clear() }
    }
}
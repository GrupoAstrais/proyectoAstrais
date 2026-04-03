package com.mm.astraisandroid

import android.content.Context
import android.content.SharedPreferences
import com.mm.astraisandroid.data.api.TareaResponse
import com.mm.astraisandroid.data.api.UserMeResponse
import kotlinx.serialization.json.Json
// Esto hay que borrarlo :((( ha siod boniti mientras duro
object AppCache {
    private const val PREFS_NAME = "astrais_offline_cache"
    private var prefs: SharedPreferences? = null

    fun init(context: Context) {
        if (prefs == null) {
            prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }
    }

    fun saveUser(user: UserMeResponse) {
        prefs?.edit()?.putString("cached_user", Json.encodeToString(user))?.apply()
    }

    fun getUser(): UserMeResponse? {
        val jsonString = prefs?.getString("cached_user", null) ?: return null
        return try { Json.decodeFromString<UserMeResponse>(jsonString) } catch (e: Exception) { null }
    }

    fun saveTasks(tasks: List<TareaResponse>) {
        prefs?.edit()?.putString("cached_tasks", Json.encodeToString(tasks))?.apply()
    }

    fun getTasks(): List<TareaResponse>? {
        val jsonString = prefs?.getString("cached_tasks", null) ?: return null
        return try { Json.decodeFromString<List<TareaResponse>>(jsonString) } catch (e: Exception) { null }
    }

    fun clear() {
        prefs?.edit()?.clear()?.apply()
    }
}
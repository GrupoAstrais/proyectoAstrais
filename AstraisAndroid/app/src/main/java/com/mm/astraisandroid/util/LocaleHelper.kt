package com.mm.astraisandroid.util

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

/**
 * Helper para gestionar el idioma de la aplicación de forma persistente.
 * Guarda el código de idioma en SharedPreferences y aplica el Locale
 * al contexto de la aplicación.
 */
object LocaleHelper {
    private const val PREFS_NAME = "astrais_locale_prefs"
    private const val KEY_LANGUAGE = "app_language"
    private const val DEFAULT_LANGUAGE = "ESP"

    fun getLanguage(context: Context): String {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_LANGUAGE, DEFAULT_LANGUAGE) ?: DEFAULT_LANGUAGE
    }

    fun setLanguage(context: Context, language: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_LANGUAGE, language)
            .apply()
    }

    fun applyLocale(context: Context): Context {
        val language = getLanguage(context)
        val localeCode = when (language) {
            "ENG" -> "en"
            else -> "es"
        }
        val locale = Locale(localeCode)
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }
}

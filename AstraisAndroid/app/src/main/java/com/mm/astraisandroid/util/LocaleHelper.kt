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

    /**
     * Obtiene el código de idioma actual almacenado en las preferencias.
     *
     * @param context Contexto para acceder a SharedPreferences.
     * @return Código de idioma (p. ej. "ESP", "ENG") o el valor por defecto si no hay preferencia guardada.
     */
    fun getLanguage(context: Context): String {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_LANGUAGE, DEFAULT_LANGUAGE) ?: DEFAULT_LANGUAGE
    }

    /**
     * Guarda el código de idioma en las preferencias para persistencia.
     *
     * @param context Contexto para acceder a SharedPreferences.
     * @param language Código de idioma a guardar (p. ej. "ESP", "ENG").
     */
    fun setLanguage(context: Context, language: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_LANGUAGE, language)
            .apply()
    }

    /**
     * Aplica el Locale guardado al contexto de la aplicación.
     * Convierte el código de idioma interno ("ESP"/"ENG") al código de locale estándar ("es"/"en")
     * y crea un nuevo contexto con esa configuración.
     *
     * @param context Contexto base al que aplicar el Locale.
     * @return Nuevo contexto con el Locale configurado.
     */
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

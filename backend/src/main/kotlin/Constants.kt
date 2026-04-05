package com.astrais

// Constantes globales

const val LANG_CODE_SPANISH = "ESP"
const val LANG_CODE_ENGLISH = "ENG"
const val LANG_CODE_RUSSIAN = "RUS"

val supportedLanguages = listOf(LANG_CODE_ENGLISH, LANG_CODE_SPANISH, LANG_CODE_RUSSIAN)

/** Usuario normal */
const val ROLE_USERNORMAL   = 0
/** Moderador */
const val ROLE_USERMOD      = 1
/** Dueño del grupo */
const val ROLE_USEROWNER    = 2

/** Mensaje de Ok a las rutas que no devuelve nada */
val OK_MESSAGE_RESPONSE = mapOf("aknowledged" to true)
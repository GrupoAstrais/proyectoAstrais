package com.astrais

// Constantes globales

const val LANG_CODE_SPANISH = "ESP"
const val LANG_CODE_ENGLISH = "ENG"
const val LANG_CODE_RUSSIAN = "RUS"

val supportedLanguages = listOf(LANG_CODE_ENGLISH, LANG_CODE_SPANISH, LANG_CODE_RUSSIAN)


const val ROLE_USERNORMAL   = 0
const val ROLE_USERMOD      = 1
const val ROLE_USEROWNER    = 2


val OK_MESSAGE_RESPONSE = mapOf("aknowledged" to true)
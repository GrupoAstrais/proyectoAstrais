package com.mm.astraisandroid.data.api

import kotlinx.serialization.Serializable

@Serializable
data class EditUserRequest(
    val uid: Int,
    val nombreusu: String,
    val lang: String?,
    val utcOffset: Float?
)

package com.urmyfood.shared.domain.model

data class AuthToken(
    val accessToken: String,
    val refreshToken: String?,
    val expiresIn: Long?,
    val fullName: String? = null,
    val role: String? = null
)

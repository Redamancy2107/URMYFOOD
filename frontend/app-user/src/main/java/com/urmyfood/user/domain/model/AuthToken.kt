package com.urmyfood.user.domain.model

/**
 * Domain model representing authentication tokens.
 */
data class AuthToken(
    val accessToken: String,
    val refreshToken: String?,
    val expiresIn: Long?,
    val fullName: String? = null,
    val role: String? = null
)

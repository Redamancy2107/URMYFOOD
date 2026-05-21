package com.urmyfood.user.domain.model

data class UserProfile(
    val id: Long,
    val fullName: String,
    val email: String,
    val phone: String?,
    val role: String,
    val avatarUrl: String? = null
)

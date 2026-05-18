package com.urmyfood.user.domain.model

/**
 * Domain model representing an authenticated user.
 */
data class User(
    val id: String,
    val name: String,
    val email: String,
    val phone: String?,
    val avatarUrl: String?,
    val status: AccountStatus
)

enum class AccountStatus {
    ACTIVE,
    INACTIVE,
    BANNED
}

package com.urmyfood.user.data.model

import com.urmyfood.user.domain.model.AccountStatus
import com.urmyfood.user.domain.model.AuthToken
import com.urmyfood.user.domain.model.User

/**
 * Extension functions to map Data DTOs to Domain models.
 */

fun LoginResponse.toDomain(): AuthToken {
    return AuthToken(
        accessToken = accessToken,
        refreshToken = refreshToken,
        expiresIn = expiresIn
    )
}

fun UserResponse.toDomain(): User {
    return User(
        id = id,
        name = name,
        email = email,
        phone = phone,
        avatarUrl = avatarUrl,
        status = when (status.uppercase()) {
            "ACTIVE" -> AccountStatus.ACTIVE
            "INACTIVE" -> AccountStatus.INACTIVE
            "BANNED" -> AccountStatus.BANNED
            else -> AccountStatus.INACTIVE
        }
    )
}

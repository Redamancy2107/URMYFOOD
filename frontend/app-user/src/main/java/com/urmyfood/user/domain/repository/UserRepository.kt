package com.urmyfood.user.domain.repository

import com.urmyfood.user.domain.model.Result
import com.urmyfood.user.domain.model.UserProfile

interface UserRepository {
    suspend fun getMyProfile(token: String): Result<UserProfile>
    
    suspend fun updateProfile(
        token: String,
        fullName: String?,
        phone: String?,
        avatarUrl: String?
    ): Result<UserProfile>

    suspend fun changePassword(
        token: String,
        currentPass: String,
        newPass: String
    ): Result<Unit>
}

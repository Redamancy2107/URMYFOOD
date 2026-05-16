package com.urmyfood.user.domain.repository

import com.urmyfood.user.domain.model.Result
import com.urmyfood.user.domain.model.UserProfile

interface UserRepository {
    suspend fun getMyProfile(token: String): Result<UserProfile>
}

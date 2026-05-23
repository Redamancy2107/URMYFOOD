package com.urmyfood.user.domain.usecase

import com.urmyfood.user.domain.model.Result
import com.urmyfood.user.domain.model.TokenProvider
import com.urmyfood.user.domain.model.UserProfile
import com.urmyfood.user.domain.repository.UserRepository

class GetUserProfileUseCase(
    private val userRepository: UserRepository,
    private val tokenManager: TokenProvider
) {
    suspend operator fun invoke(): Result<UserProfile> {
        val token = tokenManager.getAccessToken()
            ?: return Result.Error("Không tìm thấy token đăng nhập")
        return userRepository.getMyProfile("Bearer $token")
    }
}

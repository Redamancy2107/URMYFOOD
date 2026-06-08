package com.urmyfood.shop.domain.usecase

import com.urmyfood.shared.data.local.TokenManager
import com.urmyfood.shared.domain.model.Result
import com.urmyfood.shop.domain.model.UserProfile
import com.urmyfood.shop.domain.repository.UserRepository

class UpdateUserProfileUseCase(
    private val userRepository: UserRepository,
    private val tokenManager: TokenManager
) {
    suspend operator fun invoke(
        fullName: String?,
        phone: String?,
        avatarUrl: String?
    ): Result<UserProfile> {
        val token = tokenManager.getAccessToken()
            ?: return Result.Error("Không tìm thấy token đăng nhập")
        return userRepository.updateProfile("Bearer $token", fullName, phone, avatarUrl)
    }
}

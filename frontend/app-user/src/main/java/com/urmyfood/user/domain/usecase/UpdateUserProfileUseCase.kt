package com.urmyfood.user.domain.usecase

import com.urmyfood.user.data.local.TokenManager
import com.urmyfood.user.domain.model.Result
import com.urmyfood.user.domain.model.UserProfile
import com.urmyfood.user.domain.repository.UserRepository

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
        val result = userRepository.updateProfile("Bearer $token", fullName, phone, avatarUrl)
        if (result is Result.Success) {
            tokenManager.saveFullName(result.data.fullName)
        }
        return result
    }
}

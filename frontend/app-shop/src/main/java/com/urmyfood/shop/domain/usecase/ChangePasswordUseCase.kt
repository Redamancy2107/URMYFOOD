package com.urmyfood.shop.domain.usecase

import com.urmyfood.shared.data.local.TokenManager
import com.urmyfood.shared.domain.model.Result
import com.urmyfood.shop.domain.repository.UserRepository

class ChangePasswordUseCase(
    private val userRepository: UserRepository,
    private val tokenManager: TokenManager
) {
    suspend operator fun invoke(currentPass: String, newPass: String): Result<Unit> {
        val token = tokenManager.getAccessToken()
            ?: return Result.Error("Không tìm thấy token đăng nhập")
        return userRepository.changePassword("Bearer $token", currentPass, newPass)
    }
}

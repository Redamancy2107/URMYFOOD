package com.urmyfood.user.domain.usecase

import com.urmyfood.user.domain.model.AuthToken
import com.urmyfood.user.domain.model.Result
import com.urmyfood.user.domain.repository.AuthRepository

/**
 * Use case for user login.
 * Validates input and delegates to the repository.
 */
class LoginUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(emailOrPhone: String, password: String): Result<AuthToken> {
        // Input validation
        if (emailOrPhone.isBlank()) {
            return Result.Error("Email hoặc số điện thoại không được để trống")
        }
        if (password.isBlank()) {
            return Result.Error("Mật khẩu không được để trống")
        }
        if (password.length < 6) {
            return Result.Error("Mật khẩu phải có ít nhất 6 ký tự")
        }

        return authRepository.login(emailOrPhone, password)
    }
}

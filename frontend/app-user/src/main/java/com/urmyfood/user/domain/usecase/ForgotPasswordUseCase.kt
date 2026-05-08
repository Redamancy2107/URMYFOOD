package com.urmyfood.user.domain.usecase

import com.urmyfood.user.domain.model.Result
import com.urmyfood.user.domain.repository.AuthRepository

/**
 * Use case for requesting a password reset.
 * Sends an OTP to the user's email.
 */
class ForgotPasswordUseCase(
    private val authRepository: AuthRepository
) {
    private val emailRegex = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")

    suspend operator fun invoke(email: String): Result<Unit> {
        if (email.isBlank()) {
            return Result.Error("Email không được để trống")
        }
        if (!emailRegex.matches(email)) {
            return Result.Error("Email không hợp lệ")
        }

        return authRepository.forgotPassword(email)
    }
}

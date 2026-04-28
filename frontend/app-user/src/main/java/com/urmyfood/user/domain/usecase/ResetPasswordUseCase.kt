package com.urmyfood.user.domain.usecase

import com.urmyfood.user.domain.model.Result
import com.urmyfood.user.domain.repository.AuthRepository

/**
 * Use case for resetting the user's password.
 * Requires the reset token obtained after OTP verification.
 */
class ResetPasswordUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        resetToken: String,
        newPassword: String,
        confirmPassword: String
    ): Result<Unit> {
        if (newPassword.isBlank()) {
            return Result.Error("Mật khẩu mới không được để trống")
        }
        if (newPassword.length < 6) {
            return Result.Error("Mật khẩu mới phải có ít nhất 6 ký tự")
        }
        if (newPassword != confirmPassword) {
            return Result.Error("Mật khẩu xác nhận không khớp")
        }

        return authRepository.resetPassword(resetToken, newPassword)
    }
}

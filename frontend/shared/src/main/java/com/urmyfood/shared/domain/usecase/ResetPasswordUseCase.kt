package com.urmyfood.shared.domain.usecase

import com.urmyfood.shared.domain.model.Result
import com.urmyfood.shared.domain.repository.AuthRepository

class ResetPasswordUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(
        resetToken: String,
        newPassword: String,
        confirmPassword: String
    ): Result<Unit> {
        if (newPassword.length < 6) return Result.Error("Mật khẩu phải có ít nhất 6 ký tự")
        if (newPassword != confirmPassword) return Result.Error("Mật khẩu xác nhận không khớp")
        return authRepository.resetPassword(resetToken, newPassword)
    }
}

package com.urmyfood.user.domain.usecase

import com.urmyfood.user.domain.model.Result
import com.urmyfood.user.domain.repository.AuthRepository

/**
 * Use case for verifying OTP code.
 * Returns a reset token on success.
 */
class VerifyOtpUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, otpCode: String): Result<String> {
        if (email.isBlank()) {
            return Result.Error("Email chưa được cung cấp. Vui lòng quay lại.")
        }
        if (otpCode.isBlank()) {
            return Result.Error("Mã OTP không được để trống")
        }
        if (otpCode.length != 6) {
            return Result.Error("Mã OTP phải có 6 chữ số")
        }
        if (!otpCode.all { it.isDigit() }) {
            return Result.Error("Mã OTP chỉ chứa chữ số")
        }

        return authRepository.verifyOtp(email, otpCode)
    }
}

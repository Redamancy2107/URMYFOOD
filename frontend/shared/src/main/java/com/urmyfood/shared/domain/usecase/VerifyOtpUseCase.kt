package com.urmyfood.shared.domain.usecase

import com.urmyfood.shared.domain.model.Result
import com.urmyfood.shared.domain.repository.AuthRepository

class VerifyOtpUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(email: String, otpCode: String): Result<String> {
        if (otpCode.length != 6 || !otpCode.all { it.isDigit() }) {
            return Result.Error("Mã OTP phải gồm 6 chữ số")
        }
        return authRepository.verifyOtp(email, otpCode)
    }
}

package com.urmyfood.shared.domain.usecase

import com.urmyfood.shared.domain.model.Result
import com.urmyfood.shared.domain.repository.AuthRepository

class SendOtpUseCase(private val authRepository: AuthRepository) {

    private val emailRegex = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+\$")

    suspend operator fun invoke(email: String, phone: String?): Result<Unit> {
        if (!emailRegex.matches(email.trim())) return Result.Error("Email không hợp lệ")
        return authRepository.sendOtp(email.trim(), phone?.trim())
    }
}

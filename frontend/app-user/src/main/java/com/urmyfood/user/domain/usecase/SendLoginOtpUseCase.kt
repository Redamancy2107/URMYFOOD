package com.urmyfood.user.domain.usecase

import com.urmyfood.user.domain.model.Result
import com.urmyfood.user.domain.repository.AuthRepository

class SendLoginOtpUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, phone: String? = null): Result<Unit> {
        return authRepository.sendLoginOtp(email, phone)
    }
}

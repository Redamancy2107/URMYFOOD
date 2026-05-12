package com.urmyfood.user.domain.usecase

import com.urmyfood.user.domain.model.AuthToken
import com.urmyfood.user.domain.model.Result
import com.urmyfood.user.domain.repository.AuthRepository

class LoginWithOtpUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, otpCode: String): Result<AuthToken> {
        return authRepository.loginWithOtp(email, otpCode)
    }
}

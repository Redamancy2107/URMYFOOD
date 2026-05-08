package com.urmyfood.user.domain.usecase

import com.urmyfood.user.domain.model.AuthToken
import com.urmyfood.user.domain.model.Result
import com.urmyfood.user.domain.repository.AuthRepository

class LoginWithGoogleUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(idToken: String): Result<AuthToken> {
        if (idToken.isBlank()) {
            return Result.Error("ID Token không hợp lệ")
        }
        return authRepository.loginWithGoogle(idToken)
    }
}

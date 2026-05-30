package com.urmyfood.shared.domain.usecase

import com.urmyfood.shared.domain.model.Result
import com.urmyfood.shared.domain.repository.AuthRepository

class ForgotPasswordUseCase(private val authRepository: AuthRepository) {

    private val emailRegex = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+\$")

    suspend operator fun invoke(email: String): Result<Unit> {
        if (email.isBlank()) return Result.Error("Vui lòng nhập email")
        if (!emailRegex.matches(email.trim())) return Result.Error("Email không hợp lệ")
        return authRepository.forgotPassword(email.trim())
    }
}

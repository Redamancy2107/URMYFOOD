package com.urmyfood.shared.domain.usecase

import com.urmyfood.shared.domain.model.AuthToken
import com.urmyfood.shared.domain.model.Result
import com.urmyfood.shared.domain.repository.AuthRepository

class LoginUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(emailOrPhone: String, password: String): Result<AuthToken> {
        if (emailOrPhone.isBlank()) return Result.Error("Vui lòng nhập email hoặc số điện thoại")
        if (password.isBlank()) return Result.Error("Vui lòng nhập mật khẩu")
        return authRepository.login(emailOrPhone.trim(), password)
    }
}

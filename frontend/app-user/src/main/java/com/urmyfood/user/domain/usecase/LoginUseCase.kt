package com.urmyfood.user.domain.usecase

import com.urmyfood.user.domain.model.AuthToken
import com.urmyfood.user.domain.model.Result
import com.urmyfood.user.domain.repository.AuthRepository

/**
 * Use case for user login.
 * Validates input and delegates to the repository.
 */
class LoginUseCase(
    private val authRepository: AuthRepository
) {
    private val emailRegex = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")
    private val phoneRegex = Regex("^(0|\\+84)[0-9]{9,10}$")

    suspend operator fun invoke(emailOrPhone: String, password: String): Result<AuthToken> {
        // Input validation
        if (emailOrPhone.isBlank()) {
            return Result.Error("Email hoặc số điện thoại không được để trống")
        }

        // Check if it matches email or phone format
        if (!emailRegex.matches(emailOrPhone) && !phoneRegex.matches(emailOrPhone)) {
            return Result.Error("Email hoặc số điện thoại không đúng định dạng")
        }

        if (password.isBlank()) {
            return Result.Error("Mật khẩu không được để trống")
        }
        if (password.length < 6) {
            return Result.Error("Mật khẩu phải có ít nhất 6 ký tự")
        }

        return authRepository.login(emailOrPhone, password)
    }
}

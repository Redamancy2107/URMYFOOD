package com.urmyfood.user.domain.usecase

import com.urmyfood.user.domain.model.Result
import com.urmyfood.user.domain.model.User
import com.urmyfood.user.domain.repository.AuthRepository

/**
 * Use case for user registration.
 * Validates all registration fields and delegates to the repository.
 */
class RegisterUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        name: String,
        email: String,
        phone: String,
        password: String,
        confirmPassword: String
    ): Result<User> {
        // Validate name
        if (name.isBlank()) {
            return Result.Error("Họ và tên không được để trống")
        }
        if (name.length < 2) {
            return Result.Error("Họ và tên phải có ít nhất 2 ký tự")
        }

        // Validate email
        if (email.isBlank()) {
            return Result.Error("Email không được để trống")
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return Result.Error("Email không hợp lệ")
        }

        // Validate phone
        if (phone.isBlank()) {
            return Result.Error("Số điện thoại không được để trống")
        }
        if (!phone.matches(Regex("^(0|\\+84)[0-9]{9,10}$"))) {
            return Result.Error("Số điện thoại không hợp lệ")
        }

        // Validate password
        if (password.isBlank()) {
            return Result.Error("Mật khẩu không được để trống")
        }
        if (password.length < 6) {
            return Result.Error("Mật khẩu phải có ít nhất 6 ký tự")
        }

        // Validate confirm password
        if (password != confirmPassword) {
            return Result.Error("Mật khẩu xác nhận không khớp")
        }

        return authRepository.register(name, email, phone, password)
    }
}

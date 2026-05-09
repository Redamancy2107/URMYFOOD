package com.urmyfood.user.domain.usecase

import com.urmyfood.user.domain.model.Result
import com.urmyfood.user.domain.model.User
import com.urmyfood.user.domain.repository.AuthRepository

/**
 * Use case for user registration.
 */
class RegisterUseCase(
    private val authRepository: AuthRepository
) {
    private val emailRegex = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")

    suspend operator fun invoke(
        fullName: String,
        email: String,
        phone: String,
        password: String,
        confirmPassword: String,
        otpCode: String
    ): Result<User> {
        // Validation logic
        if (fullName.isBlank()) return Result.Error("Họ và tên không được để trống")
        if (email.isBlank()) return Result.Error("Email không được để trống")
        if (phone.isBlank()) return Result.Error("Số điện thoại không được để trống")
        if (password.isBlank()) return Result.Error("Mật khẩu không được để trống")
        if (password != confirmPassword) return Result.Error("Mật khẩu xác nhận không khớp")
        if (otpCode.isBlank()) return Result.Error("Vui lòng nhập mã OTP")

        return authRepository.register(fullName, email, phone, password, otpCode)
    }
}

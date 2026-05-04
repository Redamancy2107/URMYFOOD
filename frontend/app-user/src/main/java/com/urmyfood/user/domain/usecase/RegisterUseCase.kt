package com.urmyfood.user.domain.usecase

import com.urmyfood.user.domain.model.AuthToken
import com.urmyfood.user.domain.model.Result
import com.urmyfood.user.domain.repository.AuthRepository

/**
 * Use case for user registration.
 * Validates all registration fields and delegates to the repository.
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
    ): Result<AuthToken> {
        // Validate name
        if (fullName.isBlank()) {
            return Result.Error("Họ và tên không được để trống")
        }
        if (fullName.length < 2) {
            return Result.Error("Họ và tên phải có ít nhất 2 ký tự")
        }

        // Validate email
        if (email.isBlank()) {
            return Result.Error("Email không được để trống")
        }
        if (!emailRegex.matches(email)) {
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

        // Validate OTP
        if (otpCode.isBlank()) {
            return Result.Error("Vui lòng nhập mã OTP")
        }
        if (otpCode.length != 6) {
            return Result.Error("Mã OTP phải có 6 chữ số")
        }

        return authRepository.register(fullName, email, phone, password, otpCode)
    }
}

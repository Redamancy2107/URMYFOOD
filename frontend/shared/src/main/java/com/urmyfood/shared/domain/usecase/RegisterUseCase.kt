package com.urmyfood.shared.domain.usecase

import com.urmyfood.shared.domain.model.AuthToken
import com.urmyfood.shared.domain.model.Result
import com.urmyfood.shared.domain.repository.AuthRepository

class RegisterUseCase(private val authRepository: AuthRepository) {

    private val emailRegex = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+\$")

    suspend operator fun invoke(
        fullName: String,
        email: String,
        phone: String,
        password: String,
        confirmPassword: String,
        otpCode: String,
        role: String? = null
    ): Result<AuthToken> {
        if (fullName.isBlank()) return Result.Error("Vui lòng nhập họ và tên")
        if (!emailRegex.matches(email.trim())) return Result.Error("Email không hợp lệ")
        if (phone.length < 9 || phone.length > 11 || !phone.all { it.isDigit() }) {
            return Result.Error("Số điện thoại không hợp lệ (9 đến 11 chữ số)")
        }
        if (password.length < 6) return Result.Error("Mật khẩu phải có ít nhất 6 ký tự")
        if (password != confirmPassword) return Result.Error("Mật khẩu xác nhận không khớp")
        return authRepository.register(fullName.trim(), email.trim(), phone.trim(), password, otpCode, role)
    }
}

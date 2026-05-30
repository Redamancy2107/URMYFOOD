package com.urmyfood.shared.domain.repository

import com.urmyfood.shared.domain.model.AuthToken
import com.urmyfood.shared.domain.model.Result

interface AuthRepository {
    suspend fun login(emailOrPhone: String, password: String): Result<AuthToken>
    suspend fun forgotPassword(email: String): Result<Unit>
    suspend fun verifyOtp(email: String, otpCode: String): Result<String>
    suspend fun resetPassword(resetToken: String, newPassword: String): Result<Unit>
}

package com.urmyfood.user.domain.repository

import com.urmyfood.user.domain.model.AuthToken
import com.urmyfood.user.domain.model.Result
import com.urmyfood.user.domain.model.User

/**
 * Repository interface for authentication operations.
 */
interface AuthRepository {

    suspend fun login(emailOrPhone: String, password: String): Result<AuthToken>

    suspend fun register(
        fullName: String,
        email: String,
        phone: String,
        password: String,
        otpCode: String
    ): Result<User>

    suspend fun forgotPassword(email: String): Result<Unit>

    suspend fun verifyOtp(email: String, otpCode: String): Result<String>

    suspend fun resetPassword(
        resetToken: String,
        newPassword: String
    ): Result<Unit>

    suspend fun loginWithGoogle(idToken: String): Result<AuthToken>

    suspend fun sendLoginOtp(email: String): Result<Unit>

    suspend fun loginWithOtp(email: String, otpCode: String): Result<AuthToken>
}

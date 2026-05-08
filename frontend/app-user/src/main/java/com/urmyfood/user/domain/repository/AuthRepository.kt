package com.urmyfood.user.domain.repository

import com.urmyfood.user.domain.model.AuthToken
import com.urmyfood.user.domain.model.Result
import com.urmyfood.user.domain.model.User

/**
 * Repository interface for authentication operations.
 * Defined in the domain layer; implemented in the data layer.
 */
interface AuthRepository {

    /**
     * Login with email/phone and password.
     */
    suspend fun login(emailOrPhone: String, password: String): Result<AuthToken>

    /**
     * Register a new customer account.
     */
    suspend fun register(
        fullName: String,
        email: String,
        phone: String,
        password: String,
        otpCode: String
    ): Result<AuthToken>

    /**
     * Request a password reset OTP to be sent to the given email.
     */
    suspend fun forgotPassword(email: String): Result<Unit>

    /**
     * Verify the OTP code for password reset.
     */
    suspend fun verifyOtp(email: String, otpCode: String): Result<String>

    /**
     * Reset the user's password using the reset token obtained from OTP verification.
     */
    suspend fun resetPassword(
        resetToken: String,
        newPassword: String
    ): Result<Unit>

    /**
     * Login with Google ID Token.
     */
    suspend fun loginWithGoogle(idToken: String): Result<AuthToken>

    /**
     * Send OTP for login purpose.
     */
    suspend fun sendLoginOtp(email: String): Result<Unit>

    /**
     * Login with email and OTP code.
     */
    suspend fun loginWithOtp(email: String, otpCode: String): Result<AuthToken>
}

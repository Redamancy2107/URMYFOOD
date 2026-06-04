package com.urmyfood.shared.domain.usecase

import com.urmyfood.shared.domain.model.AuthToken
import com.urmyfood.shared.domain.model.Result
import com.urmyfood.shared.domain.repository.AuthRepository

class FakeAuthRepository : AuthRepository {

    var loginResult: Result<AuthToken> = Result.Error("not set")
    var registerResult: Result<AuthToken> = Result.Error("not set")
    var sendOtpResult: Result<Unit> = Result.Success(Unit)
    var forgotResult: Result<Unit> = Result.Success(Unit)
    var verifyResult: Result<String> = Result.Success("reset-token")
    var resetResult: Result<Unit> = Result.Success(Unit)

    var lastLoginEmailOrPhone: String? = null
    var lastRegisterEmail: String? = null
    var lastRegisterPhone: String? = null
    var lastSendOtpEmail: String? = null
    var lastForgotEmail: String? = null
    var lastVerifyEmail: String? = null
    var lastVerifyOtp: String? = null
    var lastResetToken: String? = null
    var lastResetPassword: String? = null

    override suspend fun login(emailOrPhone: String, password: String): Result<AuthToken> {
        lastLoginEmailOrPhone = emailOrPhone
        return loginResult
    }

    override suspend fun register(
        fullName: String,
        email: String,
        phone: String,
        password: String,
        otpCode: String
    ): Result<AuthToken> {
        lastRegisterEmail = email
        lastRegisterPhone = phone
        return registerResult
    }

    override suspend fun sendOtp(email: String, phone: String?): Result<Unit> {
        lastSendOtpEmail = email
        return sendOtpResult
    }

    override suspend fun forgotPassword(email: String): Result<Unit> {
        lastForgotEmail = email
        return forgotResult
    }

    override suspend fun verifyOtp(email: String, otpCode: String): Result<String> {
        lastVerifyEmail = email
        lastVerifyOtp = otpCode
        return verifyResult
    }

    override suspend fun resetPassword(resetToken: String, newPassword: String): Result<Unit> {
        lastResetToken = resetToken
        lastResetPassword = newPassword
        return resetResult
    }
}

package com.urmyfood.shop.presentation.auth

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
    var lastRegisterRole: String? = null
    var lastSendOtpEmail: String? = null

    override suspend fun login(emailOrPhone: String, password: String): Result<AuthToken> = loginResult
    override suspend fun register(
        fullName: String,
        email: String,
        phone: String,
        password: String,
        otpCode: String,
        role: String?
    ): Result<AuthToken> {
        lastRegisterRole = role
        return registerResult
    }
    override suspend fun sendOtp(email: String, phone: String?): Result<Unit> {
        lastSendOtpEmail = email
        return sendOtpResult
    }
    override suspend fun forgotPassword(email: String): Result<Unit> = forgotResult
    override suspend fun verifyOtp(email: String, otpCode: String): Result<String> = verifyResult
    override suspend fun resetPassword(resetToken: String, newPassword: String): Result<Unit> = resetResult
}

package com.urmyfood.shared.data.repository

import com.urmyfood.shared.data.model.ApiResponse
import com.urmyfood.shared.data.model.ForgotPasswordRequest
import com.urmyfood.shared.data.model.LoginRequest
import com.urmyfood.shared.data.model.RegisterRequest
import com.urmyfood.shared.data.model.ResetPasswordRequest
import com.urmyfood.shared.data.model.SendOtpRequest
import com.urmyfood.shared.data.model.VerifyOtpRequest
import com.google.gson.Gson
import com.urmyfood.shared.data.model.toDomain
import com.urmyfood.shared.data.remote.AuthApiService
import com.urmyfood.shared.domain.model.AuthToken
import com.urmyfood.shared.domain.model.Result
import com.urmyfood.shared.domain.repository.AuthRepository

class AuthRepositoryImpl(
    private val authApiService: AuthApiService
) : AuthRepository {

    private val gson = Gson()

    override suspend fun login(emailOrPhone: String, password: String): Result<AuthToken> {
        return try {
            val response = authApiService.login(LoginRequest(emailOrPhone, password))
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success && body.data != null) {
                    Result.Success(body.data.toDomain())
                } else {
                    Result.Error(body?.message ?: "Đăng nhập thất bại", response.code())
                }
            } else {
                val msg = parseErrorMessage(response.errorBody()?.string())
                Result.Error(msg ?: "Đăng nhập thất bại. Vui lòng kiểm tra lại thông tin.", response.code())
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Lỗi kết nối. Vui lòng thử lại.")
        }
    }

    override suspend fun register(
        fullName: String,
        email: String,
        phone: String,
        password: String,
        otpCode: String
    ): Result<AuthToken> {
        return try {
            val response = authApiService.register(
                RegisterRequest(fullName, email, phone, password, otpCode)
            )
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success && body.data != null) {
                    Result.Success(body.data.toDomain())
                } else {
                    Result.Error(body?.message ?: "Đăng ký thất bại", response.code())
                }
            } else {
                val msg = parseErrorMessage(response.errorBody()?.string())
                Result.Error(msg ?: "Đăng ký thất bại. Email hoặc số điện thoại có thể đã tồn tại.", response.code())
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Lỗi kết nối. Vui lòng thử lại.")
        }
    }

    override suspend fun sendOtp(email: String, phone: String?): Result<Unit> {
        return try {
            val response = authApiService.sendOtp(SendOtpRequest(email, phone))
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success) Result.Success(Unit)
                else Result.Error(body?.message ?: "Gửi OTP thất bại", response.code())
            } else {
                val msg = parseErrorMessage(response.errorBody()?.string())
                Result.Error(msg ?: "Gửi OTP thất bại. Vui lòng thử lại.", response.code())
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Lỗi kết nối. Vui lòng thử lại.")
        }
    }

    override suspend fun forgotPassword(email: String): Result<Unit> {
        return try {
            val response = authApiService.forgotPassword(ForgotPasswordRequest(email))
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success) Result.Success(Unit)
                else Result.Error(body?.message ?: "Gửi OTP thất bại", response.code())
            } else {
                Result.Error("Không tìm thấy tài khoản với email này.", response.code())
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Lỗi kết nối. Vui lòng thử lại.")
        }
    }

    override suspend fun verifyOtp(email: String, otpCode: String): Result<String> {
        return try {
            val response = authApiService.verifyOtp(VerifyOtpRequest(email, otpCode))
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success && body.data != null) {
                    Result.Success(body.data.resetToken)
                } else {
                    Result.Error(body?.message ?: "Mã OTP không hợp lệ", response.code())
                }
            } else {
                Result.Error("Mã OTP không đúng hoặc đã hết hạn.", response.code())
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Lỗi kết nối. Vui lòng thử lại.")
        }
    }

    override suspend fun resetPassword(resetToken: String, newPassword: String): Result<Unit> {
        return try {
            val response = authApiService.resetPassword(ResetPasswordRequest(resetToken, newPassword))
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success) Result.Success(Unit)
                else Result.Error(body?.message ?: "Đặt lại mật khẩu thất bại", response.code())
            } else {
                Result.Error("Đặt lại mật khẩu thất bại. Vui lòng thử lại.", response.code())
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Lỗi kết nối. Vui lòng thử lại.")
        }
    }

    private fun parseErrorMessage(errorBodyStr: String?): String? {
        return try {
            gson.fromJson(errorBodyStr, ApiResponse::class.java)?.message
        } catch (e: Exception) {
            null
        }
    }
}

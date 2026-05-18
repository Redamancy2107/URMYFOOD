package com.urmyfood.user.data.repository

import com.urmyfood.user.data.model.ApiResponse
import com.urmyfood.user.data.model.ForgotPasswordRequest
import com.urmyfood.user.data.model.LoginRequest
import com.urmyfood.user.data.model.RegisterRequest
import com.urmyfood.user.data.model.ResetPasswordRequest
import com.urmyfood.user.data.model.VerifyOtpRequest
import com.urmyfood.user.data.model.toDomain
import com.urmyfood.user.data.remote.AuthApiService
import com.urmyfood.user.domain.model.AuthToken
import com.urmyfood.user.domain.model.Result
import com.urmyfood.user.domain.model.User
import com.urmyfood.user.domain.repository.AuthRepository

/**
 * Implementation of [AuthRepository].
 * Handles API calls and maps responses to domain models.
 */
class AuthRepositoryImpl(
    private val authApiService: AuthApiService
) : AuthRepository {

    override suspend fun login(emailOrPhone: String, password: String): Result<AuthToken> {
        return try {
            val response = authApiService.login(
                LoginRequest(emailOrPhone = emailOrPhone, password = password)
            )
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success && body.data != null) {
                    Result.Success(body.data.toDomain())
                } else {
                    Result.Error(
                        message = body?.message ?: "Đăng nhập thất bại",
                        code = response.code()
                    )
                }
            } else {
                val errorMsg = try {
                    val errorBody = response.errorBody()?.string()
                    val apiResponse = com.google.gson.Gson().fromJson(errorBody, ApiResponse::class.java)
                    apiResponse.message
                } catch (e: Exception) {
                    null
                }
                Result.Error(
                    message = errorMsg ?: "Đăng nhập thất bại. Vui lòng kiểm tra lại thông tin.",
                    code = response.code()
                )
            }
        } catch (e: Exception) {
            Result.Error(message = e.message ?: "Lỗi kết nối. Vui lòng thử lại.")
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
                RegisterRequest(fullName = fullName, email = email, phone = phone, password = password, otpCode = otpCode)
            )
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success && body.data != null) {
                    Result.Success(body.data.toDomain())
                } else {
                    Result.Error(
                        message = body?.message ?: "Đăng ký thất bại",
                        code = response.code()
                    )
                }
            } else {
                val errorMsg = try {
                    val errorBody = response.errorBody()?.string()
                    val apiResponse = com.google.gson.Gson().fromJson(errorBody, ApiResponse::class.java)
                    apiResponse.message
                } catch (e: Exception) {
                    null
                }
                Result.Error(
                    message = errorMsg ?: "Đăng ký thất bại. Email hoặc số điện thoại đã tồn tại hoặc mã OTP không đúng.",
                    code = response.code()
                )
            }
        } catch (e: Exception) {
            Result.Error(message = e.message ?: "Lỗi kết nối. Vui lòng thử lại.")
        }
    }

    override suspend fun forgotPassword(email: String): Result<Unit> {
        return try {
            val response = authApiService.forgotPassword(
                ForgotPasswordRequest(email = email)
            )
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success) {
                    Result.Success(Unit)
                } else {
                    Result.Error(
                        message = body?.message ?: "Gửi OTP thất bại",
                        code = response.code()
                    )
                }
            } else {
                Result.Error(
                    message = "Không tìm thấy tài khoản với email này.",
                    code = response.code()
                )
            }
        } catch (e: Exception) {
            Result.Error(message = e.message ?: "Lỗi kết nối. Vui lòng thử lại.")
        }
    }

    override suspend fun verifyOtp(email: String, otpCode: String): Result<String> {
        return try {
            val response = authApiService.verifyOtp(
                VerifyOtpRequest(email = email, otpCode = otpCode)
            )
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success && body.data != null) {
                    Result.Success(body.data.resetToken)
                } else {
                    Result.Error(
                        message = body?.message ?: "Mã OTP không hợp lệ",
                        code = response.code()
                    )
                }
            } else {
                Result.Error(
                    message = "Mã OTP không đúng hoặc đã hết hạn.",
                    code = response.code()
                )
            }
        } catch (e: Exception) {
            Result.Error(message = e.message ?: "Lỗi kết nối. Vui lòng thử lại.")
        }
    }

    override suspend fun resetPassword(resetToken: String, newPassword: String): Result<Unit> {
        return try {
            val response = authApiService.resetPassword(
                ResetPasswordRequest(resetToken = resetToken, newPassword = newPassword)
            )
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success) {
                    Result.Success(Unit)
                } else {
                    Result.Error(
                        message = body?.message ?: "Đặt lại mật khẩu thất bại",
                        code = response.code()
                    )
                }
            } else {
                Result.Error(
                    message = "Đặt lại mật khẩu thất bại. Vui lòng thử lại.",
                    code = response.code()
                )
            }
        } catch (e: Exception) {
            Result.Error(message = e.message ?: "Lỗi kết nối. Vui lòng thử lại.")
        }
    }

    override suspend fun loginWithGoogle(idToken: String): Result<AuthToken> {
        return try {
            val response = authApiService.loginGoogle(mapOf("idToken" to idToken))
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success && body.data != null) {
                    Result.Success(body.data.toDomain())
                } else {
                    Result.Error(message = body?.message ?: "Đăng nhập Google thất bại")
                }
            } else {
                Result.Error(message = "Lỗi xác thực Google")
            }
        } catch (e: Exception) {
            Result.Error(message = e.message ?: "Lỗi kết nối")
        }
    }

    override suspend fun sendLoginOtp(email: String, phone: String?): Result<Unit> {
        return try {
            val response = authApiService.sendOtp(mapOf("email" to email, "phone" to phone))
            if (response.isSuccessful) {
                Result.Success(Unit)
            } else {
                val errorMsg = try {
                    val errorBody = response.errorBody()?.string()
                    val apiResponse = com.google.gson.Gson().fromJson(errorBody, ApiResponse::class.java)
                    apiResponse.message
                } catch (e: Exception) {
                    null
                }
                Result.Error(message = errorMsg ?: "Gửi OTP thất bại")
            }
        } catch (e: Exception) {
            Result.Error(message = e.message ?: "Lỗi kết nối")
        }
    }

    override suspend fun loginWithOtp(email: String, otpCode: String): Result<AuthToken> {
        return try {
            val response = authApiService.loginOtp(mapOf("email" to email, "code" to otpCode))
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success && body.data != null) {
                    Result.Success(body.data.toDomain())
                } else {
                    Result.Error(message = body?.message ?: "Đăng nhập OTP thất bại")
                }
            } else {
                Result.Error(message = "Lỗi xác thực OTP")
            }
        } catch (e: Exception) {
            Result.Error(message = e.message ?: "Lỗi kết nối")
        }
    }
}

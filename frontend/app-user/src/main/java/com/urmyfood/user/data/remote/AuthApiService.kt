package com.urmyfood.user.data.remote

import com.urmyfood.user.data.model.ApiResponse
import com.urmyfood.user.data.model.ForgotPasswordRequest
import com.urmyfood.user.data.model.LoginRequest
import com.urmyfood.user.data.model.LoginResponse
import com.urmyfood.user.data.model.RegisterRequest
import com.urmyfood.user.data.model.ResetPasswordRequest
import com.urmyfood.user.data.model.UserResponse
import com.urmyfood.user.data.model.VerifyOtpRequest
import com.urmyfood.user.data.model.VerifyOtpResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Retrofit API service interface for authentication endpoints.
 */
interface AuthApiService {

    @POST("api/v1/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<ApiResponse<LoginResponse>>

    @POST("api/v1/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<ApiResponse<LoginResponse>>

    @POST("api/v1/auth/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): Response<ApiResponse<Unit>>

    @POST("api/v1/auth/verify-otp")
    suspend fun verifyOtp(@Body request: VerifyOtpRequest): Response<ApiResponse<VerifyOtpResponse>>

    @POST("api/v1/auth/reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): Response<ApiResponse<Unit>>

    @POST("api/v1/auth/login-google")
    suspend fun loginGoogle(@Body request: Map<String, String>): Response<ApiResponse<LoginResponse>>

    @POST("api/v1/auth/send-otp")
    suspend fun sendOtp(@Body request: Map<String, String?>): Response<ApiResponse<Unit>>

    @POST("api/v1/auth/login-otp")
    suspend fun loginOtp(@Body request: Map<String, String>): Response<ApiResponse<LoginResponse>>
}

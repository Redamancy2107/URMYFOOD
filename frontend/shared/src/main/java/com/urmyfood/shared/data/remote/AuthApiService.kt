package com.urmyfood.shared.data.remote

import com.urmyfood.shared.data.model.ApiResponse
import com.urmyfood.shared.data.model.ForgotPasswordRequest
import com.urmyfood.shared.data.model.LoginRequest
import com.urmyfood.shared.data.model.LoginResponse
import com.urmyfood.shared.data.model.ResetPasswordRequest
import com.urmyfood.shared.data.model.VerifyOtpRequest
import com.urmyfood.shared.data.model.VerifyOtpResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {

    @POST("api/v1/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<ApiResponse<LoginResponse>>

    @POST("api/v1/auth/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): Response<ApiResponse<Unit>>

    @POST("api/v1/auth/verify-otp")
    suspend fun verifyOtp(@Body request: VerifyOtpRequest): Response<ApiResponse<VerifyOtpResponse>>

    @POST("api/v1/auth/reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): Response<ApiResponse<Unit>>
}

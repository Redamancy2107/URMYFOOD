package com.urmyfood.user.di

import com.urmyfood.user.data.remote.AuthApiService
import com.urmyfood.user.data.remote.RetrofitClient
import com.urmyfood.user.data.repository.AuthRepositoryImpl
import com.urmyfood.user.domain.repository.AuthRepository
import com.urmyfood.user.domain.usecase.ForgotPasswordUseCase
import com.urmyfood.user.domain.usecase.LoginUseCase
import com.urmyfood.user.domain.usecase.RegisterUseCase
import com.urmyfood.user.domain.usecase.ResetPasswordUseCase
import com.urmyfood.user.domain.usecase.VerifyOtpUseCase

/**
 * Manual Dependency Injection container (Service Locator pattern).
 * Provides singleton instances of all dependencies following Clean Architecture layers.
 *
 * Data Layer → Domain Layer → Presentation Layer
 */
object ServiceLocator {

    // ==================== DATA LAYER ====================

    private val authApiService: AuthApiService by lazy {
        RetrofitClient.authApiService
    }

    // ==================== DOMAIN LAYER ====================

    private val authRepository: AuthRepository by lazy {
        AuthRepositoryImpl(authApiService)
    }

    // ==================== USE CASES ====================

    val loginUseCase: LoginUseCase by lazy {
        LoginUseCase(authRepository)
    }

    val registerUseCase: RegisterUseCase by lazy {
        RegisterUseCase(authRepository)
    }

    val forgotPasswordUseCase: ForgotPasswordUseCase by lazy {
        ForgotPasswordUseCase(authRepository)
    }

    val verifyOtpUseCase: VerifyOtpUseCase by lazy {
        VerifyOtpUseCase(authRepository)
    }

    val resetPasswordUseCase: ResetPasswordUseCase by lazy {
        ResetPasswordUseCase(authRepository)
    }
}

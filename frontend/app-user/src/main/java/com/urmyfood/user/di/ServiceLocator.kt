package com.urmyfood.user.di

import android.content.Context
import com.urmyfood.user.data.local.TokenManager
import com.urmyfood.user.data.remote.AuthApiService
import com.urmyfood.user.data.remote.RetrofitClient
import com.urmyfood.user.data.repository.AuthRepositoryImpl
import com.urmyfood.user.domain.repository.AuthRepository
import com.urmyfood.user.domain.usecase.*
import com.urmyfood.user.presentation.auth.forgotpass.ForgotPasswordViewModel
import com.urmyfood.user.presentation.auth.login.LoginViewModel
import com.urmyfood.user.presentation.auth.register.RegisterViewModel

/**
 * Manual Dependency Injection container (Service Locator pattern).
 * Provides singleton instances of all dependencies following Clean Architecture layers.
 */
object ServiceLocator {

    private lateinit var applicationContext: Context

    fun init(context: Context) {
        applicationContext = context.applicationContext
    }

    // ==================== DATA LAYER ====================

    private val authApiService: AuthApiService by lazy {
        RetrofitClient.authApiService
    }

    val tokenManager: TokenManager by lazy {
        TokenManager(applicationContext)
    }

    // ==================== DOMAIN LAYER ====================

    private val authRepository: AuthRepository by lazy {
        AuthRepositoryImpl(authApiService)
    }

    // ==================== USE CASES ====================

    val loginUseCase: LoginUseCase by lazy { LoginUseCase(authRepository) }
    val loginWithGoogleUseCase: LoginWithGoogleUseCase by lazy { LoginWithGoogleUseCase(authRepository) }
    val sendLoginOtpUseCase: SendLoginOtpUseCase by lazy { SendLoginOtpUseCase(authRepository) }
    val loginWithOtpUseCase: LoginWithOtpUseCase by lazy { LoginWithOtpUseCase(authRepository) }
    val registerUseCase: RegisterUseCase by lazy { RegisterUseCase(authRepository) }
    val forgotPasswordUseCase: ForgotPasswordUseCase by lazy { ForgotPasswordUseCase(authRepository) }
    val verifyOtpUseCase: VerifyOtpUseCase by lazy { VerifyOtpUseCase(authRepository) }
    val resetPasswordUseCase: ResetPasswordUseCase by lazy { ResetPasswordUseCase(authRepository) }

    // ==================== VIEW MODEL FACTORIES ====================

    fun provideLoginViewModelFactory(): LoginViewModel.Factory {
        return LoginViewModel.Factory(
            loginUseCase,
            loginWithGoogleUseCase,
            sendLoginOtpUseCase,
            loginWithOtpUseCase
        )
    }

    fun provideRegisterViewModelFactory(): RegisterViewModel.Factory {
        return RegisterViewModel.Factory(
            registerUseCase,
            sendLoginOtpUseCase
        )
    }

    fun provideForgotPasswordViewModelFactory(): ForgotPasswordViewModel.Factory {
        return ForgotPasswordViewModel.Factory(
            forgotPasswordUseCase,
            verifyOtpUseCase,
            resetPasswordUseCase
        )
    }
}

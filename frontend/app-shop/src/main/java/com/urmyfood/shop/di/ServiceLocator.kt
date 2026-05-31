package com.urmyfood.shop.di

import android.content.Context
import com.urmyfood.shop.BuildConfig
import com.urmyfood.shared.data.local.TokenManager
import com.urmyfood.shared.data.remote.NetworkModule
import com.urmyfood.shared.data.repository.AuthRepositoryImpl
import com.urmyfood.shared.domain.repository.AuthRepository
import com.urmyfood.shared.domain.usecase.ForgotPasswordUseCase
import com.urmyfood.shared.domain.usecase.LoginUseCase
import com.urmyfood.shared.domain.usecase.RegisterUseCase
import com.urmyfood.shared.domain.usecase.ResetPasswordUseCase
import com.urmyfood.shared.domain.usecase.SendOtpUseCase
import com.urmyfood.shared.domain.usecase.VerifyOtpUseCase
import com.urmyfood.shop.presentation.auth.forgotpass.ForgotPasswordViewModel
import com.urmyfood.shop.presentation.auth.login.LoginViewModel
import com.urmyfood.shop.presentation.auth.register.RegisterViewModel

object ServiceLocator {

    private lateinit var appContext: Context

    val tokenManager: TokenManager by lazy { TokenManager(appContext, "shop_prefs") }

    private val authRepository: AuthRepository by lazy {
        AuthRepositoryImpl(NetworkModule.provideAuthApiService(BuildConfig.BASE_URL, debug = BuildConfig.DEBUG))
    }

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    fun provideLoginViewModelFactory() = LoginViewModel.Factory(
        LoginUseCase(authRepository),
        tokenManager
    )

    fun provideForgotPasswordViewModelFactory() = ForgotPasswordViewModel.Factory(
        ForgotPasswordUseCase(authRepository),
        VerifyOtpUseCase(authRepository),
        ResetPasswordUseCase(authRepository)
    )

    fun provideRegisterViewModelFactory() = RegisterViewModel.Factory(
        RegisterUseCase(authRepository),
        SendOtpUseCase(authRepository)
    )
}

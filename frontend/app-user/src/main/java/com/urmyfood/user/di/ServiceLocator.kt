package com.urmyfood.user.di

import android.content.Context
import com.urmyfood.user.data.local.GuestSessionManager
import com.urmyfood.user.data.local.TokenManager
import com.urmyfood.user.domain.repository.GuestRepository
import com.urmyfood.user.data.remote.AuthApiService
import com.urmyfood.user.data.remote.RetrofitClient
import com.urmyfood.user.data.repository.AuthRepositoryImpl
import com.urmyfood.user.data.repository.PostRepositoryImpl
import com.urmyfood.user.domain.repository.AuthRepository
import com.urmyfood.user.domain.repository.GuestRepository
import com.urmyfood.user.domain.repository.PostRepository
import com.urmyfood.user.domain.usecase.*
import com.urmyfood.user.domain.usecase.GetPostsUseCase
import com.urmyfood.user.domain.usecase.LoginAsGuestUseCase
import com.urmyfood.user.presentation.auth.chooserole.ChooseRoleViewModel
import com.urmyfood.user.presentation.auth.forgotpass.ForgotPasswordViewModel
import com.urmyfood.user.presentation.auth.login.LoginViewModel
import com.urmyfood.user.presentation.auth.register.RegisterViewModel
import com.urmyfood.user.presentation.main.home.HomeViewModel
import com.urmyfood.user.presentation.main.search.SearchViewModel
import com.urmyfood.user.presentation.main.favorites.FavoritesViewModel
import com.urmyfood.user.presentation.main.profile.ProfileViewModel

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

    val guestSessionManager: GuestRepository by lazy {
        GuestSessionManager(applicationContext)
    }

    private val postApiService by lazy {
        RetrofitClient.postApiService
    }

    // ==================== DOMAIN LAYER ====================

    private val authRepository: AuthRepository by lazy {
        AuthRepositoryImpl(authApiService)
    }

    private val postRepository: PostRepository by lazy {
        PostRepositoryImpl(postApiService)
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
    val getPostsUseCase: GetPostsUseCase by lazy { GetPostsUseCase(postRepository) }
    val loginAsGuestUseCase: LoginAsGuestUseCase by lazy { LoginAsGuestUseCase(guestSessionManager) }

    // ==================== VIEW MODEL FACTORIES ====================

    fun provideLoginViewModelFactory(): LoginViewModel.Factory {
        return LoginViewModel.Factory(
            loginUseCase,
            loginWithGoogleUseCase,
            sendLoginOtpUseCase,
            loginWithOtpUseCase,
            loginAsGuestUseCase
        )
    }

    fun provideChooseRoleViewModelFactory(): ChooseRoleViewModel.Factory {
        return ChooseRoleViewModel.Factory(loginAsGuestUseCase)
    }

    fun provideRegisterViewModelFactory(): RegisterViewModel.Factory {
        return RegisterViewModel.Factory(
            registerUseCase
        )
    }

    fun provideForgotPasswordViewModelFactory(): ForgotPasswordViewModel.Factory {
        return ForgotPasswordViewModel.Factory(
            forgotPasswordUseCase,
            verifyOtpUseCase,
            resetPasswordUseCase
        )
    }

    fun provideHomeViewModelFactory(): HomeViewModel.Factory {
        return HomeViewModel.Factory(getPostsUseCase)
    }

    fun provideSearchViewModelFactory(): SearchViewModel.Factory {
        return SearchViewModel.Factory()
    }

    fun provideFavoritesViewModelFactory(): FavoritesViewModel.Factory {
        return FavoritesViewModel.Factory()
    }

    fun provideProfileViewModelFactory(): ProfileViewModel.Factory {
        return ProfileViewModel.Factory()
    }
}

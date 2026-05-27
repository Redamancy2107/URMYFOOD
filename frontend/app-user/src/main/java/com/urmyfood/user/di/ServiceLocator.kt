package com.urmyfood.user.di

import android.content.Context
import com.urmyfood.user.data.local.GuestSessionManager
import com.urmyfood.user.data.local.NotificationSettingsManager
import com.urmyfood.user.data.local.SearchHistoryManager
import com.urmyfood.user.data.local.TokenManager
import com.urmyfood.user.domain.repository.GuestRepository
import com.urmyfood.user.data.remote.AuthApiService
import com.urmyfood.user.data.remote.RetrofitClient
import com.urmyfood.user.data.remote.UserApiService
import com.urmyfood.user.data.remote.AddressApiService
import com.urmyfood.user.data.remote.VoucherApiService
import com.urmyfood.user.data.remote.CartApiService
import com.urmyfood.user.data.remote.OrderApiService
import com.urmyfood.user.data.repository.AuthRepositoryImpl
import com.urmyfood.user.data.repository.CartRepositoryImpl
import com.urmyfood.user.data.repository.OrderRepositoryImpl
import com.urmyfood.user.data.repository.PostRepositoryImpl
import com.urmyfood.user.data.repository.UserRepositoryImpl
import com.urmyfood.user.data.repository.AddressRepositoryImpl
import com.urmyfood.user.data.repository.VoucherRepositoryImpl
import com.urmyfood.user.domain.repository.AuthRepository
import com.urmyfood.user.domain.repository.CartRepository
import com.urmyfood.user.domain.repository.OrderRepository
import com.urmyfood.user.domain.repository.PostRepository
import com.urmyfood.user.domain.repository.SearchHistoryRepository
import com.urmyfood.user.domain.repository.UserRepository
import com.urmyfood.user.domain.repository.AddressRepository
import com.urmyfood.user.domain.repository.VoucherRepository
import com.urmyfood.user.domain.usecase.*
import com.urmyfood.user.domain.usecase.GetCommentsUseCase
import com.urmyfood.user.domain.usecase.GetPostsUseCase
import com.urmyfood.user.domain.usecase.PostCommentUseCase
import com.urmyfood.user.presentation.main.home.CommentViewModel
import com.urmyfood.user.domain.usecase.GetUserProfileUseCase
import com.urmyfood.user.domain.usecase.LoginAsGuestUseCase
import com.urmyfood.user.domain.usecase.SearchPostsUseCase
import com.urmyfood.user.domain.usecase.ToggleLikeUseCase
import com.urmyfood.user.presentation.auth.chooserole.ChooseRoleViewModel
import com.urmyfood.user.presentation.auth.forgotpass.ForgotPasswordViewModel
import com.urmyfood.user.presentation.auth.login.LoginViewModel
import com.urmyfood.user.presentation.auth.register.RegisterViewModel
import com.urmyfood.user.presentation.main.home.HomeViewModel
import com.urmyfood.user.presentation.main.cart.CartViewModel
import com.urmyfood.user.presentation.main.cart.CheckoutViewModel
import com.urmyfood.user.presentation.main.search.SearchViewModel
import com.urmyfood.user.presentation.main.favorites.FavoritesViewModel
import com.urmyfood.user.presentation.main.profile.ProfileViewModel
import com.urmyfood.user.presentation.main.profile.ProfileEditViewModel
import com.urmyfood.user.presentation.main.profile.ChangePasswordViewModel
import com.urmyfood.user.presentation.main.profile.AddressBookViewModel
import com.urmyfood.user.presentation.main.profile.AddressEditViewModel
import com.urmyfood.user.presentation.main.profile.VouchersViewModel
import com.urmyfood.user.presentation.main.profile.TermsPoliciesViewModel
import com.urmyfood.user.presentation.main.profile.OrderHistoryViewModel

/**
 * Manual Dependency Injection container (Service Locator pattern).
 * Provides singleton instances of all dependencies following Clean Architecture layers.
 */
object ServiceLocator {

    val postCommentEvent = androidx.lifecycle.MutableLiveData<String>()
    val postLikeEvent = androidx.lifecycle.MutableLiveData<Pair<String, Pair<Boolean, Int>>>()

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

    val favoritesManager: com.urmyfood.user.data.local.FavoritesManager by lazy {
        com.urmyfood.user.data.local.FavoritesManager(applicationContext, tokenManager)
    }

    val notificationSettingsManager: NotificationSettingsManager by lazy {
        NotificationSettingsManager(applicationContext)
    }

    private val searchHistoryRepository: SearchHistoryRepository by lazy {
        SearchHistoryManager(applicationContext)
    }

    private val postApiService by lazy {
        RetrofitClient.postApiService
    }

    private val userApiService: UserApiService by lazy {
        RetrofitClient.userApiService
    }

    private val addressApiService: AddressApiService by lazy {
        RetrofitClient.addressApiService
    }

    private val voucherApiService: VoucherApiService by lazy {
        RetrofitClient.voucherApiService
    }

    private val cartApiService: CartApiService by lazy {
        RetrofitClient.cartApiService
    }

    private val orderApiService: OrderApiService by lazy {
        RetrofitClient.orderApiService
    }

    // ==================== DOMAIN LAYER ====================

    private val authRepository: AuthRepository by lazy {
        AuthRepositoryImpl(authApiService)
    }

    private val postRepository: PostRepository by lazy {
        PostRepositoryImpl(postApiService)
    }

    private val userRepository: UserRepository by lazy {
        UserRepositoryImpl(userApiService)
    }

    private val addressRepository: AddressRepository by lazy {
        AddressRepositoryImpl(addressApiService)
    }

    private val voucherRepository: VoucherRepository by lazy {
        VoucherRepositoryImpl(voucherApiService)
    }

    private val cartRepository: CartRepository by lazy {
        CartRepositoryImpl(cartApiService)
    }

    private val orderRepository: OrderRepository by lazy {
        OrderRepositoryImpl(orderApiService)
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
    val getPostsUseCase: GetPostsUseCase by lazy { GetPostsUseCase(postRepository, tokenManager) }
    val getPostUseCase: GetPostUseCase by lazy { GetPostUseCase(postRepository, tokenManager) }
    val toggleLikeUseCase: ToggleLikeUseCase by lazy { ToggleLikeUseCase(postRepository, tokenManager) }
    val getCommentsUseCase: GetCommentsUseCase by lazy { GetCommentsUseCase(postRepository, tokenManager) }
    val postCommentUseCase: PostCommentUseCase by lazy { PostCommentUseCase(postRepository, tokenManager) }
    val searchPostsUseCase: SearchPostsUseCase by lazy { SearchPostsUseCase(postRepository, tokenManager) }
    val getSearchHistoryUseCase: GetSearchHistoryUseCase by lazy { GetSearchHistoryUseCase(searchHistoryRepository) }
    val addSearchHistoryUseCase: AddSearchHistoryUseCase by lazy { AddSearchHistoryUseCase(searchHistoryRepository) }
    val removeSearchHistoryUseCase: RemoveSearchHistoryUseCase by lazy { RemoveSearchHistoryUseCase(searchHistoryRepository) }
    val clearSearchHistoryUseCase: ClearSearchHistoryUseCase by lazy { ClearSearchHistoryUseCase(searchHistoryRepository) }
    val loginAsGuestUseCase: LoginAsGuestUseCase by lazy { LoginAsGuestUseCase(guestSessionManager) }
    val getUserProfileUseCase: GetUserProfileUseCase by lazy { GetUserProfileUseCase(userRepository, tokenManager) }
    val updateUserProfileUseCase: UpdateUserProfileUseCase by lazy { UpdateUserProfileUseCase(userRepository, tokenManager) }
    val changePasswordUseCase: ChangePasswordUseCase by lazy { ChangePasswordUseCase(userRepository, tokenManager) }

    // Address use cases
    val getAddressesUseCase: GetAddressesUseCase by lazy { GetAddressesUseCase(addressRepository, tokenManager) }
    val createAddressUseCase: CreateAddressUseCase by lazy { CreateAddressUseCase(addressRepository, tokenManager) }
    val updateAddressUseCase: UpdateAddressUseCase by lazy { UpdateAddressUseCase(addressRepository, tokenManager) }
    val deleteAddressUseCase: DeleteAddressUseCase by lazy { DeleteAddressUseCase(addressRepository, tokenManager) }
    val setDefaultAddressUseCase: SetDefaultAddressUseCase by lazy { SetDefaultAddressUseCase(addressRepository, tokenManager) }

    // Voucher use cases
    val getVouchersUseCase: GetVouchersUseCase by lazy { GetVouchersUseCase(voucherRepository) }
    val getCartUseCase: GetCartUseCase by lazy { GetCartUseCase(cartRepository, tokenManager) }
    val addToCartUseCase: AddToCartUseCase by lazy { AddToCartUseCase(cartRepository, tokenManager) }
    val updateCartItemUseCase: UpdateCartItemUseCase by lazy { UpdateCartItemUseCase(cartRepository, tokenManager) }
    val deleteCartItemUseCase: DeleteCartItemUseCase by lazy { DeleteCartItemUseCase(cartRepository, tokenManager) }
    val checkoutUseCase: CheckoutUseCase by lazy { CheckoutUseCase(orderRepository, tokenManager) }
    val getOrdersUseCase: GetOrdersUseCase by lazy { GetOrdersUseCase(orderRepository, tokenManager) }
    val cancelOrderUseCase: CancelOrderUseCase by lazy { CancelOrderUseCase(orderRepository, tokenManager) }

    // ==================== VIEW MODEL FACTORIES ====================

    fun provideLoginViewModelFactory(): LoginViewModel.Factory {
        return LoginViewModel.Factory(
            loginUseCase,
            loginWithGoogleUseCase,
            sendLoginOtpUseCase,
            loginWithOtpUseCase,
            loginAsGuestUseCase,
            tokenManager
        )
    }

    fun provideChooseRoleViewModelFactory(): ChooseRoleViewModel.Factory {
        return ChooseRoleViewModel.Factory(
            loginAsGuestUseCase,
            loginWithGoogleUseCase,
            tokenManager
        )
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
        return HomeViewModel.Factory(getPostsUseCase, toggleLikeUseCase)
    }

    fun provideSearchViewModelFactory(): SearchViewModel.Factory {
        return SearchViewModel.Factory(
            searchPostsUseCase,
            toggleLikeUseCase,
            getSearchHistoryUseCase,
            addSearchHistoryUseCase,
            removeSearchHistoryUseCase,
            clearSearchHistoryUseCase
        )
    }

    fun provideCommentViewModelFactory(): CommentViewModel.Factory {
        return CommentViewModel.Factory(getCommentsUseCase, postCommentUseCase)
    }
    fun provideCartViewModelFactory(): CartViewModel.Factory {
        return CartViewModel.Factory(getCartUseCase, updateCartItemUseCase, deleteCartItemUseCase)
    }

    fun provideCheckoutViewModelFactory(): CheckoutViewModel.Factory {
        return CheckoutViewModel.Factory(checkoutUseCase, getAddressesUseCase, getVouchersUseCase)
    }

    fun provideFavoritesViewModelFactory(): FavoritesViewModel.Factory {
        return FavoritesViewModel.Factory()
    }

    fun provideProfileViewModelFactory(): ProfileViewModel.Factory {
        return ProfileViewModel.Factory(
            tokenManager,
            guestSessionManager,
            getUserProfileUseCase
        )
    }

    fun provideProfileEditViewModelFactory(): ProfileEditViewModel.Factory {
        return ProfileEditViewModel.Factory(
            getUserProfileUseCase,
            updateUserProfileUseCase
        )
    }

    fun provideChangePasswordViewModelFactory(): ChangePasswordViewModel.Factory {
        return ChangePasswordViewModel.Factory(changePasswordUseCase)
    }

    fun provideAddressBookViewModelFactory(): AddressBookViewModel.Factory {
        return AddressBookViewModel.Factory(
            getAddressesUseCase,
            deleteAddressUseCase,
            setDefaultAddressUseCase
        )
    }

    fun provideAddressEditViewModelFactory(): AddressEditViewModel.Factory {
        return AddressEditViewModel.Factory(
            getAddressesUseCase,
            createAddressUseCase,
            updateAddressUseCase
        )
    }

    fun provideVouchersViewModelFactory(): VouchersViewModel.Factory {
        return VouchersViewModel.Factory(getVouchersUseCase)
    }

    fun provideOrderHistoryViewModelFactory(): OrderHistoryViewModel.Factory {
        return OrderHistoryViewModel.Factory(getOrdersUseCase)
    }

    fun provideTermsPoliciesViewModelFactory(): TermsPoliciesViewModel.Factory {
        return TermsPoliciesViewModel.Factory()
    }
}

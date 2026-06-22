package com.urmyfood.shop.di

import android.content.Context
import com.urmyfood.shop.BuildConfig
import com.urmyfood.shop.data.remote.OrderApiService
import com.urmyfood.shop.data.remote.ChatApiService
import com.urmyfood.shop.data.remote.PostApiService
import com.urmyfood.shop.data.remote.ShopVerificationApiService
import com.urmyfood.shop.data.remote.ShopProfileApiService
import com.urmyfood.shop.data.remote.ShopStatisticsApiService
import com.urmyfood.shop.data.remote.UserApiService
import com.urmyfood.shop.data.repository.OrderRepositoryImpl
import com.urmyfood.shop.data.repository.ChatRepositoryImpl
import com.urmyfood.shop.data.repository.PostRepositoryImpl
import com.urmyfood.shop.data.repository.ShopProfileRepositoryImpl
import com.urmyfood.shop.data.repository.ShopStatisticsRepositoryImpl
import com.urmyfood.shop.data.repository.ShopVerificationRepositoryImpl
import com.urmyfood.shop.data.repository.UserRepositoryImpl
import com.urmyfood.shop.domain.repository.OrderRepository
import com.urmyfood.shop.domain.repository.ChatRepository
import com.urmyfood.shop.domain.repository.PostRepository
import com.urmyfood.shop.domain.repository.ShopProfileRepository
import com.urmyfood.shop.domain.repository.ShopStatisticsRepository
import com.urmyfood.shop.domain.repository.ShopVerificationRepository
import com.urmyfood.shop.domain.repository.UserRepository
import com.urmyfood.shop.domain.usecase.AddCommentUseCase
import com.urmyfood.shop.domain.usecase.ChangePasswordUseCase
import com.urmyfood.shop.domain.usecase.GetShopOrderDetailUseCase
import com.urmyfood.shop.domain.usecase.GetShopOrdersUseCase
import com.urmyfood.shop.domain.usecase.CreatePostUseCase
import com.urmyfood.shop.domain.usecase.DeletePostUseCase
import com.urmyfood.shop.domain.usecase.GetChatSessionsUseCase
import com.urmyfood.shop.domain.usecase.GetMessagesUseCase
import com.urmyfood.shop.domain.usecase.GetMyPostsUseCase
import com.urmyfood.shop.domain.usecase.GetPostByIdUseCase
import com.urmyfood.shop.domain.usecase.GetPostCommentsUseCase
import com.urmyfood.shop.domain.usecase.GetShopProfileUseCase
import com.urmyfood.shop.domain.usecase.GetShopStatisticsUseCase
import com.urmyfood.shop.domain.usecase.MarkAsReadUseCase
import com.urmyfood.shop.domain.usecase.SendMessageUseCase
import com.urmyfood.shop.domain.usecase.SubmitShopVerificationUseCase
import com.urmyfood.shop.domain.usecase.UpdateOrderStatusUseCase
import com.urmyfood.shop.domain.usecase.TogglePostStatusUseCase
import com.urmyfood.shop.domain.usecase.UpdatePostUseCase
import com.urmyfood.shop.domain.usecase.UpdateShopProfileUseCase
import com.urmyfood.shop.domain.usecase.UploadChatImageUseCase
import com.urmyfood.shop.domain.usecase.UploadPostImageUseCase
import com.urmyfood.shop.domain.usecase.UploadShopProfileImageUseCase
import com.urmyfood.shared.data.local.TokenManager
import com.urmyfood.shared.data.remote.NetworkModule
import com.urmyfood.shared.data.remote.StompClient
import com.urmyfood.shared.data.repository.AuthRepositoryImpl
import com.urmyfood.shared.domain.repository.AuthRepository
import com.urmyfood.shared.domain.usecase.ForgotPasswordUseCase
import com.urmyfood.shared.domain.usecase.LoginUseCase
import com.urmyfood.shared.domain.usecase.RegisterUseCase
import com.urmyfood.shared.domain.usecase.ResetPasswordUseCase
import com.urmyfood.shared.domain.usecase.SendOtpUseCase
import com.urmyfood.shared.domain.usecase.VerifyOtpUseCase
import com.urmyfood.shop.data.local.NotificationSettingsManager
import com.urmyfood.shop.presentation.auth.forgotpass.ForgotPasswordViewModel
import com.urmyfood.shop.presentation.auth.login.LoginViewModel
import com.urmyfood.shop.presentation.auth.registration.ShopRegistrationFlowViewModel
import com.urmyfood.shop.presentation.auth.register.RegisterViewModel
import com.urmyfood.shop.presentation.main.account.AccountViewModel
import com.urmyfood.shop.presentation.main.account.ChangePasswordViewModel
import com.urmyfood.shop.presentation.main.account.ShopProfileEditViewModel
import com.urmyfood.shop.presentation.main.account.stats.StatisticsViewModel
import com.urmyfood.shop.presentation.main.orders.OrdersViewModel
import com.urmyfood.shop.presentation.main.orders.detail.OrderDetailViewModel
import com.urmyfood.shop.presentation.main.chat.ChatDetailViewModel
import com.urmyfood.shop.presentation.main.chat.ChatViewModel
import com.urmyfood.shop.presentation.main.posts.CommentViewModel
import com.urmyfood.shop.presentation.main.posts.CreatePostViewModel
import com.urmyfood.shop.presentation.main.posts.PostDetailViewModel
import com.urmyfood.shop.presentation.main.posts.PostsViewModel
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

object ServiceLocator {

    private lateinit var appContext: Context

    @Volatile
    var cachedShopProfile: com.urmyfood.shop.domain.model.ShopProfile? = null

    val tokenManager: TokenManager by lazy { TokenManager(appContext, "shop_prefs") }

    val notificationSettingsManager: NotificationSettingsManager by lazy {
        NotificationSettingsManager(appContext)
    }

    private val authRepository: AuthRepository by lazy {
        AuthRepositoryImpl(NetworkModule.provideAuthApiService(BuildConfig.BASE_URL, debug = BuildConfig.DEBUG))
    }

    private val shopVerificationRepository: ShopVerificationRepository by lazy {
        val api = NetworkModule.buildRetrofit(BuildConfig.BASE_URL, debug = BuildConfig.DEBUG)
            .create(ShopVerificationApiService::class.java)
        ShopVerificationRepositoryImpl(api)
    }

    private val shopProfileRepository: ShopProfileRepository by lazy {
        val api = NetworkModule.buildRetrofit(BuildConfig.BASE_URL, debug = BuildConfig.DEBUG)
            .create(ShopProfileApiService::class.java)
        ShopProfileRepositoryImpl(api)
    }

    private val shopStatisticsRepository: ShopStatisticsRepository by lazy {
        val api = NetworkModule.buildRetrofit(BuildConfig.BASE_URL, debug = BuildConfig.DEBUG)
            .create(ShopStatisticsApiService::class.java)
        ShopStatisticsRepositoryImpl(api)
    }

    private val userRepository: UserRepository by lazy {
        val api = NetworkModule.buildRetrofit(BuildConfig.BASE_URL, debug = BuildConfig.DEBUG)
            .create(UserApiService::class.java)
        UserRepositoryImpl(api)
    }

    private val orderRepository: OrderRepository by lazy {
        val api = NetworkModule.buildRetrofit(BuildConfig.BASE_URL, debug = BuildConfig.DEBUG)
            .create(OrderApiService::class.java)
        OrderRepositoryImpl(api)
    }
    
    private val postRepository: PostRepository by lazy {
        val api = NetworkModule.buildRetrofit(BuildConfig.BASE_URL, debug = BuildConfig.DEBUG)
            .create(PostApiService::class.java)
        PostRepositoryImpl(api)
    }

    private val okHttpClient: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
                    else HttpLoggingInterceptor.Level.NONE
        }
        OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    val stompClient: StompClient by lazy { StompClient(okHttpClient) }

    private val chatRepository: ChatRepository by lazy {
        val api = NetworkModule.buildRetrofit(BuildConfig.BASE_URL, debug = BuildConfig.DEBUG)
            .create(ChatApiService::class.java)
        ChatRepositoryImpl(api, stompClient)
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
        SendOtpUseCase(authRepository),
        tokenManager
    )

    fun provideShopRegistrationFlowViewModelFactory() = ShopRegistrationFlowViewModel.Factory(
        SubmitShopVerificationUseCase(shopVerificationRepository, tokenManager)
    )

    fun provideGetShopProfileUseCase() = GetShopProfileUseCase(shopProfileRepository, tokenManager)

    fun provideAccountViewModelFactory() = AccountViewModel.Factory(
        GetShopProfileUseCase(shopProfileRepository, tokenManager),
        tokenManager
    )

    fun provideShopProfileEditViewModelFactory() = ShopProfileEditViewModel.Factory(
        GetShopProfileUseCase(shopProfileRepository, tokenManager),
        UpdateShopProfileUseCase(shopProfileRepository, tokenManager),
        UploadShopProfileImageUseCase(shopProfileRepository, tokenManager)
    )

    fun provideChangePasswordViewModelFactory() = ChangePasswordViewModel.Factory(
        ChangePasswordUseCase(userRepository, tokenManager)
    )

    fun provideStatisticsViewModelFactory() = StatisticsViewModel.Factory(
        GetShopStatisticsUseCase(shopStatisticsRepository, tokenManager)
    )

    fun provideOrdersViewModelFactory() = OrdersViewModel.Factory(
        GetShopOrdersUseCase(orderRepository, tokenManager),
        UpdateOrderStatusUseCase(orderRepository, tokenManager)
    )

    fun provideOrderDetailViewModelFactory() = OrderDetailViewModel.Factory(
        GetShopOrderDetailUseCase(orderRepository, tokenManager),
        UpdateOrderStatusUseCase(orderRepository, tokenManager)
    )
    fun providePostsViewModelFactory() = PostsViewModel.Factory(
        GetMyPostsUseCase(postRepository, tokenManager),
        DeletePostUseCase(postRepository, tokenManager),
        TogglePostStatusUseCase(postRepository, tokenManager)
    )

    fun provideCreatePostViewModelFactory(postId: String? = null) = CreatePostViewModel.Factory(
        postId,
        GetPostByIdUseCase(postRepository, tokenManager),
        CreatePostUseCase(postRepository, tokenManager),
        UpdatePostUseCase(postRepository, tokenManager),
        UploadPostImageUseCase(postRepository, tokenManager)
    )

    fun providePostDetailViewModelFactory(postId: String) = PostDetailViewModel.Factory(
        postId,
        GetPostByIdUseCase(postRepository, tokenManager),
        TogglePostStatusUseCase(postRepository, tokenManager),
        DeletePostUseCase(postRepository, tokenManager)
    )

    fun provideCommentViewModelFactory() = CommentViewModel.Factory(
        GetPostCommentsUseCase(postRepository, tokenManager),
        AddCommentUseCase(postRepository, tokenManager)
    )

    fun provideChatViewModelFactory() = ChatViewModel.Factory(
        GetChatSessionsUseCase(chatRepository, tokenManager)
    )

    fun provideChatDetailViewModelFactory(sessionId: Long): ChatDetailViewModel.Factory {
        val token = tokenManager.getAccessToken() ?: ""
        val wsUrl = StompClient.toWsUrl(BuildConfig.BASE_URL)
        return ChatDetailViewModel.Factory(
            sessionId = sessionId,
            wsUrl = wsUrl,
            accessToken = token,
            chatRepository = chatRepository,
            getMessagesUseCase = GetMessagesUseCase(chatRepository, tokenManager),
            sendMessageUseCase = SendMessageUseCase(chatRepository),
            markAsReadUseCase = MarkAsReadUseCase(chatRepository, tokenManager),
            uploadChatImageUseCase = UploadChatImageUseCase(chatRepository, tokenManager)
        )
    }
}

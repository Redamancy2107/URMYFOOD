package com.urmyfood.user.data.remote

import com.urmyfood.user.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private val BASE_URL get() = BuildConfig.BASE_URL

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
                else HttpLoggingInterceptor.Level.NONE
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val authApiService: AuthApiService = retrofit.create(AuthApiService::class.java)
    val postApiService: PostApiService = retrofit.create(PostApiService::class.java)
    val userApiService: UserApiService = retrofit.create(UserApiService::class.java)
    val addressApiService: AddressApiService = retrofit.create(AddressApiService::class.java)
    val voucherApiService: VoucherApiService = retrofit.create(VoucherApiService::class.java)
    val cartApiService: CartApiService = retrofit.create(CartApiService::class.java)
    val orderApiService: OrderApiService = retrofit.create(OrderApiService::class.java)
    val chatApiService: ChatApiService = retrofit.create(ChatApiService::class.java)
}

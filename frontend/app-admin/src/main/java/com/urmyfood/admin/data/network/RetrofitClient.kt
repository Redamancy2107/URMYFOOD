package com.urmyfood.admin.data.network

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // 10.0.2.2 is the localhost alias for Android Emulator
    private const val BASE_URL = "http://10.0.2.2:8080/"

    private val client = OkHttpClient.Builder().build()

    val api: AdminApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AdminApi::class.java)
    }
}

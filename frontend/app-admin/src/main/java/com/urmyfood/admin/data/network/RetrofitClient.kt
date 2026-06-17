package com.urmyfood.admin.data.network

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private val BASE_URL get() = com.urmyfood.admin.BuildConfig.BASE_URL

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

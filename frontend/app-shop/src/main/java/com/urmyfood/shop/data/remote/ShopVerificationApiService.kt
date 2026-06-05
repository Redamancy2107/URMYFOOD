package com.urmyfood.shop.data.remote

import com.urmyfood.shared.data.model.ApiResponse
import com.urmyfood.shop.data.model.ShopVerificationRequest
import com.urmyfood.shop.data.model.ShopVerificationResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface ShopVerificationApiService {

    @POST("api/v1/shops/me/verification")
    suspend fun submitVerification(
        @Header("Authorization") authorization: String,
        @Body request: ShopVerificationRequest
    ): Response<ApiResponse<ShopVerificationResponse>>

    @GET("api/v1/shops/me/verification")
    suspend fun getVerification(
        @Header("Authorization") authorization: String
    ): Response<ApiResponse<ShopVerificationResponse>>
}

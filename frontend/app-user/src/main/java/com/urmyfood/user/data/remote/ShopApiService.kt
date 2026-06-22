package com.urmyfood.user.data.remote

import com.urmyfood.user.data.model.ApiResponse
import com.urmyfood.user.data.model.ShopFollowResponse
import com.urmyfood.user.data.model.ShopProfileResponse
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface ShopApiService {
    @GET("api/v1/shops/{shopId}/profile")
    suspend fun getProfile(
        @Header("Authorization") token: String,
        @Path("shopId") shopId: Long
    ): Response<ApiResponse<ShopProfileResponse>>

    @GET("api/v1/shops/{shopId}/follow")
    suspend fun getFollowState(
        @Header("Authorization") token: String,
        @Path("shopId") shopId: Long
    ): Response<ApiResponse<ShopFollowResponse>>

    @POST("api/v1/shops/{shopId}/follow")
    suspend fun follow(
        @Header("Authorization") token: String,
        @Path("shopId") shopId: Long
    ): Response<ApiResponse<ShopFollowResponse>>

    @DELETE("api/v1/shops/{shopId}/follow")
    suspend fun unfollow(
        @Header("Authorization") token: String,
        @Path("shopId") shopId: Long
    ): Response<ApiResponse<ShopFollowResponse>>
}

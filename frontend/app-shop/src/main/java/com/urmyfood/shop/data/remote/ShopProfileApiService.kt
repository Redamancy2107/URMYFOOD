package com.urmyfood.shop.data.remote

import com.urmyfood.shared.data.model.ApiResponse
import com.urmyfood.shop.data.model.ProfileImageUploadResponse
import com.urmyfood.shop.data.model.ShopProfileRequest
import com.urmyfood.shop.data.model.ShopProfileResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.PUT
import retrofit2.http.Query

interface ShopProfileApiService {
    @GET("api/v1/shops/me/profile")
    suspend fun getMyProfile(
        @Header("Authorization") token: String
    ): Response<ApiResponse<ShopProfileResponse>>

    @PUT("api/v1/shops/me/profile")
    suspend fun updateMyProfile(
        @Header("Authorization") token: String,
        @Body request: ShopProfileRequest
    ): Response<ApiResponse<ShopProfileResponse>>

    @Multipart
    @POST("api/v1/shops/me/profile/images")
    suspend fun uploadProfileImage(
        @Header("Authorization") token: String,
        @Query("type") type: String,
        @Part file: MultipartBody.Part
    ): Response<ApiResponse<ProfileImageUploadResponse>>
}

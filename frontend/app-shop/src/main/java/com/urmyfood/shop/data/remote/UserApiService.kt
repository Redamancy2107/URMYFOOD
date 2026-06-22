package com.urmyfood.shop.data.remote

import com.urmyfood.shared.data.model.ApiResponse
import com.urmyfood.shop.data.model.AccountProfileResponse
import com.urmyfood.shop.data.model.ChangePasswordRequest
import com.urmyfood.shop.data.model.UpdateProfileRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PUT

interface UserApiService {
    @GET("api/v1/accounts/me")
    suspend fun getMyProfile(
        @Header("Authorization") token: String
    ): Response<ApiResponse<AccountProfileResponse>>

    @PUT("api/v1/accounts/me")
    suspend fun updateProfile(
        @Header("Authorization") token: String,
        @Body request: UpdateProfileRequest
    ): Response<ApiResponse<AccountProfileResponse>>

    @PUT("api/v1/accounts/me/change-password")
    suspend fun changePassword(
        @Header("Authorization") token: String,
        @Body request: ChangePasswordRequest
    ): Response<ApiResponse<Void>>
}

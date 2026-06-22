package com.urmyfood.user.data.remote

import com.urmyfood.user.data.model.AccountProfileResponse
import com.urmyfood.user.data.model.ApiResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Body

interface UserApiService {
    @GET("api/v1/accounts/me")
    suspend fun getMyProfile(
        @Header("Authorization") token: String
    ): Response<ApiResponse<AccountProfileResponse>>

    @PUT("api/v1/accounts/me")
    suspend fun updateProfile(
        @Header("Authorization") token: String,
        @Body request: com.urmyfood.user.data.model.UpdateProfileRequest
    ): Response<ApiResponse<AccountProfileResponse>>

    @Multipart
    @POST("api/v1/accounts/me/avatar")
    suspend fun uploadAvatar(
        @Header("Authorization") token: String,
        @Part file: MultipartBody.Part
    ): Response<ApiResponse<AccountProfileResponse>>

    @PUT("api/v1/accounts/me/change-password")
    suspend fun changePassword(
        @Header("Authorization") token: String,
        @Body request: com.urmyfood.user.data.model.ChangePasswordRequest
    ): Response<ApiResponse<Void>>
}

package com.urmyfood.user.data.remote

import com.urmyfood.user.data.model.AccountProfileResponse
import com.urmyfood.user.data.model.ApiResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header

interface UserApiService {
    @GET("api/v1/accounts/me")
    suspend fun getMyProfile(
        @Header("Authorization") token: String
    ): Response<ApiResponse<AccountProfileResponse>>
}

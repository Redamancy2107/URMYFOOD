package com.urmyfood.admin.data.network

import com.urmyfood.admin.data.model.AdminProfile
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Query

interface AdminApi {
    @GET("api/admin")
    suspend fun getAdminProfile(
        @Query("account_id") accountId: Long
    ): Response<AdminProfile>

    @PATCH("api/admin")
    suspend fun updateAdminProfile(
        @Query("account_id") accountId: Long,
        @Body profileUpdates: Map<String, String>
    ): Response<AdminProfile>
}

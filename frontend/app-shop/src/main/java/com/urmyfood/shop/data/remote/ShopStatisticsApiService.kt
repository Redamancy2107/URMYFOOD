package com.urmyfood.shop.data.remote

import com.urmyfood.shared.data.model.ApiResponse
import com.urmyfood.shop.data.model.ShopStatisticsResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface ShopStatisticsApiService {
    @GET("api/v1/shops/me/statistics")
    suspend fun getStatistics(
        @Header("Authorization") token: String,
        @Query("period") period: String,
        @Query("date") date: String? = null,
        @Query("month") month: String? = null,
        @Query("year") year: String? = null
    ): Response<ApiResponse<ShopStatisticsResponse>>
}

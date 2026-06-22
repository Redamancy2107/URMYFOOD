package com.urmyfood.user.data.remote

import com.urmyfood.user.data.model.ApiResponse
import com.urmyfood.user.data.model.VoucherResponse
import retrofit2.Response
import retrofit2.http.GET

interface VoucherApiService {

    @GET("api/v1/vouchers")
    suspend fun getActiveVouchers(): Response<ApiResponse<List<VoucherResponse>>>
}

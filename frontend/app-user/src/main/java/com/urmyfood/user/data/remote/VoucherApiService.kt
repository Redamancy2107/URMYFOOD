package com.urmyfood.user.data.remote

import com.urmyfood.user.data.model.ApiResponse
import com.urmyfood.user.data.model.SavedVoucherResponse
import com.urmyfood.user.data.model.VoucherResponse
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface VoucherApiService {

    @GET("api/v1/vouchers")
    suspend fun getActiveVouchers(
        @Header("Authorization") token: String?
    ): Response<ApiResponse<List<VoucherResponse>>>

    @GET("api/v1/vouchers/saved")
    suspend fun getSavedVouchers(
        @Header("Authorization") token: String
    ): Response<ApiResponse<List<VoucherResponse>>>

    @POST("api/v1/vouchers/{voucherId}/saved")
    suspend fun saveVoucher(
        @Header("Authorization") token: String,
        @Path("voucherId") voucherId: Long
    ): Response<ApiResponse<SavedVoucherResponse>>

    @DELETE("api/v1/vouchers/{voucherId}/saved")
    suspend fun unsaveVoucher(
        @Header("Authorization") token: String,
        @Path("voucherId") voucherId: Long
    ): Response<ApiResponse<SavedVoucherResponse>>
}

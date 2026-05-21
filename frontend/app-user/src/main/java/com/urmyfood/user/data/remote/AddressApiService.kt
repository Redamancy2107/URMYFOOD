package com.urmyfood.user.data.remote

import com.urmyfood.user.data.model.AddressRequest
import com.urmyfood.user.data.model.AddressResponse
import com.urmyfood.user.data.model.ApiResponse
import retrofit2.Response
import retrofit2.http.*

interface AddressApiService {

    @GET("api/v1/addresses")
    suspend fun getMyAddresses(
        @Header("Authorization") token: String
    ): Response<ApiResponse<List<AddressResponse>>>

    @POST("api/v1/addresses")
    suspend fun createAddress(
        @Header("Authorization") token: String,
        @Body request: AddressRequest
    ): Response<ApiResponse<AddressResponse>>

    @PUT("api/v1/addresses/{id}")
    suspend fun updateAddress(
        @Header("Authorization") token: String,
        @Path("id") id: Long,
        @Body request: AddressRequest
    ): Response<ApiResponse<AddressResponse>>

    @DELETE("api/v1/addresses/{id}")
    suspend fun deleteAddress(
        @Header("Authorization") token: String,
        @Path("id") id: Long
    ): Response<ApiResponse<Void>>

    @PUT("api/v1/addresses/{id}/default")
    suspend fun setDefault(
        @Header("Authorization") token: String,
        @Path("id") id: Long
    ): Response<ApiResponse<AddressResponse>>
}

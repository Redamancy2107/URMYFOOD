package com.urmyfood.user.data.remote

import com.urmyfood.user.data.model.AddCartItemRequest
import com.urmyfood.user.data.model.ApiResponse
import com.urmyfood.user.data.model.CartResponse
import com.urmyfood.user.data.model.UpdateCartItemRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface CartApiService {
    @GET("api/v1/cart")
    suspend fun getCart(
        @Header("Authorization") token: String
    ): Response<ApiResponse<CartResponse>>

    @POST("api/v1/cart")
    suspend fun addItem(
        @Header("Authorization") token: String,
        @Body request: AddCartItemRequest
    ): Response<ApiResponse<CartResponse>>

    @PUT("api/v1/cart/{itemId}")
    suspend fun updateItem(
        @Header("Authorization") token: String,
        @Path("itemId") itemId: String,
        @Body request: UpdateCartItemRequest
    ): Response<ApiResponse<CartResponse>>

    @DELETE("api/v1/cart/{itemId}")
    suspend fun deleteItem(
        @Header("Authorization") token: String,
        @Path("itemId") itemId: String
    ): Response<ApiResponse<Void>>
}

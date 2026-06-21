package com.urmyfood.shop.data.remote

import com.urmyfood.shared.data.model.ApiResponse
import com.urmyfood.shop.data.model.ChatImageUrlDto
import com.urmyfood.shop.data.model.ChatMessageDto
import com.urmyfood.shop.data.model.ChatSessionDto
import com.urmyfood.shop.data.model.GetOrCreateSessionRequest
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface ChatApiService {

    @GET("api/v1/chat/sessions")
    suspend fun getSessions(
        @Header("Authorization") token: String
    ): Response<ApiResponse<List<ChatSessionDto>>>

    @POST("api/v1/chat/sessions")
    suspend fun getOrCreateSession(
        @Header("Authorization") token: String,
        @Body request: GetOrCreateSessionRequest
    ): Response<ApiResponse<ChatSessionDto>>

    @GET("api/v1/chat/sessions/{sessionId}/messages")
    suspend fun getMessages(
        @Header("Authorization") token: String,
        @Path("sessionId") sessionId: Long
    ): Response<ApiResponse<List<ChatMessageDto>>>

    @PUT("api/v1/chat/sessions/{sessionId}/read")
    suspend fun markAsRead(
        @Header("Authorization") token: String,
        @Path("sessionId") sessionId: Long
    ): Response<ApiResponse<Void>>

    @Multipart
    @POST("api/v1/chat/sessions/{sessionId}/upload-image")
    suspend fun uploadChatImage(
        @Header("Authorization") token: String,
        @Path("sessionId") sessionId: Long,
        @Part file: MultipartBody.Part
    ): Response<ApiResponse<ChatImageUrlDto>>
}

package com.urmyfood.shop.data.repository

import com.google.gson.Gson
import com.urmyfood.shared.data.model.ApiResponse
import com.urmyfood.shared.data.remote.StompClient
import com.urmyfood.shared.domain.model.ChatMessage
import com.urmyfood.shared.domain.model.ChatSession
import com.urmyfood.shared.domain.model.Result
import com.urmyfood.shop.data.model.GetOrCreateSessionRequest
import com.urmyfood.shop.data.model.toDomain
import com.urmyfood.shop.data.remote.ChatApiService
import com.urmyfood.shop.domain.repository.ChatRepository
import okhttp3.MultipartBody
import retrofit2.Response

class ChatRepositoryImpl(
    private val api: ChatApiService,
    private val stompClient: StompClient
) : ChatRepository {

    private val gson = Gson()

    private fun <T> Response<T>.errorMessage(): String {
        return try {
            errorBody()?.charStream()?.use { reader ->
                gson.fromJson(reader, ApiResponse::class.java)?.message
            } ?: "Lỗi máy chủ: ${code()}"
        } catch (_: Exception) {
            "Lỗi máy chủ: ${code()}"
        }
    }

    override suspend fun getSessions(token: String): Result<List<ChatSession>> = try {
        val response = api.getSessions(token)
        val body = response.body()
        if (response.isSuccessful && body?.success == true)
            Result.Success(body.data?.map { it.toDomain() } ?: emptyList())
        else
            Result.Error(body?.message ?: response.errorMessage())
    } catch (e: Exception) {
        Result.Error(e.message ?: "Lỗi kết nối")
    }

    override suspend fun getOrCreateSession(token: String, shopId: Long): Result<ChatSession> = try {
        val response = api.getOrCreateSession(token, GetOrCreateSessionRequest(shopId))
        val body = response.body()
        val sessionData = body?.data
        if (response.isSuccessful && body?.success == true && sessionData != null)
            Result.Success(sessionData.toDomain())
        else
            Result.Error(body?.message ?: response.errorMessage())
    } catch (e: Exception) {
        Result.Error(e.message ?: "Lỗi kết nối")
    }

    override suspend fun getMessages(token: String, sessionId: Long): Result<List<ChatMessage>> = try {
        val response = api.getMessages(token, sessionId)
        val body = response.body()
        if (response.isSuccessful && body?.success == true)
            Result.Success(body.data?.map { it.toDomain() } ?: emptyList())
        else
            Result.Error(body?.message ?: response.errorMessage())
    } catch (e: Exception) {
        Result.Error(e.message ?: "Lỗi kết nối")
    }

    override suspend fun markAsRead(token: String, sessionId: Long): Result<Unit> = try {
        val response = api.markAsRead(token, sessionId)
        val body = response.body()
        if (response.isSuccessful && body?.success == true)
            Result.Success(Unit)
        else
            Result.Error(body?.message ?: response.errorMessage())
    } catch (e: Exception) {
        Result.Error(e.message ?: "Lỗi kết nối")
    }

    override fun connectWebSocket(wsUrl: String, token: String) {
        stompClient.connect(wsUrl, token)
    }

    override fun subscribeToSession(sessionId: Long, onMessage: (String) -> Unit) {
        stompClient.subscribe("/topic/chat/$sessionId", onMessage)
    }

    override suspend fun uploadImage(token: String, sessionId: Long, file: MultipartBody.Part): Result<String> = try {
        val response = api.uploadChatImage(token, sessionId, file)
        val body = response.body()
        val imageData = body?.data
        if (response.isSuccessful && body?.success == true && imageData != null)
            Result.Success(imageData.imageUrl)
        else
            Result.Error(body?.message ?: response.errorMessage(), response.code())
    } catch (e: Exception) {
        Result.Error(e.message ?: "Lỗi kết nối")
    }

    override fun sendMessageViaWebSocket(sessionId: Long, content: String) {
        val payload = gson.toJson(mapOf("sessionId" to sessionId, "content" to content, "messageType" to "TEXT"))
        stompClient.send("/app/chat.send", payload)
    }

    override fun sendImageViaWebSocket(sessionId: Long, imageUrl: String) {
        val payload = gson.toJson(mapOf("sessionId" to sessionId, "content" to "", "messageType" to "IMAGE", "imageUrl" to imageUrl))
        stompClient.send("/app/chat.send", payload)
    }

    override fun disconnectWebSocket() {
        stompClient.disconnect()
    }
}

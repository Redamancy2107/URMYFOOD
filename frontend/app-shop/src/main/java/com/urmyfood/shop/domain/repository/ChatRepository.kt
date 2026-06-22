package com.urmyfood.shop.domain.repository

import com.urmyfood.shared.domain.model.ChatMessage
import com.urmyfood.shared.domain.model.ChatSession
import com.urmyfood.shared.domain.model.Result
import okhttp3.MultipartBody

interface ChatRepository {
    suspend fun getSessions(token: String): Result<List<ChatSession>>
    suspend fun getOrCreateSession(token: String, shopId: Long): Result<ChatSession>
    suspend fun getMessages(token: String, sessionId: Long): Result<List<ChatMessage>>
    suspend fun markAsRead(token: String, sessionId: Long): Result<Unit>
    suspend fun uploadImage(token: String, sessionId: Long, file: MultipartBody.Part): Result<String>
    fun connectWebSocket(wsUrl: String, token: String)
    fun subscribeToSession(sessionId: Long, onMessage: (String) -> Unit)
    fun sendMessageViaWebSocket(sessionId: Long, content: String)
    fun sendImageViaWebSocket(sessionId: Long, imageUrl: String)
    fun disconnectWebSocket()
}

package com.urmyfood.user.data.model

import com.google.gson.annotations.SerializedName
import com.urmyfood.shared.domain.model.ChatMessage
import com.urmyfood.shared.domain.model.ChatSession

data class ChatSessionDto(
    @SerializedName("id") val id: Long,
    @SerializedName("shopId") val shopId: Long,
    @SerializedName("shopName") val shopName: String,
    @SerializedName("shopAvatarUrl") val shopAvatarUrl: String?,
    @SerializedName("userId") val userId: Long,
    @SerializedName("customerName") val customerName: String,
    @SerializedName("customerAvatarUrl") val customerAvatarUrl: String?,
    @SerializedName("lastMessage") val lastMessage: String?,
    @SerializedName("lastMessageAt") val lastMessageAt: String?,
    @SerializedName("unreadCount") val unreadCount: Int
)

data class ChatMessageDto(
    @SerializedName("id") val id: Long,
    @SerializedName("sessionId") val sessionId: Long,
    @SerializedName("senderId") val senderId: Long,
    @SerializedName("senderRole") val senderRole: String,
    @SerializedName("content") val content: String,
    @SerializedName("read") val isRead: Boolean,
    @SerializedName("sentAt") val sentAt: String?
)

data class GetOrCreateSessionRequest(
    @SerializedName("shopId") val shopId: Long
)

fun ChatSessionDto.toDomain() = ChatSession(
    id = id,
    shopId = shopId,
    shopName = shopName,
    shopAvatarUrl = shopAvatarUrl,
    userId = userId,
    customerName = customerName,
    customerAvatarUrl = customerAvatarUrl,
    lastMessage = lastMessage,
    lastMessageAt = lastMessageAt,
    unreadCount = unreadCount
)

fun ChatMessageDto.toDomain() = ChatMessage(
    id = id,
    sessionId = sessionId,
    senderId = senderId,
    senderRole = senderRole,
    content = content,
    isRead = isRead,
    sentAt = sentAt ?: ""
)

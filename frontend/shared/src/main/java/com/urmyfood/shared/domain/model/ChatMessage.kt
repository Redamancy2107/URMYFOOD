package com.urmyfood.shared.domain.model

data class ChatMessage(
    val id: Long,
    val sessionId: Long,
    val senderId: Long,
    val senderRole: String,
    val messageType: String = "TEXT",
    val content: String,
    val imageUrl: String? = null,
    val isRead: Boolean,
    val sentAt: String
)

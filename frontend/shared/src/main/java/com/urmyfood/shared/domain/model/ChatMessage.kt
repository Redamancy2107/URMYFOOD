package com.urmyfood.shared.domain.model

data class ChatMessage(
    val id: Long,
    val sessionId: Long,
    val senderId: Long,
    val senderRole: String,
    val content: String,
    val isRead: Boolean,
    val sentAt: String
)

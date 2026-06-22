package com.urmyfood.shared.domain.model

data class ChatSession(
    val id: Long,
    val shopId: Long,
    val shopName: String,
    val shopAvatarUrl: String?,
    val userId: Long,
    val customerName: String,
    val customerAvatarUrl: String?,
    val lastMessage: String?,
    val lastMessageAt: String?,
    val unreadCount: Int
)

package com.urmyfood.user.presentation.model

data class Category(
    val id: Int,
    val name: String,
    val icon: String // Emoji or Drawable ID
)

data class ChatSession(
    val id: Int,
    val name: String,
    val avatar: Int,
    val lastMessage: String,
    val time: String,
    val unreadCount: Int
)

data class Message(
    val id: Int,
    val content: String,
    val time: String,
    val isSent: Boolean,
    val isOrderCard: Boolean = false
)

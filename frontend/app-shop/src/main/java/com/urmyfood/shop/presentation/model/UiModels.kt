package com.urmyfood.shop.presentation.model

data class ChatSession(
    val id: Int,
    val name: String,
    val avatar: Int,
    val lastMessage: String,
    val time: String,
    val unreadCount: Int
)

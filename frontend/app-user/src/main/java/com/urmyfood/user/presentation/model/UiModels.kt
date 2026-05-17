package com.urmyfood.user.presentation.model

data class Category(
    val id: Int,
    val name: String,
    val icon: String // Emoji or Drawable ID
)

data class Post(
    val id: Int,
    val shopName: String,
    val shopAvatar: Int,
    val time: String,
    val description: String,
    val image: Int,
    val badge: String?,
    val price: String,
    val oldPrice: String? = null,
    var likeCount: Int,
    val commentCount: Int,
    var isLiked: Boolean = false,
    var isFlashSale: Boolean = false
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

data class Friend(
    val id: Int,
    val name: String,
    val avatarUrl: String?,
    var isSent: Boolean = false
)

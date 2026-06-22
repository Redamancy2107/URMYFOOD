package com.urmyfood.shop.domain.model

data class OrderItem(
    val orderItemId: String,
    val postId: String,
    val quantity: Int,
    val priceAtPurchase: Double,
    val dishNameSnapshot: String,
    val imageUrlSnapshot: String?
)

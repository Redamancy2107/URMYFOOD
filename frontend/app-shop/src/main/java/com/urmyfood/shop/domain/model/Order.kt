package com.urmyfood.shop.domain.model

data class Order(
    val orderId: String,
    val customerId: Long,
    val customerName: String,
    val customerPhone: String,
    val shopId: Long,
    val totalAmount: Double,
    val discountAmount: Double,
    val finalAmount: Double,
    val orderStatus: String,
    val paymentMethod: String,
    val paymentStatus: String,
    val deliveryAddress: String,
    val note: String?,
    val cancelReason: String?,
    val createdAt: String,
    val items: List<OrderItem>
)

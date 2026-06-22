package com.urmyfood.shop.data.model

import com.google.gson.annotations.SerializedName
import com.urmyfood.shared.util.OrderDateFormatter
import com.urmyfood.shop.domain.model.Order
import com.urmyfood.shop.domain.model.OrderItem

data class OrderItemResponse(
    @SerializedName("order_item_id") val orderItemId: String?,
    @SerializedName("post_id") val postId: String?,
    @SerializedName("quantity") val quantity: Int?,
    @SerializedName("price_at_purchase") val priceAtPurchase: Double?,
    @SerializedName("dish_name_snapshot") val dishNameSnapshot: String?,
    @SerializedName("image_url_snapshot") val imageUrlSnapshot: String?
)

data class OrderResponse(
    @SerializedName("order_id") val orderId: String?,
    @SerializedName("customer_id") val customerId: Long?,
    @SerializedName("customer_name") val customerName: String?,
    @SerializedName("customer_phone") val customerPhone: String?,
    @SerializedName("shop_id") val shopId: Long?,
    @SerializedName("total_amount") val totalAmount: Double?,
    @SerializedName("discount_amount") val discountAmount: Double?,
    @SerializedName("final_amount") val finalAmount: Double?,
    @SerializedName("order_status") val orderStatus: String?,
    @SerializedName("payment_method") val paymentMethod: String?,
    @SerializedName("payment_status") val paymentStatus: String?,
    @SerializedName("delivery_address") val deliveryAddress: String?,
    @SerializedName("note") val note: String?,
    @SerializedName("cancel_reason") val cancelReason: String?,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("items") val items: List<OrderItemResponse>?
)

data class UpdateOrderStatusRequest(
    @SerializedName("status") val status: String,
    @SerializedName("reject_reason") val rejectReason: String?
)

fun OrderItemResponse.toDomain() = OrderItem(
    orderItemId = orderItemId.orEmpty(),
    postId = postId.orEmpty(),
    quantity = quantity ?: 0,
    priceAtPurchase = priceAtPurchase ?: 0.0,
    dishNameSnapshot = dishNameSnapshot.orEmpty(),
    imageUrlSnapshot = imageUrlSnapshot?.let { url ->
        if (url.isEmpty()) null
        else if (url.startsWith("http://") || url.startsWith("https://")) url
        else {
            val baseUrl = com.urmyfood.shop.BuildConfig.BASE_URL.removeSuffix("/")
            val path = if (url.startsWith("/")) url else "/$url"
            "$baseUrl$path"
        }
    }
)

fun OrderResponse.toDomain() = Order(
    orderId = orderId.orEmpty(),
    customerId = customerId ?: 0L,
    customerName = customerName.orEmpty(),
    customerPhone = customerPhone.orEmpty(),
    shopId = shopId ?: 0L,
    totalAmount = totalAmount ?: 0.0,
    discountAmount = discountAmount ?: 0.0,
    finalAmount = finalAmount ?: 0.0,
    orderStatus = orderStatus.orEmpty(),
    paymentMethod = paymentMethod.orEmpty(),
    paymentStatus = paymentStatus.orEmpty(),
    deliveryAddress = deliveryAddress.orEmpty(),
    note = note,
    cancelReason = cancelReason,
    createdAt = OrderDateFormatter.format(createdAt),
    items = items?.map { it.toDomain() } ?: emptyList()
)

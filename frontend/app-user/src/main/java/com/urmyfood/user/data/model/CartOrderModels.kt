package com.urmyfood.user.data.model

import com.google.gson.annotations.SerializedName
import com.urmyfood.user.domain.model.CartItem

data class AddCartItemRequest(
    @SerializedName("postId")
    val postId: String,
    @SerializedName("quantity")
    val quantity: Int
)

data class UpdateCartItemRequest(
    @SerializedName("quantity")
    val quantity: Int
)

data class CartResponse(
    @SerializedName("items")
    val items: List<CartItemResponse>,
    @SerializedName("total_amount")
    val totalAmount: Double,
    @SerializedName("item_count")
    val itemCount: Int
)

data class CartItemResponse(
    @SerializedName("cart_item_id")
    val cartItemId: String,
    @SerializedName("post_id")
    val postId: String,
    @SerializedName("dish_name")
    val dishName: String,
    @SerializedName("price")
    val price: Double,
    @SerializedName("image_url")
    val imageUrl: String?,
    @SerializedName("shop_id")
    val shopId: Long,
    @SerializedName("shop_name")
    val shopName: String,
    @SerializedName("quantity")
    val quantity: Int,
    @SerializedName("subtotal")
    val subtotal: Double,
    @SerializedName("remaining_quantity")
    val remainingQuantity: Int
)

data class CheckoutRequest(
    @SerializedName("paymentMethod")
    val paymentMethod: String,
    @SerializedName("deliveryAddress")
    val deliveryAddress: String,
    @SerializedName("voucherId")
    val voucherId: Long? = null,
    @SerializedName("note")
    val note: String? = null,
    @SerializedName("voucher_code")
    val voucherCode: String? = null
)

data class DirectCheckoutRequest(
    @SerializedName("post_id")
    val postId: String,
    @SerializedName("quantity")
    val quantity: Int,
    @SerializedName("paymentMethod")
    val paymentMethod: String,
    @SerializedName("deliveryAddress")
    val deliveryAddress: String,
    @SerializedName("voucherId")
    val voucherId: Long? = null,
    @SerializedName("note")
    val note: String? = null,
    @SerializedName("voucher_code")
    val voucherCode: String? = null
)

data class CancelOrderRequest(
    @SerializedName("cancelReason")
    val cancelReason: String
)

data class OrderResponse(
    @SerializedName("order_id")
    val orderId: String,
    @SerializedName("customer_id")
    val customerId: Long,
    @SerializedName("shop_id")
    val shopId: Long,
    @SerializedName("shop_name")
    val shopName: String,
    @SerializedName("voucher_id")
    val voucherId: Long?,
    @SerializedName("total_amount")
    val totalAmount: Double,
    @SerializedName("discount_amount")
    val discountAmount: Double,
    @SerializedName("final_amount")
    val finalAmount: Double,
    @SerializedName("order_status")
    val orderStatus: String,
    @SerializedName("payment_method")
    val paymentMethod: String,
    @SerializedName("payment_status")
    val paymentStatus: String,
    @SerializedName("delivery_address")
    val deliveryAddress: String,
    @SerializedName("note")
    val note: String?,
    @SerializedName("cancel_reason")
    val cancelReason: String?,
    @SerializedName("items")
    val items: List<OrderItemResponse>,
    @SerializedName("created_at")
    val createdAt: String?,
    @SerializedName("updated_at")
    val updatedAt: String?
)

data class OrderItemResponse(
    @SerializedName("order_item_id")
    val orderItemId: String,
    @SerializedName("post_id")
    val postId: String,
    @SerializedName("dish_name")
    val dishName: String,
    @SerializedName("image_url")
    val imageUrl: String?,
    @SerializedName("quantity")
    val quantity: Int,
    @SerializedName("price_at_purchase")
    val priceAtPurchase: Double,
    @SerializedName("subtotal")
    val subtotal: Double
)

data class PayOsPaymentResponse(
    @SerializedName("checkoutUrl")
    val checkoutUrl: String,
    @SerializedName("qrCode")
    val qrCode: String,
    @SerializedName("orderCode")
    val orderCode: Long
)

fun CartItemResponse.toDomain(): CartItem = CartItem(
    cartItemId = cartItemId,
    postId = postId,
    dishName = dishName,
    price = price,
    imageUrl = imageUrl,
    shopName = shopName,
    quantity = quantity,
    selectedOption = null,
    remainingQuantity = remainingQuantity
)

package com.urmyfood.user.domain.model

/**
 * Data class representing a food item inside the Shopping Cart.
 */
data class CartItem(
    val cartItemId: String? = null,
    val postId: String,
    val dishName: String,
    val price: Double,
    val imageUrl: String?,
    val shopName: String,
    var quantity: Int,
    val selectedOption: String?,
    val remainingQuantity: Int = Int.MAX_VALUE
)

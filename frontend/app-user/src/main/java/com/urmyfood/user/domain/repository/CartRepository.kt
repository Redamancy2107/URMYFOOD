package com.urmyfood.user.domain.repository

import com.urmyfood.user.data.model.CartResponse
import com.urmyfood.user.domain.model.Result

interface CartRepository {
    suspend fun getCart(token: String): Result<CartResponse>
    suspend fun addItem(token: String, postId: String, quantity: Int): Result<CartResponse>
    suspend fun updateItem(token: String, itemId: String, quantity: Int): Result<CartResponse>
    suspend fun deleteItem(token: String, itemId: String): Result<Unit>
}

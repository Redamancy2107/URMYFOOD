package com.urmyfood.user.data.local

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.urmyfood.user.domain.model.CartItem
import com.urmyfood.user.presentation.main.profile.OrderHistoryFragment.Order

/**
 * Manages local storage of shopping cart items and dynamically placed orders.
 * Uses SharedPreferences and Gson for local serialization.
 */
class CartManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val PREF_NAME = "urmyfood_cart_prefs"
        private const val KEY_CART = "cart_items"
        private const val KEY_ORDERS = "orders_list"
    }

    /**
     * Retrieve the list of items currently in the cart.
     */
    fun getCartItems(): List<CartItem> {
        val json = prefs.getString(KEY_CART, null) ?: return emptyList()
        val type = object : TypeToken<List<CartItem>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }

    /**
     * Save the entire cart item list.
     */
    fun saveCartItems(items: List<CartItem>) {
        prefs.edit().putString(KEY_CART, gson.toJson(items)).apply()
    }

    /**
     * Add an item to the cart. If the item with the same ID and options already exists,
     * its quantity is incremented. Otherwise, a new item is appended.
     */
    fun addCartItem(item: CartItem) {
        val items = getCartItems().toMutableList()
        val existingIndex = items.indexOfFirst { 
            it.postId == item.postId && it.selectedOption == item.selectedOption 
        }
        if (existingIndex != -1) {
            items[existingIndex].quantity += item.quantity
        } else {
            items.add(item)
        }
        saveCartItems(items)
    }

    /**
     * Update the quantity of a specific item in the cart.
     * If the new quantity <= 0, the item is removed from the cart.
     */
    fun updateCartItemQuantity(postId: String, selectedOption: String?, newQuantity: Int) {
        val items = getCartItems().toMutableList()
        val existingIndex = items.indexOfFirst { 
            it.postId == postId && it.selectedOption == selectedOption 
        }
        if (existingIndex != -1) {
            if (newQuantity <= 0) {
                items.removeAt(existingIndex)
            } else {
                items[existingIndex].quantity = newQuantity
            }
            saveCartItems(items)
        }
    }

    /**
     * Remove an item from the cart entirely.
     */
    fun removeCartItem(postId: String, selectedOption: String?) {
        val items = getCartItems().toMutableList()
        val existingIndex = items.indexOfFirst { 
            it.postId == postId && it.selectedOption == selectedOption 
        }
        if (existingIndex != -1) {
            items.removeAt(existingIndex)
            saveCartItems(items)
        }
    }

    /**
     * Clear all items in the cart (e.g. after checkout).
     */
    fun clearCart() {
        prefs.edit().remove(KEY_CART).apply()
    }

    // ==========================================
    // Dynamic Order History Logic
    // ==========================================

    /**
     * Retrieve the user's order history.
     * Pre-populates with mock data on first access if no history exists.
     */
    fun getOrders(): List<Order> {
        val json = prefs.getString(KEY_ORDERS, null)
        if (json == null) {
            // Populate initial mock orders - remove when BE ready
            val initial = listOf(
                Order("Phở Hà Nội", "Phở bò tái nạm x1", "65.000đ", "18/05/2026 12:30", 0, "https://images.unsplash.com/photo-1582878826629-29b7ad1cdc43?w=500"),
                Order("Bún Đậu Mắm Tôm", "Bún đậu đặc biệt x2", "110.000đ", "17/05/2026 19:00", 1, "https://images.unsplash.com/photo-1569718212165-3a8278d5f624?w=500"),
                Order("Cơm Tấm Sà Bi Chưởng", "Cơm tấm sườn bì chả x1", "75.000đ", "15/05/2026 11:45", 2, "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=500"),
                Order("Bánh Mì Huỳnh Hoa", "Bánh mì đặc biệt x1", "68.000đ", "14/05/2026 08:00", 2, "https://images.unsplash.com/photo-1600454021970-351feb2a5149?w=500"),
                Order("Trà Sữa Gong Cha", "Trà sữa truyền thống x2", "96.000đ", "10/05/2026 15:20", 3, "https://images.unsplash.com/photo-1541658016709-82535e94bc69?w=500")
            )
            saveOrders(initial)
            return initial
        }
        val type = object : TypeToken<List<Order>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }

    /**
     * Save the entire order list.
     */
    fun saveOrders(orders: List<Order>) {
        prefs.edit().putString(KEY_ORDERS, gson.toJson(orders)).apply()
    }

    /**
     * Add a newly placed order to the top of the history list.
     */
    fun addOrder(order: Order) {
        val ordersList = getOrders().toMutableList()
        ordersList.add(0, order) // Add to top
        saveOrders(ordersList)
    }
}

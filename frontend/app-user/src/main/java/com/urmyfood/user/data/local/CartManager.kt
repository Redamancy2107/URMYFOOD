package com.urmyfood.user.data.local

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.urmyfood.user.domain.model.CartItem


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


}

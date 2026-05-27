package com.urmyfood.user.data.local

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.urmyfood.user.domain.model.FoodPost
import com.urmyfood.user.domain.model.TokenProvider

/**
 * Manages local storage of bookmarked/favorite food posts.
 * Uses SharedPreferences and Gson for local serialization.
 */
class FavoritesManager(context: Context, private val tokenProvider: TokenProvider) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val PREF_NAME = "urmyfood_favorites_prefs"
        private const val KEY_FAVORITES = "favorite_posts"
    }

    private fun getFavoritesKey(): String {
        val token = tokenProvider.getAccessToken() ?: return KEY_FAVORITES + "_guest"
        val email = getEmailFromToken(token)
        return "${KEY_FAVORITES}_$email"
    }

    private fun getEmailFromToken(token: String): String {
        return try {
            val cleanToken = token.removePrefix("Bearer ").trim()
            val parts = cleanToken.split(".")
            if (parts.size >= 2) {
                val payload = String(
                    android.util.Base64.decode(parts[1], android.util.Base64.URL_SAFE or android.util.Base64.NO_WRAP)
                )
                val subKey = "\"sub\":\""
                val index = payload.indexOf(subKey)
                if (index != -1) {
                    val start = index + subKey.length
                    val end = payload.indexOf("\"", start)
                    if (end != -1) {
                        return payload.substring(start, end)
                    }
                }
            }
            cleanToken.takeLast(20).replace(Regex("[^a-zA-Z0-9]"), "")
        } catch (e: Exception) {
            "unknown"
        }
    }

    /**
     * Retrieve the list of favorite food posts.
     */
    fun getFavorites(): List<FoodPost> {
        val key = getFavoritesKey()
        val json = prefs.getString(key, null) ?: return emptyList()
        val type = object : TypeToken<List<FoodPost>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }

    /**
     * Save the entire list of favorite food posts.
     */
    fun saveFavorites(posts: List<FoodPost>) {
        val key = getFavoritesKey()
        prefs.edit().putString(key, gson.toJson(posts)).apply()
    }

    /**
     * Check if a post is already in favorites by its ID.
     */
    fun isFavorite(postId: String): Boolean {
        return getFavorites().any { it.postId == postId }
    }

    /**
     * Toggle the favorite status of a post.
     * If already bookmarked, it removes it. Otherwise, it adds it.
     * @return true if added, false if removed.
     */
    fun toggleFavorite(post: FoodPost): Boolean {
        val favorites = getFavorites().toMutableList()
        val index = favorites.indexOfFirst { it.postId == post.postId }
        val isAdded: Boolean
        if (index != -1) {
            favorites.removeAt(index)
            isAdded = false
        } else {
            favorites.add(post)
            isAdded = true
        }
        saveFavorites(favorites)
        return isAdded
    }

    fun updateFavorite(post: FoodPost) {
        val favorites = getFavorites().toMutableList()
        val index = favorites.indexOfFirst { it.postId == post.postId }
        if (index != -1) {
            favorites[index] = post
            saveFavorites(favorites)
        }
    }
}

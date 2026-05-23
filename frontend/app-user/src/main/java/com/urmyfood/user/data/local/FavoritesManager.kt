package com.urmyfood.user.data.local

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.urmyfood.user.domain.model.FoodPost

/**
 * Manages local storage of bookmarked/favorite food posts.
 * Uses SharedPreferences and Gson for local serialization.
 */
class FavoritesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val PREF_NAME = "urmyfood_favorites_prefs"
        private const val KEY_FAVORITES = "favorite_posts"
    }

    /**
     * Retrieve the list of favorite food posts.
     */
    fun getFavorites(): List<FoodPost> {
        val json = prefs.getString(KEY_FAVORITES, null) ?: return emptyList()
        val type = object : TypeToken<List<FoodPost>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }

    /**
     * Save the entire list of favorite food posts.
     */
    fun saveFavorites(posts: List<FoodPost>) {
        prefs.edit().putString(KEY_FAVORITES, gson.toJson(posts)).apply()
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

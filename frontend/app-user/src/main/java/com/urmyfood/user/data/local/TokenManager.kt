package com.urmyfood.user.data.local

import android.content.Context
import android.content.SharedPreferences

/**
 * Manages local storage of authentication tokens.
 * Uses SharedPreferences for simplicity.
 */
class TokenManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME = "urmyfood_prefs"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_ROLE = "user_role"
    }

    fun saveToken(token: String, refreshToken: String? = null, fullName: String? = null, role: String? = null) {
        prefs.edit().apply {
            putString(KEY_ACCESS_TOKEN, token)
            putString(KEY_REFRESH_TOKEN, refreshToken)
            putString(KEY_USER_NAME, fullName)
            putString(KEY_USER_ROLE, role)
            apply()
        }
    }

    fun getAccessToken(): String? = prefs.getString(KEY_ACCESS_TOKEN, null)

    fun getFullName(): String? = prefs.getString(KEY_USER_NAME, null)

    fun clear() {
        prefs.edit().clear().apply()
    }

    fun isLoggedIn(): Boolean = getAccessToken() != null
}

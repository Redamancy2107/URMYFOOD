package com.urmyfood.shared.data.local

import android.content.Context
import android.content.SharedPreferences

open class TokenManager(context: Context, prefName: String) : TokenStore {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(prefName, Context.MODE_PRIVATE)

    companion object {
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_ROLE = "user_role"
        private const val KEY_IS_FIRST_TIME = "is_first_time"
    }

    override fun saveToken(
        token: String,
        refreshToken: String?,
        fullName: String?,
        role: String?
    ) {
        prefs.edit().apply {
            putString(KEY_ACCESS_TOKEN, token)
            putString(KEY_REFRESH_TOKEN, refreshToken)
            putString(KEY_USER_NAME, fullName)
            putString(KEY_USER_ROLE, role)
            apply()
        }
    }

    override fun getAccessToken(): String? = prefs.getString(KEY_ACCESS_TOKEN, null)
    override fun getRefreshToken(): String? = prefs.getString(KEY_REFRESH_TOKEN, null)
    override fun getFullName(): String? = prefs.getString(KEY_USER_NAME, null)
    override fun getRole(): String? = prefs.getString(KEY_USER_ROLE, null)

    override fun clear() {
        prefs.edit().apply {
            remove(KEY_ACCESS_TOKEN)
            remove(KEY_REFRESH_TOKEN)
            remove(KEY_USER_NAME)
            remove(KEY_USER_ROLE)
            apply()
        }
    }

    override fun isLoggedIn(): Boolean = getAccessToken() != null

    override fun isFirstTime(): Boolean = prefs.getBoolean(KEY_IS_FIRST_TIME, true)

    override fun setFirstTime(isFirstTime: Boolean) {
        prefs.edit().putBoolean(KEY_IS_FIRST_TIME, isFirstTime).apply()
    }

    override fun saveFullName(fullName: String) {
        prefs.edit().putString(KEY_USER_NAME, fullName).apply()
    }
}

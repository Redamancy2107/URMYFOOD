package com.urmyfood.admin.data.local

import android.content.Context
import android.content.SharedPreferences

/**
 * Manages admin session data (JWT token, account info) using SharedPreferences.
 * Acts as a single source of truth for auth state across the app.
 */
object SessionManager {

    private const val PREF_NAME = "urmyfood_admin_session"
    private const val KEY_TOKEN = "jwt_token"
    private const val KEY_REFRESH_TOKEN = "jwt_refresh_token"
    private const val KEY_ACCOUNT_ID = "account_id"
    private const val KEY_FULL_NAME = "full_name"
    private const val KEY_ROLE = "role"
    private const val KEY_EMAIL = "email"

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    var token: String?
        get() = prefs.getString(KEY_TOKEN, null)
        set(value) = prefs.edit().putString(KEY_TOKEN, value).apply()

    var refreshToken: String?
        get() = prefs.getString(KEY_REFRESH_TOKEN, null)
        set(value) = prefs.edit().putString(KEY_REFRESH_TOKEN, value).apply()

    var accountId: Long
        get() = prefs.getLong(KEY_ACCOUNT_ID, -1L)
        set(value) = prefs.edit().putLong(KEY_ACCOUNT_ID, value).apply()

    var fullName: String?
        get() = prefs.getString(KEY_FULL_NAME, null)
        set(value) = prefs.edit().putString(KEY_FULL_NAME, value).apply()

    var role: String?
        get() = prefs.getString(KEY_ROLE, null)
        set(value) = prefs.edit().putString(KEY_ROLE, value).apply()

    var email: String?
        get() = prefs.getString(KEY_EMAIL, null)
        set(value) = prefs.edit().putString(KEY_EMAIL, value).apply()

    fun isLoggedIn(): Boolean = !token.isNullOrEmpty()

    fun saveSession(
        token: String,
        refreshToken: String,
        accountId: Long,
        fullName: String?,
        role: String?,
        email: String?
    ) {
        prefs.edit().apply {
            putString(KEY_TOKEN, token)
            putString(KEY_REFRESH_TOKEN, refreshToken)
            putLong(KEY_ACCOUNT_ID, accountId)
            putString(KEY_FULL_NAME, fullName)
            putString(KEY_ROLE, role)
            putString(KEY_EMAIL, email)
            apply()
        }
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }
}

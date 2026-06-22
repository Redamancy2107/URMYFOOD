package com.urmyfood.shared.data.local

interface TokenStore {
    fun saveToken(token: String, refreshToken: String? = null, fullName: String? = null, role: String? = null)
    fun getAccessToken(): String?
    fun getRefreshToken(): String?
    fun getFullName(): String?
    fun getRole(): String?
    fun clear()
    fun isLoggedIn(): Boolean
    fun isFirstTime(): Boolean
    fun setFirstTime(isFirstTime: Boolean)
    fun saveFullName(fullName: String)
}

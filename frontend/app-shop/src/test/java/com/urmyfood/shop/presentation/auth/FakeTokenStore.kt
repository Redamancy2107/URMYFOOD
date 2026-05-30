package com.urmyfood.shop.presentation.auth

import com.urmyfood.shared.data.local.TokenStore

class FakeTokenStore : TokenStore {

    var savedToken: String? = null
    var savedRole: String? = null
    private var loggedIn = false

    override fun saveToken(token: String, refreshToken: String?, fullName: String?, role: String?) {
        savedToken = token
        savedRole = role
        loggedIn = true
    }

    override fun getAccessToken(): String? = savedToken
    override fun getRefreshToken(): String? = null
    override fun getFullName(): String? = null
    override fun getRole(): String? = savedRole
    override fun clear() {
        savedToken = null
        savedRole = null
        loggedIn = false
    }

    override fun isLoggedIn(): Boolean = loggedIn
}

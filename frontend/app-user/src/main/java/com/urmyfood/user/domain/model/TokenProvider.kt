package com.urmyfood.user.domain.model

interface TokenProvider {
    fun getAccessToken(): String?
}

package com.urmyfood.user.domain.repository

interface GuestRepository {
    fun setGuest()
    fun clearGuest()
    fun isGuest(): Boolean
}

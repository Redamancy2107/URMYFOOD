package com.urmyfood.user.data.local

import android.content.Context
import android.content.SharedPreferences
import com.urmyfood.user.domain.repository.GuestRepository

class GuestSessionManager(context: Context) : GuestRepository {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME = "urmyfood_prefs"
        private const val KEY_IS_GUEST = "is_guest"
    }

    override fun setGuest() = prefs.edit().putBoolean(KEY_IS_GUEST, true).apply()

    override fun clearGuest() = prefs.edit().putBoolean(KEY_IS_GUEST, false).apply()

    override fun isGuest(): Boolean = prefs.getBoolean(KEY_IS_GUEST, false)
}

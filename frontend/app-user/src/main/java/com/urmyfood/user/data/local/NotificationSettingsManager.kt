package com.urmyfood.user.data.local

import android.content.Context
import android.content.SharedPreferences

/**
 * Manages local storage of notification preferences.
 * Uses SharedPreferences for persistence.
 */
class NotificationSettingsManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME = "urmyfood_notification_prefs"
        private const val KEY_ORDERS = "notify_orders"
        private const val KEY_PROMOTIONS = "notify_promotions"
        private const val KEY_MESSAGES = "notify_messages"
        private const val KEY_SYSTEM = "notify_system"
    }

    fun isOrdersEnabled(): Boolean = prefs.getBoolean(KEY_ORDERS, true)
    fun setOrdersEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_ORDERS, enabled).apply()

    fun isPromotionsEnabled(): Boolean = prefs.getBoolean(KEY_PROMOTIONS, true)
    fun setPromotionsEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_PROMOTIONS, enabled).apply()

    fun isMessagesEnabled(): Boolean = prefs.getBoolean(KEY_MESSAGES, false)
    fun setMessagesEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_MESSAGES, enabled).apply()

    fun isSystemEnabled(): Boolean = prefs.getBoolean(KEY_SYSTEM, true)
    fun setSystemEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_SYSTEM, enabled).apply()
}

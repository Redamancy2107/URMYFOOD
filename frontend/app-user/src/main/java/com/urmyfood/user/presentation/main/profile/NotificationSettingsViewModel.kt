package com.urmyfood.user.presentation.main.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.urmyfood.user.data.local.NotificationSettingsManager

class NotificationSettingsViewModel(
    private val settingsManager: NotificationSettingsManager
) : ViewModel() {

    private val _ordersEnabled = MutableLiveData<Boolean>()
    val ordersEnabled: LiveData<Boolean> = _ordersEnabled

    private val _promotionsEnabled = MutableLiveData<Boolean>()
    val promotionsEnabled: LiveData<Boolean> = _promotionsEnabled

    private val _messagesEnabled = MutableLiveData<Boolean>()
    val messagesEnabled: LiveData<Boolean> = _messagesEnabled

    private val _systemEnabled = MutableLiveData<Boolean>()
    val systemEnabled: LiveData<Boolean> = _systemEnabled

    init {
        loadSettings()
    }

    private fun loadSettings() {
        _ordersEnabled.value = settingsManager.isOrdersEnabled()
        _promotionsEnabled.value = settingsManager.isPromotionsEnabled()
        _messagesEnabled.value = settingsManager.isMessagesEnabled()
        _systemEnabled.value = settingsManager.isSystemEnabled()
    }

    fun setOrdersEnabled(enabled: Boolean) {
        settingsManager.setOrdersEnabled(enabled)
        _ordersEnabled.value = enabled
    }

    fun setPromotionsEnabled(enabled: Boolean) {
        settingsManager.setPromotionsEnabled(enabled)
        _promotionsEnabled.value = enabled
    }

    fun setMessagesEnabled(enabled: Boolean) {
        settingsManager.setMessagesEnabled(enabled)
        _messagesEnabled.value = enabled
    }

    fun setSystemEnabled(enabled: Boolean) {
        settingsManager.setSystemEnabled(enabled)
        _systemEnabled.value = enabled
    }

    class Factory(
        private val settingsManager: NotificationSettingsManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(NotificationSettingsViewModel::class.java)) {
                return NotificationSettingsViewModel(settingsManager) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

package com.urmyfood.shop.presentation.main.account.hours

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class BusinessHoursViewModel : ViewModel() {

    data class BusinessDay(
        val dayName: String,
        val isOpen: Boolean,
        val openTime: String,
        val closeTime: String
    )

    private val _businessDays = MutableLiveData<List<BusinessDay>>()
    val businessDays: LiveData<List<BusinessDay>> = _businessDays

    init {
        loadMockData()
    }

    private fun loadMockData() {
        // Mock data - remove when BE ready
        _businessDays.value = listOf(
            BusinessDay("Thứ Hai", true, "09:00", "22:00"),
            BusinessDay("Thứ Ba", true, "09:00", "22:00"),
            BusinessDay("Thứ Tư", true, "09:00", "22:00"),
            BusinessDay("Thứ Năm", true, "09:00", "22:00"),
            BusinessDay("Thứ Sáu", true, "09:00", "22:00"),
            BusinessDay("Thứ Bảy", true, "09:00", "22:00"),
            BusinessDay("Chủ Nhật", false, "09:00", "22:00")
        )
    }

    fun toggleDay(dayName: String, isOpen: Boolean) {
        val currentList = _businessDays.value ?: return
        val updatedList = currentList.map {
            if (it.dayName == dayName) {
                it.copy(isOpen = isOpen)
            } else {
                it
            }
        }
        _businessDays.value = updatedList
    }
}

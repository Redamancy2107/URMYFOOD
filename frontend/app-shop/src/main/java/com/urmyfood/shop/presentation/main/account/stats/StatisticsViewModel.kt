package com.urmyfood.shop.presentation.main.account.stats

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlin.math.abs

class StatisticsViewModel : ViewModel() {

    enum class Period { DAY, MONTH, YEAR, ALL }

    data class RevenueEntry(
        val label: String,
        val amount: Long
    )

    private val _selectedPeriod = MutableLiveData(Period.MONTH)
    val selectedPeriod: LiveData<Period> = _selectedPeriod

    private val _revenueEntries = MutableLiveData<List<RevenueEntry>>()
    val revenueEntries: LiveData<List<RevenueEntry>> = _revenueEntries

    private val _totalRevenue = MutableLiveData<Long>()
    val totalRevenue: LiveData<Long> = _totalRevenue

    private val _totalOrders = MutableLiveData<Int>()
    val totalOrders: LiveData<Int> = _totalOrders

    private val _cancelledOrders = MutableLiveData<Int>()
    val cancelledOrders: LiveData<Int> = _cancelledOrders

    private val _cancellationRate = MutableLiveData<Double>()
    val cancellationRate: LiveData<Double> = _cancellationRate

    // Sub-period selections
    private val _selectorText = MutableLiveData<String>()
    val selectorText: LiveData<String> = _selectorText

    private val _showSelector = MutableLiveData<Boolean>()
    val showSelector: LiveData<Boolean> = _showSelector

    // Lists of available options for dialogs
    val availableMonths = listOf(
        "Tháng 01/2026", "Tháng 02/2026", "Tháng 03/2026", "Tháng 04/2026",
        "Tháng 05/2026", "Tháng 06/2026", "Tháng 07/2026", "Tháng 08/2026",
        "Tháng 09/2026", "Tháng 10/2026", "Tháng 11/2026", "Tháng 12/2026"
    )

    val availableYears = listOf(
        "Năm 2026", "Năm 2025", "Năm 2024", "Năm 2023", "Năm 2022"
    )

    private var currentSelectedDay = "08/06/2026"
    private var currentSelectedMonth = "Tháng 06/2026"
    private var currentSelectedYear = "Năm 2026"

    init {
        loadData(Period.MONTH)
    }

    fun switchPeriod(period: Period) {
        if (_selectedPeriod.value == period) return
        _selectedPeriod.value = period
        loadData(period)
    }

    fun selectDay(day: String) {
        currentSelectedDay = day
        if (_selectedPeriod.value == Period.DAY) {
            loadData(Period.DAY)
        }
    }

    fun selectMonth(month: String) {
        currentSelectedMonth = month
        if (_selectedPeriod.value == Period.MONTH) {
            loadData(Period.MONTH)
        }
    }

    fun selectYear(year: String) {
        currentSelectedYear = year
        if (_selectedPeriod.value == Period.YEAR) {
            loadData(Period.YEAR)
        }
    }

    private fun loadData(period: Period) {
        _showSelector.value = period != Period.ALL

        when (period) {
            Period.DAY -> {
                _selectorText.value = currentSelectedDay
                val hash = abs(currentSelectedDay.hashCode().toLong())
                
                // Dynamic mock data based on selected day
                val seedAmount = (hash % 1_500_000L) + 400_000L
                val entries = listOf(
                    RevenueEntry("Sáng", seedAmount * 2 / 10),
                    RevenueEntry("Trưa", seedAmount * 4 / 10),
                    RevenueEntry("Chiều", seedAmount * 3 / 10),
                    RevenueEntry("Tối", seedAmount * 1 / 10)
                )
                _revenueEntries.value = entries
                _totalRevenue.value = entries.sumOf { it.amount }

                val orders = (hash % 12).toInt() + 6
                val cancelled = (hash % 3).toInt()
                _totalOrders.value = orders
                _cancelledOrders.value = cancelled
                _cancellationRate.value = if (orders > 0) (cancelled.toDouble() / orders.toDouble() * 100.0) else 0.0
            }
            Period.MONTH -> {
                _selectorText.value = currentSelectedMonth
                val hash = abs(currentSelectedMonth.hashCode().toLong())

                // Dynamic mock data based on selected month
                val seedAmount = (hash % 15_000_000L) + 12_000_000L
                val entries = listOf(
                    RevenueEntry("T1", seedAmount * 12 / 100),
                    RevenueEntry("T2", seedAmount * 10 / 100),
                    RevenueEntry("T3", seedAmount * 18 / 100),
                    RevenueEntry("T4", seedAmount * 15 / 100),
                    RevenueEntry("T5", seedAmount * 20 / 100),
                    RevenueEntry("T6", seedAmount * 25 / 100)
                )
                _revenueEntries.value = entries
                _totalRevenue.value = seedAmount

                val orders = (hash % 100).toInt() + 200
                val cancelled = (hash % 12).toInt() + 2
                _totalOrders.value = orders
                _cancelledOrders.value = cancelled
                _cancellationRate.value = if (orders > 0) (cancelled.toDouble() / orders.toDouble() * 100.0) else 0.0
            }
            Period.YEAR -> {
                _selectorText.value = currentSelectedYear
                val hash = abs(currentSelectedYear.hashCode().toLong())

                // Dynamic mock data based on selected year
                val seedAmount = (hash % 150_000_000L) + 180_000_000L
                val entries = listOf(
                    RevenueEntry("2024", seedAmount * 30 / 100),
                    RevenueEntry("2025", seedAmount * 40 / 100),
                    RevenueEntry("2026", seedAmount * 30 / 100)
                )
                _revenueEntries.value = entries
                _totalRevenue.value = seedAmount

                val orders = (hash % 1000).toInt() + 2500
                val cancelled = (hash % 100).toInt() + 50
                _totalOrders.value = orders
                _cancelledOrders.value = cancelled
                _cancellationRate.value = if (orders > 0) (cancelled.toDouble() / orders.toDouble() * 100.0) else 0.0
            }
            Period.ALL -> {
                val entries = listOf(
                    RevenueEntry("2024", 150_000_000L),
                    RevenueEntry("2025", 210_000_000L),
                    RevenueEntry("2026", 180_000_000L)
                )
                _revenueEntries.value = entries
                _totalRevenue.value = 540_000_000L
                _totalOrders.value = 6800
                _cancelledOrders.value = 180
                _cancellationRate.value = 2.6
            }
        }
    }

    class Factory : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(StatisticsViewModel::class.java)) {
                return StatisticsViewModel() as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

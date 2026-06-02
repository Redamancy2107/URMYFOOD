package com.urmyfood.shop.presentation.main.account.stats

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class StatisticsViewModel : ViewModel() {

    enum class Period { MONTHLY, QUARTERLY }

    data class RevenueEntry(
        val label: String,
        val amount: Long
    )

    private val _selectedPeriod = MutableLiveData(Period.MONTHLY)
    val selectedPeriod: LiveData<Period> = _selectedPeriod

    private val _revenueEntries = MutableLiveData<List<RevenueEntry>>()
    val revenueEntries: LiveData<List<RevenueEntry>> = _revenueEntries

    private val _totalRevenue = MutableLiveData<Long>()
    val totalRevenue: LiveData<Long> = _totalRevenue

    private val _totalOrders = MutableLiveData<Int>()
    val totalOrders: LiveData<Int> = _totalOrders

    // Mock data - remove when BE ready
    private val monthlyData = listOf(
        RevenueEntry(label = "T1", amount = 3_200_000L),
        RevenueEntry(label = "T2", amount = 2_800_000L),
        RevenueEntry(label = "T3", amount = 4_100_000L),
        RevenueEntry(label = "T4", amount = 3_500_000L),
        RevenueEntry(label = "T5", amount = 4_800_000L),
        RevenueEntry(label = "T6", amount = 5_100_000L)
    )

    // Mock data - remove when BE ready
    private val quarterlyData = listOf(
        RevenueEntry(label = "Q1", amount = 10_100_000L),
        RevenueEntry(label = "Q2", amount = 13_400_000L),
        RevenueEntry(label = "Q3", amount = 11_800_000L),
        RevenueEntry(label = "Q4", amount = 14_200_000L)
    )

    init {
        loadData(Period.MONTHLY)
    }

    fun switchPeriod(period: Period) {
        if (_selectedPeriod.value == period) return
        _selectedPeriod.value = period
        loadData(period)
    }

    private fun loadData(period: Period) {
        // Mock data - remove when BE ready
        val entries = when (period) {
            Period.MONTHLY -> monthlyData
            Period.QUARTERLY -> quarterlyData
        }
        _revenueEntries.value = entries
        _totalRevenue.value = entries.sumOf { it.amount }
        _totalOrders.value = when (period) {
            Period.MONTHLY -> 156
            Period.QUARTERLY -> 624
        }
    }
}

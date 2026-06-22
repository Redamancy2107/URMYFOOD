package com.urmyfood.shop.presentation.main.account.stats

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.urmyfood.shared.domain.model.Result
import com.urmyfood.shop.domain.model.RevenueEntry
import com.urmyfood.shop.domain.model.ShopStatisticsPeriod
import com.urmyfood.shop.domain.usecase.GetShopStatisticsUseCase
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.Year
import java.time.YearMonth
import java.time.format.DateTimeFormatter

sealed class StatisticsUiState {
    object Idle : StatisticsUiState()
    object Loading : StatisticsUiState()
    object Success : StatisticsUiState()
    data class Error(val message: String) : StatisticsUiState()
}

class StatisticsViewModel(
    private val getShopStatisticsUseCase: GetShopStatisticsUseCase
) : ViewModel() {

    enum class Period { DAY, MONTH, YEAR, ALL }

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

    private val _selectorText = MutableLiveData<String>()
    val selectorText: LiveData<String> = _selectorText

    private val _showSelector = MutableLiveData<Boolean>()
    val showSelector: LiveData<Boolean> = _showSelector

    private val _uiState = MutableLiveData<StatisticsUiState>(StatisticsUiState.Idle)
    val uiState: LiveData<StatisticsUiState> = _uiState

    private var currentSelectedDay = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
    private var currentSelectedMonth = YearMonth.now()
    private var currentSelectedYear = Year.now()

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
        parseMonth(month)?.let { currentSelectedMonth = it }
        if (_selectedPeriod.value == Period.MONTH) {
            loadData(Period.MONTH)
        }
    }

    fun selectYear(year: String) {
        parseYear(year)?.let { currentSelectedYear = it }
        if (_selectedPeriod.value == Period.YEAR) {
            loadData(Period.YEAR)
        }
    }

    private fun loadData(period: Period) {
        _showSelector.value = period != Period.ALL
        _selectorText.value = selectorTextFor(period)
        _uiState.value = StatisticsUiState.Loading

        viewModelScope.launch {
            val result = getShopStatisticsUseCase(
                period.toDomainPeriod(),
                date = if (period == Period.DAY) currentSelectedDay.toApiDate() else null,
                month = if (period == Period.MONTH) currentSelectedMonth.toString() else null,
                year = if (period == Period.YEAR) currentSelectedYear.toString() else null
            )
            when (result) {
                is Result.Success -> {
                    val statistics = result.data
                    _selectorText.value = statistics.selectorText.ifBlank { selectorTextFor(period) }
                    _revenueEntries.value = statistics.entries
                    _totalRevenue.value = statistics.totalRevenue
                    _totalOrders.value = statistics.totalOrders
                    _cancelledOrders.value = statistics.cancelledOrders
                    _cancellationRate.value = statistics.cancellationRate
                    _uiState.value = StatisticsUiState.Success
                }
                is Result.Error -> _uiState.value = StatisticsUiState.Error(result.message)
            }
        }
    }

    private fun Period.toDomainPeriod(): ShopStatisticsPeriod {
        return when (this) {
            Period.DAY -> ShopStatisticsPeriod.DAY
            Period.MONTH -> ShopStatisticsPeriod.MONTH
            Period.YEAR -> ShopStatisticsPeriod.YEAR
            Period.ALL -> ShopStatisticsPeriod.ALL
        }
    }

    private fun selectorTextFor(period: Period): String {
        return when (period) {
            Period.DAY -> currentSelectedDay
            Period.MONTH -> "Tháng %02d/%d".format(currentSelectedMonth.monthValue, currentSelectedMonth.year)
            Period.YEAR -> "Năm ${currentSelectedYear.value}"
            Period.ALL -> "Toàn bộ"
        }
    }

    private fun String.toApiDate(): String {
        val parsed = LocalDate.parse(this, DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        return parsed.toString()
    }

    private fun parseMonth(value: String): YearMonth? {
        return runCatching {
            val parts = value.removePrefix("Tháng ").split("/")
            YearMonth.of(parts[1].toInt(), parts[0].toInt())
        }.getOrNull()
    }

    private fun parseYear(value: String): Year? {
        return runCatching { Year.of(value.removePrefix("Năm ").toInt()) }.getOrNull()
    }

    class Factory(
        private val getShopStatisticsUseCase: GetShopStatisticsUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(StatisticsViewModel::class.java)) {
                return StatisticsViewModel(getShopStatisticsUseCase) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

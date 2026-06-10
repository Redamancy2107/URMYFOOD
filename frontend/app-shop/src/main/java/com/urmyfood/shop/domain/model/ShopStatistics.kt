package com.urmyfood.shop.domain.model

data class ShopStatistics(
    val period: String,
    val selectorText: String,
    val totalRevenue: Long,
    val totalOrders: Int,
    val cancelledOrders: Int,
    val cancellationRate: Double,
    val entries: List<RevenueEntry>
)

data class RevenueEntry(
    val label: String,
    val amount: Long
)

enum class ShopStatisticsPeriod {
    DAY,
    MONTH,
    YEAR,
    ALL
}

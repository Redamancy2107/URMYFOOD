package com.urmyfood.shop.data.model

import com.google.gson.annotations.SerializedName
import com.urmyfood.shop.domain.model.RevenueEntry
import com.urmyfood.shop.domain.model.ShopStatistics
import java.math.BigDecimal

data class ShopStatisticsResponse(
    @SerializedName("period")
    val period: String?,
    @SerializedName("selector_text")
    val selectorText: String?,
    @SerializedName("total_revenue")
    val totalRevenue: BigDecimal?,
    @SerializedName("total_orders")
    val totalOrders: Long?,
    @SerializedName("cancelled_orders")
    val cancelledOrders: Long?,
    @SerializedName("cancellation_rate")
    val cancellationRate: Double?,
    @SerializedName("entries")
    val entries: List<RevenueEntryResponse>?
)

data class RevenueEntryResponse(
    @SerializedName("label")
    val label: String?,
    @SerializedName("amount")
    val amount: BigDecimal?
)

fun ShopStatisticsResponse.toDomain() = ShopStatistics(
    period = period.orEmpty(),
    selectorText = selectorText.orEmpty(),
    totalRevenue = totalRevenue?.toLong() ?: 0L,
    totalOrders = totalOrders?.toInt() ?: 0,
    cancelledOrders = cancelledOrders?.toInt() ?: 0,
    cancellationRate = cancellationRate ?: 0.0,
    entries = entries.orEmpty().map {
        RevenueEntry(
            label = it.label.orEmpty(),
            amount = it.amount?.toLong() ?: 0L
        )
    }
)

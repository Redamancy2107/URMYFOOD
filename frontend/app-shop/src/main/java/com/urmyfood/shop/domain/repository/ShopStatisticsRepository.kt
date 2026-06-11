package com.urmyfood.shop.domain.repository

import com.urmyfood.shared.domain.model.Result
import com.urmyfood.shop.domain.model.ShopStatistics
import com.urmyfood.shop.domain.model.ShopStatisticsPeriod

interface ShopStatisticsRepository {
    suspend fun getStatistics(
        token: String,
        period: ShopStatisticsPeriod,
        date: String?,
        month: String?,
        year: String?
    ): Result<ShopStatistics>
}

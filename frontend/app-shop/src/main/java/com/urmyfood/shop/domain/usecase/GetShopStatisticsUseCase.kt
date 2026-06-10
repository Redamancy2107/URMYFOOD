package com.urmyfood.shop.domain.usecase

import com.urmyfood.shared.data.local.TokenStore
import com.urmyfood.shared.domain.model.Result
import com.urmyfood.shop.domain.model.ShopStatistics
import com.urmyfood.shop.domain.model.ShopStatisticsPeriod
import com.urmyfood.shop.domain.repository.ShopStatisticsRepository

class GetShopStatisticsUseCase(
    private val repository: ShopStatisticsRepository,
    private val tokenStore: TokenStore
) {
    suspend operator fun invoke(
        period: ShopStatisticsPeriod,
        date: String?,
        month: String?,
        year: String?
    ): Result<ShopStatistics> {
        val token = tokenStore.getAccessToken()
            ?: return Result.Error("Vui lòng đăng nhập lại")
        return repository.getStatistics("Bearer $token", period, date, month, year)
    }
}

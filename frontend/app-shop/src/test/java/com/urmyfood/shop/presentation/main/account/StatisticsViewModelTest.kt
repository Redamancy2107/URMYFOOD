package com.urmyfood.shop.presentation.main.account

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.urmyfood.shared.domain.model.Result
import com.urmyfood.shop.domain.model.RevenueEntry
import com.urmyfood.shop.domain.model.ShopStatistics
import com.urmyfood.shop.domain.model.ShopStatisticsPeriod
import com.urmyfood.shop.domain.repository.ShopStatisticsRepository
import com.urmyfood.shop.domain.usecase.GetShopStatisticsUseCase
import com.urmyfood.shop.presentation.auth.FakeTokenStore
import com.urmyfood.shop.presentation.main.account.stats.StatisticsUiState
import com.urmyfood.shop.presentation.main.account.stats.StatisticsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class StatisticsViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var repository: FakeShopStatisticsRepository
    private lateinit var tokenStore: FakeTokenStore

    @Before
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
        repository = FakeShopStatisticsRepository()
        tokenStore = FakeTokenStore().also {
            it.saveToken("token", null, null, "SHOP")
        }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init loads month statistics from repository`() = runTest {
        val viewModel = viewModel()
        advanceUntilIdle()

        assertEquals(ShopStatisticsPeriod.MONTH, repository.lastPeriod)
        assertEquals("Bearer token", repository.lastToken)
        assertEquals(200000L, viewModel.totalRevenue.value)
        assertEquals(12, viewModel.totalOrders.value)
        assertTrue(viewModel.uiState.value is StatisticsUiState.Success)
    }

    @Test
    fun `switchPeriod day sends api date`() = runTest {
        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.selectDay("08/06/2026")
        viewModel.switchPeriod(StatisticsViewModel.Period.DAY)
        advanceUntilIdle()

        assertEquals(ShopStatisticsPeriod.DAY, repository.lastPeriod)
        assertEquals("2026-06-08", repository.lastDate)
    }

    private fun viewModel() = StatisticsViewModel(GetShopStatisticsUseCase(repository, tokenStore))

    private class FakeShopStatisticsRepository : ShopStatisticsRepository {
        var lastToken: String? = null
        var lastPeriod: ShopStatisticsPeriod? = null
        var lastDate: String? = null

        override suspend fun getStatistics(
            token: String,
            period: ShopStatisticsPeriod,
            date: String?,
            month: String?,
            year: String?
        ): Result<ShopStatistics> {
            lastToken = token
            lastPeriod = period
            lastDate = date
            return Result.Success(
                ShopStatistics(
                    period = period.name,
                    selectorText = "Tháng 06/2026",
                    totalRevenue = 200000L,
                    totalOrders = 12,
                    cancelledOrders = 1,
                    cancellationRate = 8.3,
                    entries = listOf(RevenueEntry("T1", 200000L))
                )
            )
        }
    }
}

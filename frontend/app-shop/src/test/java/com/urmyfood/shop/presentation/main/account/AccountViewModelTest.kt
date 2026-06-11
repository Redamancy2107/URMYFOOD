package com.urmyfood.shop.presentation.main.account

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.urmyfood.shared.domain.model.Result
import com.urmyfood.shop.domain.model.ShopProfile
import com.urmyfood.shop.domain.model.ShopProfileImageType
import com.urmyfood.shop.domain.repository.ShopProfileRepository
import com.urmyfood.shop.domain.usecase.GetShopProfileUseCase
import com.urmyfood.shop.presentation.auth.FakeTokenStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import okhttp3.MultipartBody

@OptIn(ExperimentalCoroutinesApi::class)
class AccountViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var repository: FakeShopProfileRepository
    private lateinit var tokenStore: FakeTokenStore
    private lateinit var viewModel: AccountViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
        repository = FakeShopProfileRepository()
        tokenStore = FakeTokenStore().also {
            it.saveToken("token", null, null, "SHOP")
        }
        viewModel = AccountViewModel(GetShopProfileUseCase(repository, tokenStore), tokenStore)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadProfile success emits Success with shop profile`() = runTest {
        viewModel.loadProfile()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is AccountUiState.Success)
        assertEquals("Bearer token", repository.lastGetToken)
        assertEquals("Bếp Nhà A", (state as AccountUiState.Success).profile.shopName)
    }

    @Test
    fun `loadProfile error emits Error`() = runTest {
        repository.getResult = Result.Error("Không thể lấy hồ sơ quán")

        viewModel.loadProfile()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is AccountUiState.Error)
        assertEquals("Không thể lấy hồ sơ quán", (state as AccountUiState.Error).message)
    }

    @Test
    fun `logout clears token`() {
        viewModel.logout()

        assertNull(tokenStore.savedToken)
    }

    private class FakeShopProfileRepository : ShopProfileRepository {
        var getResult: Result<ShopProfile> = Result.Success(profile())
        var lastGetToken: String? = null

        override suspend fun getMyProfile(token: String): Result<ShopProfile> {
            lastGetToken = token
            return getResult
        }

        override suspend fun updateMyProfile(token: String, profile: ShopProfile): Result<ShopProfile> {
            return Result.Success(profile)
        }

        override suspend fun uploadProfileImage(
            token: String,
            type: ShopProfileImageType,
            file: MultipartBody.Part
        ): Result<String> {
            return Result.Success("https://cdn.example.com/profile.png")
        }
    }
}

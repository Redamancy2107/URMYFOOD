package com.urmyfood.shop.presentation.auth.register

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.urmyfood.shared.domain.model.AuthToken
import com.urmyfood.shared.domain.model.Result
import com.urmyfood.shared.domain.usecase.RegisterUseCase
import com.urmyfood.shared.domain.usecase.SendOtpUseCase
import com.urmyfood.shop.presentation.auth.FakeAuthRepository
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RegisterViewModelTest {

    @get:Rule
    val instantRule = InstantTaskExecutorRule()

    private lateinit var repository: FakeAuthRepository
    private lateinit var tokenStore: FakeTokenStore
    private lateinit var viewModel: RegisterViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(StandardTestDispatcher())
        repository = FakeAuthRepository()
        tokenStore = FakeTokenStore()
        viewModel = RegisterViewModel(RegisterUseCase(repository), SendOtpUseCase(repository), tokenStore)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `submitForm with invalid input emits Error`() = runTest {
        viewModel.submitForm("", "a@b.com", "0123456789", "secret1", "secret1", true)
        advanceUntilIdle()
        assertTrue(viewModel.formUiState.value is RegisterViewModel.FormUiState.Error)
    }

    @Test
    fun `submitForm without terms emits Error`() = runTest {
        viewModel.submitForm("Shop A", "a@b.com", "0123456789", "secret1", "secret1", false)
        advanceUntilIdle()
        assertTrue(viewModel.formUiState.value is RegisterViewModel.FormUiState.Error)
    }

    @Test
    fun `submitForm valid sends otp and emits OtpSent`() = runTest {
        viewModel.submitForm("Shop A", "a@b.com", "0123456789", "secret1", "secret1", true)
        advanceUntilIdle()
        assertTrue(viewModel.formUiState.value is RegisterViewModel.FormUiState.OtpSent)
        assertEquals("a@b.com", viewModel.email)
        assertEquals("a@b.com", repository.lastSendOtpEmail)
    }

    @Test
    fun `submitForm emits Error when sendOtp fails`() = runTest {
        repository.sendOtpResult = Result.Error("Send OTP failed")
        viewModel.submitForm("Shop A", "a@b.com", "0123456789", "secret1", "secret1", true)
        advanceUntilIdle()
        val state = viewModel.formUiState.value
        assertTrue(state is RegisterViewModel.FormUiState.Error)
        assertEquals("Send OTP failed", (state as RegisterViewModel.FormUiState.Error).message)
    }

    @Test
    fun `register calls backend with shop role and saves token`() = runTest {
        repository.registerResult = Result.Success(AuthToken("access", "refresh", null, "Shop A", "SHOP"))
        viewModel.submitForm("Shop A", "a@b.com", "0123456789", "secret1", "secret1", true)
        advanceUntilIdle()

        viewModel.register("123456")
        advanceUntilIdle()
        assertTrue(viewModel.registerUiState.value is RegisterViewModel.RegisterUiState.Success)
        assertEquals("SHOP", repository.lastRegisterRole)
        assertEquals("access", tokenStore.savedToken)
        assertEquals("SHOP", tokenStore.savedRole)
    }

    @Test
    fun `register propagates backend error`() = runTest {
        repository.registerResult = Result.Error("Mã OTP không chính xác hoặc đã hết hạn")
        viewModel.submitForm("Shop A", "a@b.com", "0123456789", "secret1", "secret1", true)
        advanceUntilIdle()

        viewModel.register("000000")
        advanceUntilIdle()
        val state = viewModel.registerUiState.value
        assertTrue(state is RegisterViewModel.RegisterUiState.Error)
        assertEquals("Mã OTP không chính xác hoặc đã hết hạn", (state as RegisterViewModel.RegisterUiState.Error).message)
    }
}

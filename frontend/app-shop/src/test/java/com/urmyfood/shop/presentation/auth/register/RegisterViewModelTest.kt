package com.urmyfood.shop.presentation.auth.register

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.urmyfood.shared.domain.model.Result
import com.urmyfood.shared.domain.usecase.RegisterUseCase
import com.urmyfood.shared.domain.usecase.SendOtpUseCase
import com.urmyfood.shop.presentation.auth.FakeAuthRepository
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
    private lateinit var viewModel: RegisterViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(StandardTestDispatcher())
        repository = FakeAuthRepository()
        viewModel = RegisterViewModel(RegisterUseCase(repository), SendOtpUseCase(repository))
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
    fun `submitForm valid emits OtpSent without backend`() = runTest {
        viewModel.submitForm("Shop A", "a@b.com", "0123456789", "secret1", "secret1", true)
        advanceUntilIdle()
        assertTrue(viewModel.formUiState.value is RegisterViewModel.FormUiState.OtpSent)
        assertEquals("a@b.com", viewModel.email)
    }

    @Test
    fun `submitForm ignores sendOtp error in mock mode`() = runTest {
        repository.sendOtpResult = Result.Error("Send OTP failed")
        viewModel.submitForm("Shop A", "a@b.com", "0123456789", "secret1", "secret1", true)
        advanceUntilIdle()
        assertTrue(viewModel.formUiState.value is RegisterViewModel.FormUiState.OtpSent)
    }

    @Test
    fun `register with mock otp emits Success`() = runTest {
        viewModel.submitForm("Shop A", "a@b.com", "0123456789", "secret1", "secret1", true)
        advanceUntilIdle()

        viewModel.register("123456")
        advanceUntilIdle()
        assertTrue(viewModel.registerUiState.value is RegisterViewModel.RegisterUiState.Success)
    }

    @Test
    fun `register with wrong mock otp emits Error`() = runTest {
        viewModel.submitForm("Shop A", "a@b.com", "0123456789", "secret1", "secret1", true)
        advanceUntilIdle()

        viewModel.register("000000")
        advanceUntilIdle()
        val state = viewModel.registerUiState.value
        assertTrue(state is RegisterViewModel.RegisterUiState.Error)
        assertEquals("Mã OTP demo là 123456", (state as RegisterViewModel.RegisterUiState.Error).message)
    }
}

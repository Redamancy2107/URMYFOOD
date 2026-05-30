package com.urmyfood.shop.presentation.auth.forgotpass

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.urmyfood.shared.domain.model.Result
import com.urmyfood.shared.domain.usecase.ForgotPasswordUseCase
import com.urmyfood.shared.domain.usecase.ResetPasswordUseCase
import com.urmyfood.shared.domain.usecase.VerifyOtpUseCase
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
class ForgotPasswordViewModelTest {

    @get:Rule
    val instantRule = InstantTaskExecutorRule()

    private lateinit var repository: FakeAuthRepository
    private lateinit var viewModel: ForgotPasswordViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(StandardTestDispatcher())
        repository = FakeAuthRepository()
        viewModel = ForgotPasswordViewModel(
            ForgotPasswordUseCase(repository),
            VerifyOtpUseCase(repository),
            ResetPasswordUseCase(repository)
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `sendForgotPassword success stores email and emits Success`() = runTest {
        repository.forgotResult = Result.Success(Unit)

        viewModel.sendForgotPassword("shop@mail.com")
        advanceUntilIdle()

        assertTrue(viewModel.forgotUiState.value is ForgotPasswordViewModel.ForgotUiState.Success)
        assertEquals("shop@mail.com", viewModel.email)
    }

    @Test
    fun `sendForgotPassword with invalid email emits Error`() = runTest {
        viewModel.sendForgotPassword("bad-email")
        advanceUntilIdle()

        assertTrue(viewModel.forgotUiState.value is ForgotPasswordViewModel.ForgotUiState.Error)
    }

    @Test
    fun `verifyOtp success stores reset token and emits Success`() = runTest {
        repository.verifyResult = Result.Success("reset-token-xyz")

        viewModel.verifyOtp("123456")
        advanceUntilIdle()

        assertTrue(viewModel.otpUiState.value is ForgotPasswordViewModel.OtpUiState.Success)
        assertEquals("reset-token-xyz", viewModel.resetToken)
    }

    @Test
    fun `verifyOtp with invalid length emits Error`() = runTest {
        viewModel.verifyOtp("123")
        advanceUntilIdle()

        assertTrue(viewModel.otpUiState.value is ForgotPasswordViewModel.OtpUiState.Error)
    }

    @Test
    fun `resetPassword success emits Success`() = runTest {
        repository.resetResult = Result.Success(Unit)

        viewModel.resetPassword("secret1", "secret1")
        advanceUntilIdle()

        assertTrue(viewModel.resetUiState.value is ForgotPasswordViewModel.ResetUiState.Success)
    }

    @Test
    fun `resetPassword with mismatched passwords emits Error`() = runTest {
        viewModel.resetPassword("secret1", "secret2")
        advanceUntilIdle()

        assertTrue(viewModel.resetUiState.value is ForgotPasswordViewModel.ResetUiState.Error)
    }
}

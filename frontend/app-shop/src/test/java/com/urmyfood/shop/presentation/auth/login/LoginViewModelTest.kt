package com.urmyfood.shop.presentation.auth.login

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.urmyfood.shared.domain.model.AuthToken
import com.urmyfood.shared.domain.model.Result
import com.urmyfood.shared.domain.usecase.LoginUseCase
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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    @get:Rule
    val instantRule = InstantTaskExecutorRule()

    private lateinit var repository: FakeAuthRepository
    private lateinit var tokenStore: FakeTokenStore
    private lateinit var viewModel: LoginViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(StandardTestDispatcher())
        repository = FakeAuthRepository()
        tokenStore = FakeTokenStore()
        viewModel = LoginViewModel(LoginUseCase(repository), tokenStore)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `login with SHOP role saves token and emits Success`() = runTest {
        repository.loginResult = Result.Success(
            AuthToken("access-token", "refresh", null, "Quán A", "SHOP")
        )

        viewModel.login("shop@mail.com", "secret")
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is LoginViewModel.UiState.Success)
        assertEquals("access-token", tokenStore.savedToken)
        assertEquals("SHOP", tokenStore.savedRole)
    }

    @Test
    fun `login with non-SHOP role emits WrongRole and does not save token`() = runTest {
        repository.loginResult = Result.Success(
            AuthToken("access-token", null, null, "User", "USER")
        )

        viewModel.login("user@mail.com", "secret")
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is LoginViewModel.UiState.WrongRole)
        assertNull(tokenStore.savedToken)
    }

    @Test
    fun `login failure emits Error with repository message`() = runTest {
        repository.loginResult = Result.Error("Sai mật khẩu")

        viewModel.login("shop@mail.com", "wrong")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is LoginViewModel.UiState.Error)
        assertEquals("Sai mật khẩu", (state as LoginViewModel.UiState.Error).message)
    }

    @Test
    fun `blank input emits validation Error from use case`() = runTest {
        viewModel.login("", "")
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is LoginViewModel.UiState.Error)
        assertNull(tokenStore.savedToken)
    }
}

package com.urmyfood.shared.domain.usecase

import com.urmyfood.shared.domain.model.Result
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ForgotPasswordUseCaseTest {

    private lateinit var repository: FakeAuthRepository
    private lateinit var useCase: ForgotPasswordUseCase

    @Before
    fun setup() {
        repository = FakeAuthRepository()
        useCase = ForgotPasswordUseCase(repository)
    }

    @Test
    fun `blank email returns error`() = runTest {
        val result = useCase("")
        assertTrue(result is Result.Error)
        assertNull(repository.lastForgotEmail)
    }

    @Test
    fun `malformed email returns error`() = runTest {
        val result = useCase("not-an-email")
        assertTrue(result is Result.Error)
        assertNull(repository.lastForgotEmail)
    }

    @Test
    fun `valid email trims and delegates to repository`() = runTest {
        val result = useCase("  shop@mail.com ")
        assertTrue(result is Result.Success)
        assertEquals("shop@mail.com", repository.lastForgotEmail)
    }

    @Test
    fun `repository error is propagated`() = runTest {
        repository.forgotResult = Result.Error("Không tìm thấy tài khoản")
        val result = useCase("shop@mail.com")
        assertTrue(result is Result.Error)
    }
}

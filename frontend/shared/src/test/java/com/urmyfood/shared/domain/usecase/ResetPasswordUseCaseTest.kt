package com.urmyfood.shared.domain.usecase

import com.urmyfood.shared.domain.model.Result
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ResetPasswordUseCaseTest {

    private lateinit var repository: FakeAuthRepository
    private lateinit var useCase: ResetPasswordUseCase

    @Before
    fun setup() {
        repository = FakeAuthRepository()
        useCase = ResetPasswordUseCase(repository)
    }

    @Test
    fun `password shorter than 6 chars returns error`() = runTest {
        val result = useCase("token", "123", "123")
        assertTrue(result is Result.Error)
        assertNull(repository.lastResetPassword)
    }

    @Test
    fun `mismatched passwords return error`() = runTest {
        val result = useCase("token", "123456", "654321")
        assertTrue(result is Result.Error)
        assertNull(repository.lastResetPassword)
    }

    @Test
    fun `valid matching passwords delegate to repository`() = runTest {
        val result = useCase("token-abc", "secret1", "secret1")
        assertTrue(result is Result.Success)
        assertEquals("token-abc", repository.lastResetToken)
        assertEquals("secret1", repository.lastResetPassword)
    }

    @Test
    fun `repository error is propagated`() = runTest {
        repository.resetResult = Result.Error("Token hết hạn")
        val result = useCase("token", "secret1", "secret1")
        assertTrue(result is Result.Error)
    }
}

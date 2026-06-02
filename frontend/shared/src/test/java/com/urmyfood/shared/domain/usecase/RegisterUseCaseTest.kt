package com.urmyfood.shared.domain.usecase

import com.urmyfood.shared.domain.model.AuthToken
import com.urmyfood.shared.domain.model.Result
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RegisterUseCaseTest {

    private lateinit var repository: FakeAuthRepository
    private lateinit var useCase: RegisterUseCase

    @Before
    fun setup() {
        repository = FakeAuthRepository()
        useCase = RegisterUseCase(repository)
    }

    @Test
    fun `blank fullName returns error`() = runTest {
        val result = useCase("", "a@b.com", "0123456789", "secret1", "secret1", "123456")
        assertTrue(result is Result.Error)
        assertNull(repository.lastRegisterEmail)
    }

    @Test
    fun `invalid email returns error`() = runTest {
        val result = useCase("Quán A", "bad-email", "0123456789", "secret1", "secret1", "123456")
        assertTrue(result is Result.Error)
        assertNull(repository.lastRegisterEmail)
    }

    @Test
    fun `invalid phone returns error`() = runTest {
        val result = useCase("Quán A", "a@b.com", "12", "secret1", "secret1", "123456")
        assertTrue(result is Result.Error)
        assertNull(repository.lastRegisterEmail)
    }

    @Test
    fun `password shorter than 6 returns error`() = runTest {
        val result = useCase("Quán A", "a@b.com", "0123456789", "123", "123", "123456")
        assertTrue(result is Result.Error)
        assertNull(repository.lastRegisterEmail)
    }

    @Test
    fun `mismatched passwords return error`() = runTest {
        val result = useCase("Quán A", "a@b.com", "0123456789", "secret1", "secret2", "123456")
        assertTrue(result is Result.Error)
        assertNull(repository.lastRegisterEmail)
    }

    @Test
    fun `valid input delegates to repository`() = runTest {
        repository.registerResult = Result.Success(AuthToken("token", null, null, "Quán A", "SHOP"))
        val result = useCase("  Quán A ", " a@b.com ", "0123456789", "secret1", "secret1", "123456")
        assertTrue(result is Result.Success)
        assertEquals("a@b.com", repository.lastRegisterEmail)
    }

    @Test
    fun `repository error is propagated`() = runTest {
        repository.registerResult = Result.Error("Email đã tồn tại")
        val result = useCase("Quán A", "a@b.com", "0123456789", "secret1", "secret1", "123456")
        assertTrue(result is Result.Error)
        assertEquals("Email đã tồn tại", (result as Result.Error).message)
    }
}

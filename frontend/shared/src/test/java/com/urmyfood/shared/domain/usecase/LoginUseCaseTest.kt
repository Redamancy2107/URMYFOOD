package com.urmyfood.shared.domain.usecase

import com.urmyfood.shared.domain.model.AuthToken
import com.urmyfood.shared.domain.model.Result
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class LoginUseCaseTest {

    private lateinit var repository: FakeAuthRepository
    private lateinit var useCase: LoginUseCase

    @Before
    fun setup() {
        repository = FakeAuthRepository()
        useCase = LoginUseCase(repository)
    }

    @Test
    fun `blank emailOrPhone returns error without calling repository`() = runTest {
        val result = useCase("", "password")
        assertTrue(result is Result.Error)
        assertEquals(null, repository.lastLoginEmailOrPhone)
    }

    @Test
    fun `blank password returns error without calling repository`() = runTest {
        val result = useCase("user@mail.com", "")
        assertTrue(result is Result.Error)
        assertEquals(null, repository.lastLoginEmailOrPhone)
    }

    @Test
    fun `valid input trims emailOrPhone and delegates to repository`() = runTest {
        val token = AuthToken("access", null, null, "Shop", "SHOP")
        repository.loginResult = Result.Success(token)

        val result = useCase("  user@mail.com  ", "secret")

        assertTrue(result is Result.Success)
        assertEquals("user@mail.com", repository.lastLoginEmailOrPhone)
        assertEquals(token, (result as Result.Success).data)
    }

    @Test
    fun `repository error is propagated`() = runTest {
        repository.loginResult = Result.Error("Sai mật khẩu")
        val result = useCase("user@mail.com", "wrong")
        assertTrue(result is Result.Error)
        assertEquals("Sai mật khẩu", (result as Result.Error).message)
    }
}

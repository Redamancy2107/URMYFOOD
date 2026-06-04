package com.urmyfood.shared.domain.usecase

import com.urmyfood.shared.domain.model.Result
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SendOtpUseCaseTest {

    private lateinit var repository: FakeAuthRepository
    private lateinit var useCase: SendOtpUseCase

    @Before
    fun setup() {
        repository = FakeAuthRepository()
        useCase = SendOtpUseCase(repository)
    }

    @Test
    fun `invalid email returns error without calling repository`() = runTest {
        val result = useCase("bad-email", "0123456789")
        assertTrue(result is Result.Error)
        assertNull(repository.lastSendOtpEmail)
    }

    @Test
    fun `valid email trims and delegates to repository`() = runTest {
        repository.sendOtpResult = Result.Success(Unit)
        val result = useCase("  a@b.com ", "0123456789")
        assertTrue(result is Result.Success)
        assertEquals("a@b.com", repository.lastSendOtpEmail)
    }

    @Test
    fun `repository error is propagated`() = runTest {
        repository.sendOtpResult = Result.Error("Gửi OTP thất bại")
        val result = useCase("a@b.com", null)
        assertTrue(result is Result.Error)
    }
}

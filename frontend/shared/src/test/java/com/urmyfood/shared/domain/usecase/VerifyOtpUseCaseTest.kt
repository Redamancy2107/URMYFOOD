package com.urmyfood.shared.domain.usecase

import com.urmyfood.shared.domain.model.Result
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class VerifyOtpUseCaseTest {

    private lateinit var repository: FakeAuthRepository
    private lateinit var useCase: VerifyOtpUseCase

    @Before
    fun setup() {
        repository = FakeAuthRepository()
        useCase = VerifyOtpUseCase(repository)
    }

    @Test
    fun `otp shorter than 6 digits returns error`() = runTest {
        val result = useCase("shop@mail.com", "1234")
        assertTrue(result is Result.Error)
        assertNull(repository.lastVerifyOtp)
    }

    @Test
    fun `otp with non-digit returns error`() = runTest {
        val result = useCase("shop@mail.com", "12a456")
        assertTrue(result is Result.Error)
        assertNull(repository.lastVerifyOtp)
    }

    @Test
    fun `valid 6-digit otp delegates and returns reset token`() = runTest {
        repository.verifyResult = Result.Success("token-123")
        val result = useCase("shop@mail.com", "123456")
        assertTrue(result is Result.Success)
        assertEquals("token-123", (result as Result.Success).data)
        assertEquals("123456", repository.lastVerifyOtp)
    }

    @Test
    fun `repository error is propagated`() = runTest {
        repository.verifyResult = Result.Error("Mã OTP không đúng")
        val result = useCase("shop@mail.com", "123456")
        assertTrue(result is Result.Error)
    }
}

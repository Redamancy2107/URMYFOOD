package com.urmyfood.user.presentation.main.cart

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.urmyfood.user.data.model.AddressResponse
import com.urmyfood.user.data.model.OrderResponse
import com.urmyfood.user.data.model.PayOsPaymentResponse
import com.urmyfood.user.data.model.VoucherResponse
import com.urmyfood.user.domain.model.Result
import com.urmyfood.user.domain.model.TokenProvider
import com.urmyfood.user.domain.repository.AddressRepository
import com.urmyfood.user.domain.repository.OrderRepository
import com.urmyfood.user.domain.repository.VoucherRepository
import com.urmyfood.user.domain.usecase.CheckoutUseCase
import com.urmyfood.user.domain.usecase.CreatePayOsPaymentUseCase
import com.urmyfood.user.domain.usecase.DirectCheckoutUseCase
import com.urmyfood.user.domain.usecase.GetAddressesUseCase
import com.urmyfood.user.domain.usecase.GetVouchersUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
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
class CheckoutViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private val tokenProvider = object : TokenProvider {
        override fun getAccessToken(): String = "token"
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `vietqr qr failure keeps checkout success with pending payment state`() = runTest(testDispatcher) {
        val orderRepository = FakeOrderRepository(
            checkoutResult = Result.Success(orderResponse(paymentMethod = "VIETQR")),
            createPaymentResult = Result.Error("PayOS unavailable")
        )
        val viewModel = checkoutViewModel(orderRepository)

        viewModel.checkout(
            paymentMethod = "VIETQR",
            deliveryAddress = "KTX Khu A",
            note = null,
            voucherCode = null
        )
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value!!
        assertTrue(state.isSuccess)
        assertEquals("order-1", state.orderId)
        assertEquals("VIETQR", state.paymentMethod)
        assertEquals(50000L, state.finalAmount)
        assertTrue(state.message!!.contains("chờ quán xác nhận"))
    }

    private fun checkoutViewModel(orderRepository: OrderRepository): CheckoutViewModel {
        val addressRepository = object : AddressRepository {
            override suspend fun getMyAddresses(token: String): Result<List<AddressResponse>> =
                Result.Success(emptyList())

            override suspend fun createAddress(
                token: String,
                label: String,
                name: String,
                phone: String,
                detail: String,
                isDefault: Boolean
            ): Result<AddressResponse> = Result.Error("unused")

            override suspend fun updateAddress(
                token: String,
                id: Long,
                label: String,
                name: String,
                phone: String,
                detail: String,
                isDefault: Boolean
            ): Result<AddressResponse> = Result.Error("unused")

            override suspend fun deleteAddress(token: String, id: Long): Result<Unit> =
                Result.Error("unused")

            override suspend fun setDefault(token: String, id: Long): Result<AddressResponse> =
                Result.Error("unused")
        }
        val voucherRepository = object : VoucherRepository {
            override suspend fun getActiveVouchers(): Result<List<VoucherResponse>> =
                Result.Success(emptyList())
        }
        return CheckoutViewModel(
            CheckoutUseCase(orderRepository, tokenProvider),
            DirectCheckoutUseCase(orderRepository, tokenProvider),
            GetAddressesUseCase(addressRepository, tokenProvider),
            GetVouchersUseCase(voucherRepository)
        )
    }

    private fun orderResponse(paymentMethod: String): OrderResponse = OrderResponse(
        orderId = "order-1",
        customerId = 1L,
        shopId = 2L,
        shopName = "Shop",
        voucherId = null,
        totalAmount = 50000.0,
        discountAmount = 0.0,
        finalAmount = 50000.0,
        orderStatus = "PENDING",
        paymentMethod = paymentMethod,
        paymentStatus = "UNPAID",
        deliveryAddress = "KTX Khu A",
        note = null,
        cancelReason = null,
        items = emptyList(),
        createdAt = "2026-06-22T07:05:00Z"
    )

    private class FakeOrderRepository(
        private val checkoutResult: Result<OrderResponse>,
        private val createPaymentResult: Result<PayOsPaymentResponse>
    ) : OrderRepository {
        override suspend fun checkout(
            token: String,
            paymentMethod: String,
            deliveryAddress: String,
            voucherId: Long?,
            note: String?,
            voucherCode: String?
        ): Result<OrderResponse> = checkoutResult

        override suspend fun directCheckout(
            token: String,
            postId: String,
            quantity: Int,
            paymentMethod: String,
            deliveryAddress: String,
            voucherId: Long?,
            note: String?,
            voucherCode: String?
        ): Result<OrderResponse> = checkoutResult

        override suspend fun getOrders(token: String): Result<List<OrderResponse>> =
            Result.Success(emptyList())

        override suspend fun getOrderDetail(token: String, orderId: String): Result<OrderResponse> =
            Result.Error("unused")

        override suspend fun cancelOrder(
            token: String,
            orderId: String,
            cancelReason: String
        ): Result<OrderResponse> = Result.Error("unused")

        override suspend fun createPayOsPayment(
            token: String,
            orderId: String
        ): Result<PayOsPaymentResponse> = createPaymentResult

        override suspend fun checkPayOsStatus(token: String, orderId: String): Result<OrderResponse> =
            Result.Error("unused")
    }
}

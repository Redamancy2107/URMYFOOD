package com.urmyfood.user.presentation.main.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.urmyfood.shared.util.OrderDateFormatter
import com.urmyfood.user.data.model.OrderResponse
import com.urmyfood.user.domain.model.Result
import com.urmyfood.user.domain.usecase.CancelOrderUseCase
import com.urmyfood.user.domain.usecase.CreatePayOsPaymentUseCase
import com.urmyfood.user.domain.usecase.CreateOrderReviewUseCase
import com.urmyfood.user.domain.usecase.GetOrdersUseCase
import com.urmyfood.user.domain.usecase.ReorderUseCase
import kotlinx.coroutines.launch

data class OrderHistoryUiState(
    val isLoading: Boolean = false,
    val orders: List<OrderHistoryFragment.Order> = emptyList(),
    val message: String? = null
)

data class PaymentQrNavigation(
    val orderId: String,
    val amount: Long,
    val qrCode: String
)

class OrderHistoryViewModel(
    private val getOrdersUseCase: GetOrdersUseCase,
    private val cancelOrderUseCase: CancelOrderUseCase,
    private val createPayOsPaymentUseCase: CreatePayOsPaymentUseCase,
    private val createOrderReviewUseCase: CreateOrderReviewUseCase,
    private val reorderUseCase: ReorderUseCase
) : ViewModel() {

    private val _uiState = MutableLiveData(OrderHistoryUiState(isLoading = true))
    val uiState: LiveData<OrderHistoryUiState> = _uiState

    private val _paymentQrNavigation = MutableLiveData<PaymentQrNavigation?>()
    val paymentQrNavigation: LiveData<PaymentQrNavigation?> = _paymentQrNavigation

    private val _cartNavigation = MutableLiveData(false)
    val cartNavigation: LiveData<Boolean> = _cartNavigation

    fun loadOrders() {
        viewModelScope.launch {
            _uiState.value = _uiState.value?.copy(isLoading = true, message = null)
            when (val result = getOrdersUseCase()) {
                is Result.Success -> _uiState.value = OrderHistoryUiState(
                    orders = result.data.flatMap { it.toDisplayOrders() }
                )
                is Result.Error -> _uiState.value = OrderHistoryUiState(message = result.message)
            }
        }
    }

    fun cancelOrder(orderId: String, reason: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value?.copy(isLoading = true, message = null)
            when (val result = cancelOrderUseCase(orderId, reason)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value?.copy(message = "Hủy đơn hàng thành công")
                    loadOrders()
                }
                is Result.Error -> _uiState.value = _uiState.value?.copy(
                    isLoading = false,
                    message = result.message
                )
            }
        }
    }

    fun createVietQrPayment(orderId: String, amount: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value?.copy(isLoading = true, message = null)
            when (val result = createPayOsPaymentUseCase(orderId)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value?.copy(isLoading = false)
                    _paymentQrNavigation.value = PaymentQrNavigation(
                        orderId = orderId,
                        amount = amount,
                        qrCode = result.data.qrCode
                    )
                }
                is Result.Error -> _uiState.value = _uiState.value?.copy(
                    isLoading = false,
                    message = result.message
                )
            }
        }
    }

    fun clearPaymentQrNavigation() {
        _paymentQrNavigation.value = null
    }

    fun createReview(orderId: String, rating: Int, comment: String?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value?.copy(isLoading = true, message = null)
            when (val result = createOrderReviewUseCase(orderId, rating, comment)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value?.copy(message = "Đánh giá đơn hàng thành công")
                    loadOrders()
                }
                is Result.Error -> _uiState.value = _uiState.value?.copy(
                    isLoading = false,
                    message = result.message
                )
            }
        }
    }

    fun reorder(orderId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value?.copy(isLoading = true, message = null)
            when (val result = reorderUseCase(orderId)) {
                is Result.Success -> {
                    val skippedCount = result.data.skippedItems.size
                    val message = if (skippedCount > 0) {
                        "Đã thêm ${result.data.addedCount} món vào giỏ, bỏ qua $skippedCount món"
                    } else {
                        "Đã thêm món vào giỏ hàng"
                    }
                    _uiState.value = _uiState.value?.copy(isLoading = false, message = message)
                    _cartNavigation.value = true
                }
                is Result.Error -> _uiState.value = _uiState.value?.copy(
                    isLoading = false,
                    message = result.message
                )
            }
        }
    }

    fun clearCartNavigation() {
        _cartNavigation.value = false
    }

    fun clearMessage() {
        _uiState.value = _uiState.value?.copy(message = null)
    }

    private fun OrderResponse.toDisplayOrders(): List<OrderHistoryFragment.Order> {
        val statusTab = OrderHistoryStatusMapper.tabFor(orderStatus)
        return items.ifEmpty {
            listOf(null)
        }.map { item ->
            OrderHistoryFragment.Order(
                orderId = orderId,
                shop = shopName,
                desc = item?.let { "${it.dishName} x${it.quantity}" } ?: "Đơn hàng ${orderId.take(8)}",
                price = formatCurrency(item?.subtotal ?: finalAmount),
                date = OrderDateFormatter.format(createdAt),
                status = statusTab,
                statusLabel = OrderHistoryStatusMapper.labelFor(orderStatus) + paymentStatusLabel(),
                originalStatus = orderStatus,
                rawCreatedAt = createdAt ?: "",
                rawUpdatedAt = updatedAt ?: "",
                paymentMethod = paymentMethod,
                paymentStatus = paymentStatus,
                finalAmount = finalAmount.toLong(),
                reviewed = reviewed,
                imageUrl = item?.imageUrl?.let { url ->
                    if (url.isEmpty()) null
                    else if (url.startsWith("http://") || url.startsWith("https://")) url
                    else {
                        val baseUrl = com.urmyfood.user.BuildConfig.BASE_URL.removeSuffix("/")
                        val path = if (url.startsWith("/")) url else "/$url"
                        "$baseUrl$path"
                    }
                }
            )
        }
    }

    private fun OrderResponse.paymentStatusLabel(): String = when {
        paymentMethod == PAYMENT_VIETQR && paymentStatus != PAYMENT_PAID -> " (Chờ thanh toán VietQR)"
        paymentMethod == PAYMENT_VIETQR && paymentStatus == PAYMENT_PAID -> " (VietQR - Đã thanh toán)"
        paymentStatus == PAYMENT_PAID -> " (Đã thanh toán)"
        else -> ""
    }

    private fun formatCurrency(value: Double): String {
        return "${java.text.NumberFormat.getNumberInstance(java.util.Locale("vi", "VN")).format(value)}đ"
    }

    class Factory(
        private val getOrdersUseCase: GetOrdersUseCase,
        private val cancelOrderUseCase: CancelOrderUseCase,
        private val createPayOsPaymentUseCase: CreatePayOsPaymentUseCase,
        private val createOrderReviewUseCase: CreateOrderReviewUseCase,
        private val reorderUseCase: ReorderUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(OrderHistoryViewModel::class.java)) {
                return OrderHistoryViewModel(
                    getOrdersUseCase,
                    cancelOrderUseCase,
                    createPayOsPaymentUseCase,
                    createOrderReviewUseCase,
                    reorderUseCase
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

    companion object {
        private const val PAYMENT_VIETQR = "VIETQR"
        private const val PAYMENT_PAID = "PAID"
    }
}

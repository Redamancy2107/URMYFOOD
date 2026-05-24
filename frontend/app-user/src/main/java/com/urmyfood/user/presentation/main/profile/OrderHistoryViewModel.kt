package com.urmyfood.user.presentation.main.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.urmyfood.user.data.model.OrderResponse
import com.urmyfood.user.domain.model.Result
import com.urmyfood.user.domain.usecase.GetOrdersUseCase
import kotlinx.coroutines.launch

data class OrderHistoryUiState(
    val isLoading: Boolean = false,
    val orders: List<OrderHistoryFragment.Order> = emptyList(),
    val message: String? = null
)

class OrderHistoryViewModel(
    private val getOrdersUseCase: GetOrdersUseCase
) : ViewModel() {

    private val _uiState = MutableLiveData(OrderHistoryUiState(isLoading = true))
    val uiState: LiveData<OrderHistoryUiState> = _uiState

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

    private fun OrderResponse.toDisplayOrders(): List<OrderHistoryFragment.Order> {
        val statusTab = when (orderStatus) {
            "PENDING", "ACCEPTED" -> 0
            "PICKING_UP", "DELIVERING" -> 1
            "COMPLETED" -> 2
            else -> 3
        }
        return items.ifEmpty {
            listOf(null)
        }.map { item ->
            OrderHistoryFragment.Order(
                shop = shopName,
                desc = item?.let { "${it.dishName} x${it.quantity}" } ?: "Đơn hàng ${orderId.take(8)}",
                price = formatCurrency(item?.subtotal ?: finalAmount),
                date = createdAt ?: "",
                status = statusTab,
                imageUrl = item?.imageUrl
            )
        }
    }

    private fun formatCurrency(value: Double): String {
        return "${java.text.NumberFormat.getNumberInstance(java.util.Locale("vi", "VN")).format(value)}đ"
    }

    class Factory(
        private val getOrdersUseCase: GetOrdersUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(OrderHistoryViewModel::class.java)) {
                return OrderHistoryViewModel(getOrdersUseCase) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

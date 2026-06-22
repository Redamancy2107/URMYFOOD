package com.urmyfood.shop.presentation.main.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.urmyfood.shared.domain.model.Result
import com.urmyfood.shop.domain.model.Order
import com.urmyfood.shop.domain.usecase.GetShopOrdersUseCase
import com.urmyfood.shop.domain.usecase.UpdateOrderStatusUseCase
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class HomeViewModel(
    private val getShopOrdersUseCase: GetShopOrdersUseCase,
    private val updateOrderStatusUseCase: UpdateOrderStatusUseCase
) : ViewModel() {

    data class ActiveOrder(
        val orderId: String,
        val time: String,
        val customerName: String,
        val status: OrderStatus,
        val itemsSummary: String,
        val totalPrice: Long,
        val imageUrl: String?
    )

    enum class OrderStatus(val label: String) {
        WAITING("Chờ xác nhận"),
        PREPARING("Đang chuẩn bị")
    }

    private val _salesAmount = MutableLiveData("0 VNĐ")
    val salesAmount: LiveData<String> = _salesAmount

    private val _orderCount = MutableLiveData(0)
    val orderCount: LiveData<Int> = _orderCount

    private val _activeOrders = MutableLiveData<List<ActiveOrder>>(emptyList())
    val activeOrders: LiveData<List<ActiveOrder>> = _activeOrders

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    init {
        loadOrders()
    }

    fun loadOrders() {
        viewModelScope.launch {
            when (val result = getShopOrdersUseCase()) {
                is Result.Success -> applyOrders(result.data)
                is Result.Error -> _errorMessage.value = result.message
            }
        }
    }

    fun advanceOrderStatus(orderId: String) {
        val order = _activeOrders.value.orEmpty().find { it.orderId == orderId } ?: return
        val nextStatus = when (order.status) {
            OrderStatus.WAITING -> "ACCEPTED"
            OrderStatus.PREPARING -> "PICKING_UP"
        }
        viewModelScope.launch {
            when (val result = updateOrderStatusUseCase(orderId, nextStatus)) {
                is Result.Success -> loadOrders()
                is Result.Error -> _errorMessage.value = result.message
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    private fun applyOrders(orders: List<Order>) {
        val active = orders.mapNotNull { it.toActiveOrderOrNull() }
        _activeOrders.value = active
        _orderCount.value = active.size
        val completedTotal = orders
            .filter { it.orderStatus == "COMPLETED" }
            .sumOf { it.finalAmount.toLong() }
        _salesAmount.value = NumberFormat.getCurrencyInstance(Locale("vi", "VN")).format(completedTotal)
        _errorMessage.value = null
    }

    private fun Order.toActiveOrderOrNull(): ActiveOrder? {
        val mappedStatus = when (orderStatus) {
            "PENDING" -> OrderStatus.WAITING
            "ACCEPTED" -> OrderStatus.PREPARING
            else -> return null
        }
        return ActiveOrder(
            orderId = orderId,
            time = createdAt,
            customerName = customerName.ifBlank { "Khách hàng" },
            status = mappedStatus,
            itemsSummary = items.joinToString(", ") { "${it.quantity}x ${it.dishNameSnapshot}" },
            totalPrice = finalAmount.toLong(),
            imageUrl = items.firstOrNull()?.imageUrlSnapshot
        )
    }

    class Factory(
        private val getShopOrdersUseCase: GetShopOrdersUseCase,
        private val updateOrderStatusUseCase: UpdateOrderStatusUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HomeViewModel(getShopOrdersUseCase, updateOrderStatusUseCase) as T
        }
    }
}

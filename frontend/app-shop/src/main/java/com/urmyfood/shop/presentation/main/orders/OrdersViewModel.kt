package com.urmyfood.shop.presentation.main.orders

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

class OrdersViewModel(
    private val getShopOrdersUseCase: GetShopOrdersUseCase,
    private val updateOrderStatusUseCase: UpdateOrderStatusUseCase
) : ViewModel() {

    enum class StatusFilter(val label: String) {
        PENDING("Chờ xác nhận"),
        PROCESSING("Đang xử lý"),
        DELIVERING("Đang giao"),
        COMPLETED("Hoàn thành"),
        CANCELLED("Đã hủy")
    }

    private val _allOrders = MutableLiveData<List<Order>>(emptyList())
    val allOrders: LiveData<List<Order>> = _allOrders

    private val _selectedFilter = MutableLiveData(StatusFilter.PENDING)
    val selectedFilter: LiveData<StatusFilter> = _selectedFilter

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>(null)
    val errorMessage: LiveData<String?> = _errorMessage

    init {
        loadOrders()
    }


    fun loadOrders() {
        viewModelScope.launch {
            _isLoading.value = true
            when (val result = getShopOrdersUseCase()) {
                is Result.Success -> {
                    _allOrders.value = result.data
                    _errorMessage.value = null
                }
                is Result.Error -> {
                    _errorMessage.value = result.message
                }
            }
            _isLoading.value = false
        }
    }

    fun setStatusFilter(filter: StatusFilter) {
        _selectedFilter.value = filter
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    fun acceptOrder(orderId: String) {
        changeStatus(orderId, "ACCEPTED", null)
    }

    fun rejectOrder(orderId: String, reason: String) {
        changeStatus(orderId, "REJECTED", reason)
    }

    fun advanceOrderStatus(orderId: String) {
        val order = _allOrders.value?.find { it.orderId == orderId } ?: return
        val nextStatus = when (order.orderStatus) {
            "ACCEPTED" -> "PICKING_UP"
            "PICKING_UP" -> "DELIVERING"
            "DELIVERING" -> "COMPLETED"
            else -> return
        }
        changeStatus(orderId, nextStatus, null)
    }

    private fun changeStatus(orderId: String, status: String, rejectReason: String?) {
        viewModelScope.launch {
            _isLoading.value = true
            when (val result = updateOrderStatusUseCase(orderId, status, rejectReason)) {
                is Result.Success -> {
                    val updatedOrder = result.data
                    _allOrders.value = _allOrders.value?.map {
                        if (it.orderId == orderId) updatedOrder else it
                    }
                    _errorMessage.value = null
                }
                is Result.Error -> {
                    _errorMessage.value = result.message
                }
            }
            _isLoading.value = false
        }
    }

    class Factory(
        private val getShopOrdersUseCase: GetShopOrdersUseCase,
        private val updateOrderStatusUseCase: UpdateOrderStatusUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return OrdersViewModel(getShopOrdersUseCase, updateOrderStatusUseCase) as T
        }
    }
}

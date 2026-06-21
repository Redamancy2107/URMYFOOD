package com.urmyfood.shop.presentation.main.orders.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.urmyfood.shared.domain.model.Result
import com.urmyfood.shop.domain.model.Order
import com.urmyfood.shop.domain.usecase.GetShopOrderDetailUseCase
import com.urmyfood.shop.domain.usecase.UpdateOrderStatusUseCase
import kotlinx.coroutines.launch

class OrderDetailViewModel(
    private val getShopOrderDetailUseCase: GetShopOrderDetailUseCase,
    private val updateOrderStatusUseCase: UpdateOrderStatusUseCase
) : ViewModel() {

    private val _orderDetail = MutableLiveData<Order?>()
    val orderDetail: LiveData<Order?> = _orderDetail

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>(null)
    val errorMessage: LiveData<String?> = _errorMessage

    private val _actionSuccess = MutableLiveData<String?>(null)
    val actionSuccess: LiveData<String?> = _actionSuccess

    fun loadOrderDetail(orderId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            when (val result = getShopOrderDetailUseCase(orderId)) {
                is Result.Success -> {
                    _orderDetail.value = result.data
                    _errorMessage.value = null
                }
                is Result.Error -> {
                    _errorMessage.value = result.message
                }
            }
            _isLoading.value = false
        }
    }

    fun updateStatus(orderId: String, status: String, rejectReason: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            when (val result = updateOrderStatusUseCase(orderId, status, rejectReason)) {
                is Result.Success -> {
                    _orderDetail.value = result.data
                    _actionSuccess.value = "Cập nhật trạng thái thành công"
                    _errorMessage.value = null
                }
                is Result.Error -> {
                    _errorMessage.value = result.message
                }
            }
            _isLoading.value = false
        }
    }

    fun getFinalAmount(): Double = _orderDetail.value?.finalAmount ?: 0.0

    class Factory(
        private val getShopOrderDetailUseCase: GetShopOrderDetailUseCase,
        private val updateOrderStatusUseCase: UpdateOrderStatusUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return OrderDetailViewModel(getShopOrderDetailUseCase, updateOrderStatusUseCase) as T
        }
    }
}

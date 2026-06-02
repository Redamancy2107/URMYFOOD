package com.urmyfood.shop.presentation.main.orders.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.urmyfood.shop.presentation.main.orders.OrdersViewModel.OrderStatus

class OrderDetailViewModel : ViewModel() {

    data class DishItem(
        val name: String,
        val quantity: Int,
        val price: Long,
        val imageUrl: String? = null
    )

    data class OrderDetail(
        val orderId: String,
        val customerName: String,
        val customerPhone: String,
        val timestamp: String,
        val status: OrderStatus,
        val buyerNote: String?,
        val dishes: List<DishItem>
    )

    private val _orderDetail = MutableLiveData<OrderDetail>()
    val orderDetail: LiveData<OrderDetail> = _orderDetail

    fun loadOrderDetail(orderId: String) {
        // Mock data - remove when BE ready
        _orderDetail.value = OrderDetail(
            orderId = orderId,
            customerName = "Hùng đẹp zai",
            customerPhone = "0901234567",
            timestamp = "12:00 AM",
            status = if (orderId == "#ORD-1003" || orderId == "#ORD-1004" || orderId == "#ORD-1008") {
                OrderStatus.PREPARING
            } else if (orderId == "#ORD-1005" || orderId == "#ORD-1006") {
                OrderStatus.READY
            } else {
                OrderStatus.WAITING
            },
            buyerNote = "Không hành, không cay. Dị ứng mắm tôm",
            dishes = listOf(
                DishItem("Phở bò tái nạm", 2, 45_000L),
                DishItem("Bánh quẩy nóng", 3, 5_000L),
                DishItem("Trà chanh sả", 1, 15_000L)
            )
        )
    }

    fun updateStatus(newStatus: OrderStatus) {
        val current = _orderDetail.value ?: return
        _orderDetail.value = current.copy(status = newStatus)
    }

    fun getSubtotal(): Long {
        return _orderDetail.value?.dishes?.sumOf { it.price * it.quantity } ?: 0L
    }

    fun getPlatformFee(): Long {
        return (getSubtotal() * 0.05).toLong()
    }

    fun getEarnings(): Long {
        return getSubtotal() - getPlatformFee()
    }
}

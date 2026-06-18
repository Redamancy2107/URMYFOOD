package com.urmyfood.shop.presentation.main.orders

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class OrdersViewModel : ViewModel() {

    enum class OrderStatus(val label: String) {
        WAITING("Chờ xác nhận"),
        PREPARING("Đang chuẩn bị"),
        READY("Đã sẵn sàng")
    }

    data class ShopOrder(
        val orderId: String,
        val customerName: String,
        val timestamp: String,
        val status: OrderStatus,
        val items: String,
        val totalPrice: Long
    )

    private val _allOrders = MutableLiveData<List<ShopOrder>>()
    val allOrders: LiveData<List<ShopOrder>> = _allOrders

    private val _selectedStatus = MutableLiveData(OrderStatus.WAITING)
    val selectedStatus: LiveData<OrderStatus> = _selectedStatus

    init {
        loadMockOrders()
    }

    private fun loadMockOrders() {
        // Mock data - remove when BE ready
        _allOrders.value = listOf(
            ShopOrder("#ORD-1001", "Nguyễn Văn A", "10:30", OrderStatus.WAITING, "2x Cơm tấm sườn, 1x Trà đá", 85_000L),
            ShopOrder("#ORD-1002", "Trần Thị B", "10:25", OrderStatus.WAITING, "1x Bún bò Huế, 1x Sữa đậu nành", 55_000L),
            ShopOrder("#ORD-1003", "Lê Hoàng C", "10:15", OrderStatus.PREPARING, "3x Bánh mì thịt, 2x Cà phê sữa", 125_000L),
            ShopOrder("#ORD-1004", "Phạm Minh D", "10:10", OrderStatus.PREPARING, "1x Phở bò chín, 1x Quẩy", 60_000L),
            ShopOrder("#ORD-1005", "Võ Thanh E", "09:55", OrderStatus.READY, "2x Mì Quảng gà, 1x Sinh tố dừa", 95_000L),
            ShopOrder("#ORD-1006", "Đỗ Hữu F", "09:40", OrderStatus.READY, "1x Cơm chiên Dương Châu, 1x Pepsi", 50_000L),
            ShopOrder("#ORD-1007", "Hoàng Văn G", "09:15", OrderStatus.WAITING, "1x Hủ tiếu Nam Vang", 45_000L),
            ShopOrder("#ORD-1008", "Ngô Thị H", "09:00", OrderStatus.PREPARING, "2x Bún riêu cua, 2x Nước mía", 80_000L)
        )
    }

    fun setStatusFilter(status: OrderStatus) {
        _selectedStatus.value = status
    }

    fun advanceOrderStatus(orderId: String) {
        val currentList = _allOrders.value.orEmpty().toMutableList()
        val index = currentList.indexOfFirst { it.orderId == orderId }
        if (index != -1) {
            val order = currentList[index]
            val nextStatus = when (order.status) {
                OrderStatus.WAITING -> OrderStatus.PREPARING
                OrderStatus.PREPARING -> OrderStatus.READY
                OrderStatus.READY -> OrderStatus.READY // already final
            }
            currentList[index] = order.copy(status = nextStatus)
            _allOrders.value = currentList
        }
    }

    fun rejectOrder(orderId: String) {
        val currentList = _allOrders.value.orEmpty().toMutableList()
        val index = currentList.indexOfFirst { it.orderId == orderId }
        if (index != -1) {
            currentList.removeAt(index)
            _allOrders.value = currentList
        }
    }
}

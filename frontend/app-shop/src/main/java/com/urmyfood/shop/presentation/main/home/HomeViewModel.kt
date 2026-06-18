package com.urmyfood.shop.presentation.main.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HomeViewModel : ViewModel() {

    // Mock data - remove when BE ready
    data class ActiveOrder(
        val orderId: String,
        val time: String,
        val customerName: String,
        val status: OrderStatus,
        val itemsSummary: String,
        val totalPrice: Long
    )

    enum class OrderStatus(val label: String) {
        WAITING("Chờ xác nhận"),
        PREPARING("Đang chuẩn bị")
    }

    private val _salesAmount = MutableLiveData("3.500.000 VNĐ")
    val salesAmount: LiveData<String> = _salesAmount

    private val _orderCount = MutableLiveData(30)
    val orderCount: LiveData<Int> = _orderCount

    private val _activeOrders = MutableLiveData<List<ActiveOrder>>()
    val activeOrders: LiveData<List<ActiveOrder>> = _activeOrders

    init {
        loadMockData()
    }

    // Mock data - remove when BE ready
    private fun loadMockData() {
        _activeOrders.value = listOf(
            ActiveOrder(
                orderId = "#ORD-1001",
                time = "10:30",
                customerName = "Nguyễn Văn A",
                status = OrderStatus.WAITING,
                itemsSummary = "2x Cơm tấm sườn, 1x Trà đá",
                totalPrice = 85000L
            ),
            ActiveOrder(
                orderId = "#ORD-1002",
                time = "10:25",
                customerName = "Trần Thị B",
                status = OrderStatus.WAITING,
                itemsSummary = "1x Bún bò Huế, 1x Sữa đậu nành",
                totalPrice = 55000L
            ),
            ActiveOrder(
                orderId = "#ORD-1003",
                time = "10:15",
                customerName = "Lê Hoàng C",
                status = OrderStatus.PREPARING,
                itemsSummary = "3x Bánh mì thịt, 2x Cà phê sữa",
                totalPrice = 125000L
            ),
            ActiveOrder(
                orderId = "#ORD-1004",
                time = "10:10",
                customerName = "Phạm Minh D",
                status = OrderStatus.PREPARING,
                itemsSummary = "1x Phở bò chín, 1x Quẩy",
                totalPrice = 60000L
            ),
            ActiveOrder(
                orderId = "#ORD-1007",
                time = "09:15",
                customerName = "Hoàng Văn G",
                status = OrderStatus.WAITING,
                itemsSummary = "1x Hủ tiếu Nam Vang",
                totalPrice = 45000L
            ),
            ActiveOrder(
                orderId = "#ORD-1008",
                time = "09:00",
                customerName = "Ngô Thị H",
                status = OrderStatus.PREPARING,
                itemsSummary = "2x Bún riêu cua, 2x Nước mía",
                totalPrice = 80000L
            )
        )
    }

    fun advanceOrderStatus(orderId: String) {
        val currentList = _activeOrders.value.orEmpty().toMutableList()
        val index = currentList.indexOfFirst { it.orderId == orderId }
        if (index != -1) {
            val order = currentList[index]
            if (order.status == OrderStatus.WAITING) {
                currentList[index] = order.copy(status = OrderStatus.PREPARING)
                _activeOrders.value = currentList
            } else if (order.status == OrderStatus.PREPARING) {
                // If advanced from preparing, it becomes ready (completed) and is removed from active processing
                currentList.removeAt(index)
                _activeOrders.value = currentList
            }
        }
    }
}

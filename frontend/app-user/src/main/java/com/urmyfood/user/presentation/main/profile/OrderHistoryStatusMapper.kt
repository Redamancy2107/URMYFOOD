package com.urmyfood.user.presentation.main.profile

object OrderHistoryStatusMapper {
    const val TAB_PENDING = 0
    const val TAB_PROCESSING = 1
    const val TAB_DELIVERING = 2
    const val TAB_COMPLETED = 3
    const val TAB_CANCELLED = 4

    fun tabFor(orderStatus: String): Int = when (orderStatus) {
        "PENDING" -> TAB_PENDING
        "ACCEPTED", "PICKING_UP" -> TAB_PROCESSING
        "DELIVERING" -> TAB_DELIVERING
        "COMPLETED" -> TAB_COMPLETED
        "CANCELLED", "REJECTED", "EXPIRED" -> TAB_CANCELLED
        else -> TAB_CANCELLED
    }

    fun labelFor(orderStatus: String): String = when (orderStatus) {
        "PENDING" -> "Chờ xác nhận"
        "ACCEPTED" -> "Đã xác nhận"
        "PICKING_UP" -> "Đang lấy hàng"
        "DELIVERING" -> "Đang giao"
        "COMPLETED" -> "Hoàn thành"
        "CANCELLED" -> "Đã hủy"
        "REJECTED" -> "Đã từ chối"
        "EXPIRED" -> "Hết hạn"
        else -> orderStatus
    }
}

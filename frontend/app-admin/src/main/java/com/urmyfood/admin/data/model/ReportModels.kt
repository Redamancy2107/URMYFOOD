package com.urmyfood.admin.data.model

import com.google.gson.annotations.SerializedName

/** Store report from /api/v1/admin/reports/stores */
data class StoreReport(
    @SerializedName("id") val id: Long,
    @SerializedName("shopName") val shopName: String?,
    @SerializedName("email") val email: String?,
    @SerializedName("phone") val phone: String?,
    @SerializedName("totalRevenue") val totalRevenue: Double = 0.0,
    @SerializedName("totalOrders") val totalOrders: Long = 0,
    @SerializedName("completedOrders") val completedOrders: Long = 0,
    @SerializedName("cancelledOrders") val cancelledOrders: Long = 0
)

/** Customer report from /api/v1/admin/reports/customers */
data class CustomerReport(
    @SerializedName("id") val id: Long,
    @SerializedName("fullName") val fullName: String?,
    @SerializedName("email") val email: String?,
    @SerializedName("phone") val phone: String?,
    @SerializedName("totalSpent") val totalSpent: Double = 0.0,
    @SerializedName("totalOrders") val totalOrders: Long = 0,
    @SerializedName("completedOrders") val completedOrders: Long = 0,
    @SerializedName("createdAt") val createdAt: String?
)

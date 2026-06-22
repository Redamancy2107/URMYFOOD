package com.urmyfood.admin.data.model

import com.google.gson.annotations.SerializedName

/** Dashboard overview data from /api/v1/admin/dashboard */
data class DashboardOverview(
    @SerializedName("totalRevenue") val totalRevenue: Double = 0.0,
    @SerializedName("newOrders") val newOrders: Long = 0,
    @SerializedName("newUsers") val newUsers: Long = 0,
    @SerializedName("activeShops") val activeShops: Long = 0,
    @SerializedName("monthlyRevenueGrowth") val monthlyRevenueGrowth: List<MonthlyRevenue> = emptyList(),
    @SerializedName("recentActivities") val recentActivities: List<RecentActivity> = emptyList(),
    @SerializedName("latestShops") val latestShops: List<LatestShop> = emptyList()
)

data class MonthlyRevenue(
    @SerializedName("month") val month: Int,
    @SerializedName("revenue") val revenue: Double
)

data class RecentActivity(
    @SerializedName("type") val type: String,
    @SerializedName("description") val description: String,
    @SerializedName("time") val time: String
)

data class LatestShop(
    @SerializedName("id") val id: Long,
    @SerializedName("shopName") val shopName: String?,
    @SerializedName("email") val email: String?,
    @SerializedName("status") val status: String?,
    @SerializedName("createdAt") val createdAt: String?
)

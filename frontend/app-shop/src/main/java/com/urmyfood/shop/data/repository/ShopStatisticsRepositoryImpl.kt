package com.urmyfood.shop.data.repository

import com.google.gson.Gson
import com.urmyfood.shared.data.model.ApiResponse
import com.urmyfood.shared.domain.model.Result
import com.urmyfood.shop.data.model.toDomain
import com.urmyfood.shop.data.remote.ShopStatisticsApiService
import com.urmyfood.shop.domain.model.ShopStatistics
import com.urmyfood.shop.domain.model.ShopStatisticsPeriod
import com.urmyfood.shop.domain.repository.ShopStatisticsRepository

class ShopStatisticsRepositoryImpl(
    private val apiService: ShopStatisticsApiService
) : ShopStatisticsRepository {

    private val gson = Gson()

    override suspend fun getStatistics(
        token: String,
        period: ShopStatisticsPeriod,
        date: String?,
        month: String?,
        year: String?
    ): Result<ShopStatistics> {
        return try {
            val response = apiService.getStatistics(token, period.name, date, month, year)
            if (response.isSuccessful) {
                val body = response.body()
                val data = body?.data
                if (body?.success == true && data != null) {
                    Result.Success(data.toDomain())
                } else {
                    Result.Error(body?.message ?: "Không thể lấy thống kê cửa hàng")
                }
            } else {
                Result.Error(parseErrorMessage(response.errorBody()?.string()) ?: "Lỗi máy chủ: ${response.code()}", response.code())
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Lỗi kết nối")
        }
    }

    private fun parseErrorMessage(errorBodyStr: String?): String? {
        return try {
            gson.fromJson(errorBodyStr, ApiResponse::class.java)?.message
        } catch (e: Exception) {
            null
        }
    }
}

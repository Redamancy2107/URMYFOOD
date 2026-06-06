package com.urmyfood.shop.data.repository

import com.google.gson.Gson
import com.urmyfood.shared.data.model.ApiResponse
import com.urmyfood.shared.domain.model.Result
import com.urmyfood.shop.data.model.ShopVerificationRequest
import com.urmyfood.shop.data.remote.ShopVerificationApiService
import com.urmyfood.shop.domain.model.ShopRegistrationData
import com.urmyfood.shop.domain.repository.ShopVerificationRepository

class ShopVerificationRepositoryImpl(
    private val apiService: ShopVerificationApiService
) : ShopVerificationRepository {

    private val gson = Gson()

    override suspend fun submitVerification(token: String, data: ShopRegistrationData): Result<Unit> {
        return try {
            val response = apiService.submitVerification(
                authorization = "Bearer $token",
                request = data.toRequest()
            )
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success) {
                    Result.Success(Unit)
                } else {
                    Result.Error(body?.message ?: "Gửi hồ sơ xác minh thất bại", response.code())
                }
            } else {
                val message = parseErrorMessage(response.errorBody()?.string())
                Result.Error(message ?: "Gửi hồ sơ xác minh thất bại", response.code())
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Lỗi kết nối. Vui lòng thử lại.")
        }
    }

    private fun ShopRegistrationData.toRequest() = ShopVerificationRequest(
        shopName = shopName,
        category = category.name,
        address = address,
        latitude = latitude,
        longitude = longitude,
        cccdFrontUrl = cccdFrontUri.orEmpty(),
        cccdBackUrl = cccdBackUri.orEmpty(),
        shopPhotoUrls = shopPhotoUris
    )

    private fun parseErrorMessage(errorBodyStr: String?): String? {
        return try {
            gson.fromJson(errorBodyStr, ApiResponse::class.java)?.message
        } catch (e: Exception) {
            null
        }
    }
}

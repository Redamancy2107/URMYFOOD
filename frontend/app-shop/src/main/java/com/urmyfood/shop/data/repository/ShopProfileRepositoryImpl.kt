package com.urmyfood.shop.data.repository

import com.google.gson.Gson
import com.urmyfood.shared.data.model.ApiResponse
import com.urmyfood.shared.domain.model.Result
import com.urmyfood.shop.data.model.toDomain
import com.urmyfood.shop.data.model.toRequest
import com.urmyfood.shop.data.remote.ShopProfileApiService
import com.urmyfood.shop.domain.model.ShopProfile
import com.urmyfood.shop.domain.model.ShopProfileImageType
import com.urmyfood.shop.domain.repository.ShopProfileRepository
import okhttp3.MultipartBody

class ShopProfileRepositoryImpl(
    private val apiService: ShopProfileApiService
) : ShopProfileRepository {

    private val gson = Gson()

    override suspend fun getMyProfile(token: String): Result<ShopProfile> {
        return try {
            val response = apiService.getMyProfile(token)
            if (response.isSuccessful) {
                val body = response.body()
                val data = body?.data
                if (body?.success == true && data != null) {
                    Result.Success(data.toDomain())
                } else {
                    Result.Error(body?.message ?: "Không thể lấy hồ sơ quán")
                }
            } else {
                Result.Error(parseErrorMessage(response.errorBody()?.string()) ?: "Lỗi máy chủ: ${response.code()}", response.code())
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Lỗi kết nối")
        }
    }

    override suspend fun updateMyProfile(token: String, profile: ShopProfile): Result<ShopProfile> {
        return try {
            val response = apiService.updateMyProfile(token, profile.toRequest())
            if (response.isSuccessful) {
                val body = response.body()
                val data = body?.data
                if (body?.success == true && data != null) {
                    Result.Success(data.toDomain())
                } else {
                    Result.Error(body?.message ?: "Không thể cập nhật hồ sơ quán")
                }
            } else {
                Result.Error(parseErrorMessage(response.errorBody()?.string()) ?: "Lỗi máy chủ: ${response.code()}", response.code())
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Lỗi kết nối")
        }
    }

    override suspend fun uploadProfileImage(
        token: String,
        type: ShopProfileImageType,
        file: MultipartBody.Part
    ): Result<String> {
        return try {
            val response = apiService.uploadProfileImage(token, type.name, file)
            if (response.isSuccessful) {
                val body = response.body()
                val imageUrl = body?.data?.imageUrl
                if (body?.success == true && !imageUrl.isNullOrBlank()) {
                    Result.Success(imageUrl)
                } else {
                    Result.Error(body?.message ?: "Không thể tải ảnh hồ sơ")
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

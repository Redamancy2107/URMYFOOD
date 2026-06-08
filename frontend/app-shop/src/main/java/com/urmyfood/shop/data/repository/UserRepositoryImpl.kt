package com.urmyfood.shop.data.repository

import com.urmyfood.shared.domain.model.Result
import com.urmyfood.shop.data.model.ChangePasswordRequest
import com.urmyfood.shop.data.model.UpdateProfileRequest
import com.urmyfood.shop.data.model.toDomain
import com.urmyfood.shop.data.remote.UserApiService
import com.urmyfood.shop.domain.model.UserProfile
import com.urmyfood.shop.domain.repository.UserRepository

class UserRepositoryImpl(private val userApiService: UserApiService) : UserRepository {
    override suspend fun getMyProfile(token: String): Result<UserProfile> {
        return try {
            val response = userApiService.getMyProfile(token)
            if (response.isSuccessful) {
                val body = response.body()
                val data = body?.data
                if (body?.success == true && data != null) {
                    Result.Success(data.toDomain())
                } else {
                    Result.Error(body?.message ?: "Không thể lấy thông tin tài khoản")
                }
            } else {
                Result.Error("Lỗi máy chủ: ${response.code()}")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Lỗi kết nối")
        }
    }

    override suspend fun updateProfile(
        token: String,
        fullName: String?,
        phone: String?,
        avatarUrl: String?
    ): Result<UserProfile> {
        return try {
            val request = UpdateProfileRequest(fullName, phone, avatarUrl)
            val response = userApiService.updateProfile(token, request)
            if (response.isSuccessful) {
                val body = response.body()
                val data = body?.data
                if (body?.success == true && data != null) {
                    Result.Success(data.toDomain())
                } else {
                    Result.Error(body?.message ?: "Không thể cập nhật thông tin tài khoản")
                }
            } else {
                Result.Error("Lỗi máy chủ: ${response.code()}")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Lỗi kết nối")
        }
    }

    override suspend fun changePassword(
        token: String,
        currentPass: String,
        newPass: String
    ): Result<Unit> {
        return try {
            val request = ChangePasswordRequest(currentPass, newPass)
            val response = userApiService.changePassword(token, request)
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true) {
                    Result.Success(Unit)
                } else {
                    Result.Error(body?.message ?: "Đổi mật khẩu thất bại")
                }
            } else {
                Result.Error("Lỗi máy chủ: ${response.code()}")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Lỗi kết nối")
        }
    }
}

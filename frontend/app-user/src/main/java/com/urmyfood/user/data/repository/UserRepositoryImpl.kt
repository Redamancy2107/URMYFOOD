package com.urmyfood.user.data.repository

import com.urmyfood.user.data.model.toDomain
import com.urmyfood.user.data.remote.UserApiService
import com.urmyfood.user.domain.model.Result
import com.urmyfood.user.domain.model.UserProfile
import com.urmyfood.user.domain.repository.UserRepository

class UserRepositoryImpl(private val userApiService: UserApiService) : UserRepository {
    override suspend fun getMyProfile(token: String): Result<UserProfile> {
        return try {
            val response = userApiService.getMyProfile(token)
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true && body.data != null) {
                    Result.Success(body.data.toDomain())
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
            val request = com.urmyfood.user.data.model.UpdateProfileRequest(fullName, phone, avatarUrl)
            val response = userApiService.updateProfile(token, request)
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true && body.data != null) {
                    Result.Success(body.data.toDomain())
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
            val request = com.urmyfood.user.data.model.ChangePasswordRequest(currentPass, newPass)
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

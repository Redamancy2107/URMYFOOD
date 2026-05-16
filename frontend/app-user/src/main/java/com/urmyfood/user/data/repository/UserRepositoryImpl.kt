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
}

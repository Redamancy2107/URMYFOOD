package com.urmyfood.admin.data.repository

import com.urmyfood.admin.data.model.AdminProfile
import com.urmyfood.admin.data.network.RetrofitClient

class AdminRepository {
    private val api = RetrofitClient.api

    suspend fun getAdminProfile(accountId: Long): Result<AdminProfile> {
        return try {
            val response = api.getAdminProfile(accountId)
            if (response.isSuccessful) {
                val profile = response.body()
                if (profile != null) {
                    Result.success(profile)
                } else {
                    Result.failure(Exception("Admin profile not found"))
                }
            } else {
                Result.failure(Exception("Error fetching admin profile: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateAdminProfile(accountId: Long, updates: Map<String, String>): Result<Unit> {
        return try {
            val response = api.updateAdminProfile(accountId, updates)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Error updating admin profile: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

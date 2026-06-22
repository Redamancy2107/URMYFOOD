package com.urmyfood.admin.data.repository

import com.urmyfood.admin.data.model.*
import com.urmyfood.admin.data.network.RetrofitClient
import com.google.gson.Gson
import com.google.gson.JsonParser
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

/**
 * Repository that mediates between the AdminApi network layer and the UI.
 * All methods return Result<T> for clean error handling.
 */
class AdminRepository {
    private val api = RetrofitClient.api
    private val gson = Gson()

    // ── Authentication ──────────────────────────────────────────────────

    suspend fun sendOtp(email: String): Result<Unit> = safeApiCall {
        api.sendAdminOtp(OtpRequest(email = email))
    }

    suspend fun loginOtp(email: String, code: String): Result<AuthResponse> = safeApiCallWithData {
        api.loginAdminOtp(OtpLoginRequest(email = email, code = code))
    }

    // ── Admin Profile ───────────────────────────────────────────────────

    suspend fun getAdminProfile(): Result<AdminProfile> = safeApiCallWithData {
        api.getAdminProfile()
    }

    suspend fun changePassword(request: ChangePasswordRequest): Result<Unit> = safeApiCall {
        api.changePassword(request)
    }

    suspend fun updateAdminProfile(updates: Map<String, String>): Result<AdminProfile> = safeApiCallWithData {
        api.updateAdminProfile(updates)
    }

    suspend fun uploadAvatar(file: File): Result<AdminProfile> = safeApiCallWithData {
        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
        api.uploadAvatar(body)
    }

    // ── Dashboard ───────────────────────────────────────────────────────

    suspend fun getDashboardOverview(): Result<DashboardOverview> = safeApiCallWithData {
        api.getDashboardOverview()
    }

    // ── Partner Verification ────────────────────────────────────────────

    suspend fun getPendingVerifications(): Result<List<ShopVerification>> = safeApiCallWithData {
        api.getPendingVerifications()
    }

    suspend fun approveVerification(id: Long): Result<Unit> = safeApiCall {
        api.approveVerification(id)
    }

    suspend fun rejectVerification(id: Long, reason: String): Result<Unit> = safeApiCall {
        api.rejectVerification(id, mapOf("reason" to reason))
    }

    // ── Account Management ──────────────────────────────────────────────

    suspend fun getAllAccounts(
        page: Int = 0,
        size: Int = 20,
        role: String? = null
    ): Result<PageResponse<AccountProfile>> = safeApiCallWithData {
        api.getAllAccounts(page, size, role)
    }

    suspend fun lockUnlockAccount(id: Long, active: Boolean): Result<Unit> = safeApiCall {
        api.lockUnlockAccount(id, active)
    }

    // ── Content Moderation ──────────────────────────────────────────────

    suspend fun getAllPosts(page: Int = 0, size: Int = 20): Result<PageResponse<PostItem>> =
        safeApiCallWithData {
            api.getAllPosts(page, size)
        }

    suspend fun moderatePostStatus(postId: String, status: String): Result<Unit> = safeApiCall {
        api.moderatePostStatus(postId, status)
    }

    suspend fun deletePost(postId: String): Result<Unit> = safeApiCall {
        api.deletePost(postId)
    }

    // ── Voucher Management ──────────────────────────────────────────────

    suspend fun getAllVouchers(): Result<List<VoucherItem>> = safeApiCallWithData {
        api.getAllVouchers()
    }

    suspend fun createVoucher(voucher: VoucherItem): Result<VoucherItem> = safeApiCallWithData {
        api.createVoucher(voucher)
    }

    suspend fun deleteVoucher(id: Long): Result<Unit> = safeApiCall {
        api.deleteVoucher(id)
    }

    // ── Reports ─────────────────────────────────────────────────────────

    suspend fun getStoreReports(): Result<List<StoreReport>> = safeApiCallWithData {
        api.getStoreReports()
    }

    suspend fun getCustomerReports(): Result<List<CustomerReport>> = safeApiCallWithData {
        api.getCustomerReports()
    }

    // ── Private Helpers ─────────────────────────────────────────────────

    private suspend fun <T> safeApiCallWithData(
        call: suspend () -> retrofit2.Response<ApiResponse<T>>
    ): Result<T> {
        return try {
            val response = call()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success && body.data != null) {
                    Result.success(body.data)
                } else {
                    Result.failure(Exception(body?.message ?: "Không có dữ liệu"))
                }
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()?.string())
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun safeApiCall(
        call: suspend () -> retrofit2.Response<ApiResponse<Void>>
    ): Result<Unit> {
        return try {
            val response = call()
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()?.string())
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseErrorMessage(errorBody: String?): String {
        return try {
            val json = JsonParser.parseString(errorBody).asJsonObject
            json.get("message")?.asString ?: "Đã xảy ra lỗi"
        } catch (e: Exception) {
            "Lỗi kết nối máy chủ"
        }
    }
}

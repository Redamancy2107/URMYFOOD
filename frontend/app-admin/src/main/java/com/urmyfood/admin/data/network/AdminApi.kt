package com.urmyfood.admin.data.network

import com.urmyfood.admin.data.model.*
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

/**
 * Retrofit API interface for all Admin endpoints.
 * Maps to backend AdminController + AuthController endpoints.
 */
interface AdminApi {

    // ── Authentication ──────────────────────────────────────────────────

    @POST("api/v1/auth/admin/send-otp")
    suspend fun sendAdminOtp(
        @Body request: OtpRequest
    ): Response<ApiResponse<Void>>

    @POST("api/v1/auth/admin/login-otp")
    suspend fun loginAdminOtp(
        @Body request: OtpLoginRequest
    ): Response<ApiResponse<AuthResponse>>

    // ── Admin Profile ───────────────────────────────────────────────────

    @GET("api/v1/admin/profile")
    suspend fun getAdminProfile(): Response<ApiResponse<AdminProfile>>

    @PUT("api/v1/accounts/me/change-password")
    suspend fun changePassword(
        @Body request: ChangePasswordRequest
    ): Response<ApiResponse<Void>>

    @PATCH("api/v1/admin/profile")
    suspend fun updateAdminProfile(
        @Body updates: Map<String, String>
    ): Response<ApiResponse<AdminProfile>>

    @Multipart
    @POST("api/v1/admin/avatar")
    suspend fun uploadAvatar(
        @Part file: MultipartBody.Part
    ): Response<ApiResponse<AdminProfile>>

    // ── Dashboard ───────────────────────────────────────────────────────

    @GET("api/v1/admin/dashboard")
    suspend fun getDashboardOverview(): Response<ApiResponse<DashboardOverview>>

    // ── Partner Verification (Duyệt Đối Tác) ───────────────────────────

    @GET("api/v1/admin/verifications/pending")
    suspend fun getPendingVerifications(): Response<ApiResponse<List<ShopVerification>>>

    @POST("api/v1/admin/verifications/{id}/approve")
    suspend fun approveVerification(
        @Path("id") id: Long
    ): Response<ApiResponse<Void>>

    @POST("api/v1/admin/verifications/{id}/reject")
    suspend fun rejectVerification(
        @Path("id") id: Long,
        @Body body: Map<String, String>
    ): Response<ApiResponse<Void>>

    // ── Account Management (Quản Lý Người Dùng & Cửa Hàng) ─────────────

    @GET("api/v1/admin/accounts")
    suspend fun getAllAccounts(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
        @Query("role") role: String? = null,
        @Query("sortBy") sortBy: String = "createdAt",
        @Query("sortDir") sortDir: String = "DESC"
    ): Response<ApiResponse<PageResponse<AccountProfile>>>

    @POST("api/v1/admin/accounts/{id}/lock-unlock")
    suspend fun lockUnlockAccount(
        @Path("id") id: Long,
        @Query("active") active: Boolean,
        @Query("reason") reason: String
    ): Response<ApiResponse<Void>>

    @DELETE("api/v1/admin/accounts/{id}")
    suspend fun deleteAccount(
        @Path("id") id: Long,
        @Query("reason") reason: String
    ): Response<ApiResponse<Void>>

    // ── Content Moderation (Kiểm Duyệt Nội Dung) ───────────────────────

    @GET("api/v1/admin/posts")
    suspend fun getAllPosts(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<ApiResponse<PageResponse<PostItem>>>

    @POST("api/v1/admin/posts/{postId}/status")
    suspend fun moderatePostStatus(
        @Path("postId") postId: String,
        @Query("status") status: String
    ): Response<ApiResponse<Void>>

    @DELETE("api/v1/admin/posts/{postId}")
    suspend fun deletePost(
        @Path("postId") postId: String
    ): Response<ApiResponse<Void>>

    // ── Voucher Management (Mã Giảm Giá) ───────────────────────────────

    @GET("api/v1/admin/vouchers")
    suspend fun getAllVouchers(): Response<ApiResponse<List<VoucherItem>>>

    @POST("api/v1/admin/vouchers")
    suspend fun createVoucher(
        @Body voucher: VoucherItem
    ): Response<ApiResponse<VoucherItem>>

    @DELETE("api/v1/admin/vouchers/{id}")
    suspend fun deleteVoucher(
        @Path("id") id: Long
    ): Response<ApiResponse<Void>>

    // ── Reports (Báo Cáo) ───────────────────────────────────────────────

    @GET("api/v1/admin/reports/stores")
    suspend fun getStoreReports(): Response<ApiResponse<List<StoreReport>>>

    @GET("api/v1/admin/reports/customers")
    suspend fun getCustomerReports(): Response<ApiResponse<List<CustomerReport>>>
}

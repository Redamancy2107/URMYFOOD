package com.urmyfood.user.data.repository

import com.urmyfood.user.data.model.VoucherResponse
import com.urmyfood.user.data.remote.VoucherApiService
import com.urmyfood.user.data.util.toUserMessage
import com.urmyfood.user.domain.model.Result
import com.urmyfood.user.domain.repository.VoucherRepository

class VoucherRepositoryImpl(
    private val voucherApiService: VoucherApiService
) : VoucherRepository {

    override suspend fun getActiveVouchers(): Result<List<VoucherResponse>> {
        return try {
            val response = voucherApiService.getActiveVouchers()
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true && body.data != null) {
                    Result.Success(body.data)
                } else {
                    Result.Error(body?.message ?: "Không thể lấy danh sách voucher")
                }
            } else {
                Result.Error("Lỗi máy chủ: ${response.code()}")
            }
        } catch (e: Exception) {
            Result.Error(e.toUserMessage())
        }
    }
}

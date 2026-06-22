package com.urmyfood.user.data.repository

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.urmyfood.user.data.model.ApiResponse
import com.urmyfood.user.data.model.SavedVoucherResponse
import com.urmyfood.user.data.model.VoucherResponse
import com.urmyfood.user.data.remote.VoucherApiService
import com.urmyfood.user.data.util.toUserMessage
import com.urmyfood.user.domain.model.Result
import com.urmyfood.user.domain.repository.VoucherRepository
import kotlinx.coroutines.CancellationException
import retrofit2.Response

class VoucherRepositoryImpl(
    private val voucherApiService: VoucherApiService
) : VoucherRepository {

    private val gson = Gson()

    override suspend fun getActiveVouchers(token: String?): Result<List<VoucherResponse>> {
        return voucherListCall { voucherApiService.getActiveVouchers(token) }
    }

    override suspend fun getSavedVouchers(token: String): Result<List<VoucherResponse>> {
        return voucherListCall { voucherApiService.getSavedVouchers(token) }
    }

    override suspend fun saveVoucher(token: String, voucherId: Long): Result<SavedVoucherResponse> {
        return savedVoucherCall { voucherApiService.saveVoucher(token, voucherId) }
    }

    override suspend fun unsaveVoucher(token: String, voucherId: Long): Result<SavedVoucherResponse> {
        return savedVoucherCall { voucherApiService.unsaveVoucher(token, voucherId) }
    }

    private suspend fun voucherListCall(call: suspend () -> Response<ApiResponse<List<VoucherResponse>>>): Result<List<VoucherResponse>> {
        return try {
            val response = call()
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true && body.data != null) {
                    Result.Success(body.data)
                } else {
                    Result.Error(body?.message ?: "Khong the tai danh sach voucher")
                }
            } else {
                Result.Error(errorMessage(response, "Khong the tai danh sach voucher"))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.Error(e.toUserMessage())
        }
    }

    private suspend fun savedVoucherCall(call: suspend () -> Response<ApiResponse<SavedVoucherResponse>>): Result<SavedVoucherResponse> {
        return try {
            val response = call()
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true && body.data != null) {
                    Result.Success(body.data)
                } else {
                    Result.Error(body?.message ?: "Khong the cap nhat voucher")
                }
            } else {
                Result.Error(errorMessage(response, "Khong the cap nhat voucher"))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.Error(e.toUserMessage())
        }
    }

    private fun <T> errorMessage(response: Response<ApiResponse<T>>, fallback: String): String {
        val raw = response.errorBody()?.string()
        if (raw.isNullOrBlank()) {
            return "$fallback (${response.code()})"
        }
        return runCatching {
            val type = object : TypeToken<ApiResponse<Any>>() {}.type
            val body: ApiResponse<Any> = gson.fromJson(raw, type)
            body.message?.takeIf { it.isNotBlank() } ?: "$fallback (${response.code()})"
        }.getOrElse {
            "$fallback (${response.code()})"
        }
    }
}

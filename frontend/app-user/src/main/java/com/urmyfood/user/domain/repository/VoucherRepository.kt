package com.urmyfood.user.domain.repository

import com.urmyfood.user.data.model.VoucherResponse
import com.urmyfood.user.domain.model.Result

interface VoucherRepository {
    suspend fun getActiveVouchers(token: String?): Result<List<VoucherResponse>>
    suspend fun getSavedVouchers(token: String): Result<List<VoucherResponse>>
    suspend fun saveVoucher(token: String, voucherId: Long): Result<com.urmyfood.user.data.model.SavedVoucherResponse>
    suspend fun unsaveVoucher(token: String, voucherId: Long): Result<com.urmyfood.user.data.model.SavedVoucherResponse>
}

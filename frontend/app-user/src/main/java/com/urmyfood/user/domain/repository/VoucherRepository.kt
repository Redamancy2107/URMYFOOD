package com.urmyfood.user.domain.repository

import com.urmyfood.user.data.model.VoucherResponse
import com.urmyfood.user.domain.model.Result

interface VoucherRepository {
    suspend fun getActiveVouchers(): Result<List<VoucherResponse>>
}

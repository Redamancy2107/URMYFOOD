package com.urmyfood.user.domain.usecase

import com.urmyfood.user.data.model.VoucherResponse
import com.urmyfood.user.domain.model.Result
import com.urmyfood.user.domain.repository.VoucherRepository

class GetVouchersUseCase(
    private val voucherRepository: VoucherRepository
) {
    suspend operator fun invoke(): Result<List<VoucherResponse>> {
        return voucherRepository.getActiveVouchers()
    }
}

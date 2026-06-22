package com.urmyfood.user.domain.usecase

import com.urmyfood.user.data.model.SavedVoucherResponse
import com.urmyfood.user.data.model.VoucherResponse
import com.urmyfood.user.domain.model.Result
import com.urmyfood.user.domain.model.TokenProvider
import com.urmyfood.user.domain.repository.VoucherRepository

class GetVouchersUseCase(
    private val voucherRepository: VoucherRepository,
    private val tokenProvider: TokenProvider
) {
    suspend operator fun invoke(): Result<List<VoucherResponse>> {
        val token = tokenProvider.getAccessToken()?.let { "Bearer $it" }
        return voucherRepository.getActiveVouchers(token)
    }
}

class GetSavedVouchersUseCase(
    private val voucherRepository: VoucherRepository,
    private val tokenProvider: TokenProvider
) {
    suspend operator fun invoke(): Result<List<VoucherResponse>> {
        val token = tokenProvider.getAccessToken() ?: return Result.Error("Vui long dang nhap")
        return voucherRepository.getSavedVouchers("Bearer $token")
    }
}

class SaveVoucherUseCase(
    private val voucherRepository: VoucherRepository,
    private val tokenProvider: TokenProvider
) {
    suspend operator fun invoke(voucherId: Long): Result<SavedVoucherResponse> {
        val token = tokenProvider.getAccessToken() ?: return Result.Error("Vui long dang nhap")
        return voucherRepository.saveVoucher("Bearer $token", voucherId)
    }
}

class UnsaveVoucherUseCase(
    private val voucherRepository: VoucherRepository,
    private val tokenProvider: TokenProvider
) {
    suspend operator fun invoke(voucherId: Long): Result<SavedVoucherResponse> {
        val token = tokenProvider.getAccessToken() ?: return Result.Error("Vui long dang nhap")
        return voucherRepository.unsaveVoucher("Bearer $token", voucherId)
    }
}

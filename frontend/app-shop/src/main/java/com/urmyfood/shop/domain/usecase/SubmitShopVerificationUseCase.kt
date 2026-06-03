package com.urmyfood.shop.domain.usecase

import com.urmyfood.shared.data.local.TokenStore
import com.urmyfood.shared.domain.model.Result
import com.urmyfood.shop.domain.model.ShopRegistrationData
import com.urmyfood.shop.domain.repository.ShopVerificationRepository

class SubmitShopVerificationUseCase(
    private val repository: ShopVerificationRepository,
    private val tokenStore: TokenStore
) {
    suspend operator fun invoke(data: ShopRegistrationData): Result<Unit> {
        val token = tokenStore.getAccessToken()
            ?: return Result.Error("Vui lòng đăng nhập để gửi hồ sơ xác minh")
        return repository.submitVerification(token, data)
    }
}

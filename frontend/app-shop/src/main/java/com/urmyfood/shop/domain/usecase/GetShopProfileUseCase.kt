package com.urmyfood.shop.domain.usecase

import com.urmyfood.shared.data.local.TokenStore
import com.urmyfood.shared.domain.model.Result
import com.urmyfood.shop.domain.model.ShopProfile
import com.urmyfood.shop.domain.repository.ShopProfileRepository

class GetShopProfileUseCase(
    private val repository: ShopProfileRepository,
    private val tokenStore: TokenStore
) {
    suspend operator fun invoke(): Result<ShopProfile> {
        val token = tokenStore.getAccessToken()
            ?: return Result.Error("Không tìm thấy token đăng nhập")
        return repository.getMyProfile("Bearer $token")
    }
}

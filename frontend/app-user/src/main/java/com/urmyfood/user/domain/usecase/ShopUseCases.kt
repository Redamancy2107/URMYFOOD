package com.urmyfood.user.domain.usecase

import com.urmyfood.user.data.model.ShopFollowResponse
import com.urmyfood.user.data.model.ShopProfileResponse
import com.urmyfood.user.domain.model.Result
import com.urmyfood.user.domain.model.TokenProvider
import com.urmyfood.user.domain.repository.ShopRepository

class GetShopProfileUseCase(
    private val repository: ShopRepository,
    private val tokenProvider: TokenProvider
) {
    suspend operator fun invoke(shopId: Long): Result<ShopProfileResponse> {
        val token = tokenProvider.getAccessToken() ?: return Result.Error("Vui lòng đăng nhập")
        return repository.getProfile("Bearer $token", shopId)
    }
}

class FollowShopUseCase(
    private val repository: ShopRepository,
    private val tokenProvider: TokenProvider
) {
    suspend operator fun invoke(shopId: Long): Result<ShopFollowResponse> {
        val token = tokenProvider.getAccessToken() ?: return Result.Error("Vui lòng đăng nhập")
        return repository.follow("Bearer $token", shopId)
    }
}

class UnfollowShopUseCase(
    private val repository: ShopRepository,
    private val tokenProvider: TokenProvider
) {
    suspend operator fun invoke(shopId: Long): Result<ShopFollowResponse> {
        val token = tokenProvider.getAccessToken() ?: return Result.Error("Vui lòng đăng nhập")
        return repository.unfollow("Bearer $token", shopId)
    }
}

package com.urmyfood.shop.domain.usecase

import com.urmyfood.shared.data.local.TokenStore
import com.urmyfood.shared.domain.model.Result
import com.urmyfood.shop.domain.model.ShopProfileImageType
import com.urmyfood.shop.domain.repository.ShopProfileRepository
import okhttp3.MultipartBody

class UploadShopProfileImageUseCase(
    private val repository: ShopProfileRepository,
    private val tokenStore: TokenStore
) {
    suspend operator fun invoke(type: ShopProfileImageType, file: MultipartBody.Part): Result<String> {
        val token = tokenStore.getAccessToken()
            ?: return Result.Error("Vui lòng đăng nhập lại")
        return repository.uploadProfileImage("Bearer $token", type, file)
    }
}

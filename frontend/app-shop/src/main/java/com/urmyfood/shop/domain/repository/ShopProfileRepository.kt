package com.urmyfood.shop.domain.repository

import com.urmyfood.shared.domain.model.Result
import com.urmyfood.shop.domain.model.ShopProfile
import com.urmyfood.shop.domain.model.ShopProfileImageType
import okhttp3.MultipartBody

interface ShopProfileRepository {
    suspend fun getMyProfile(token: String): Result<ShopProfile>
    suspend fun updateMyProfile(token: String, profile: ShopProfile): Result<ShopProfile>
    suspend fun uploadProfileImage(
        token: String,
        type: ShopProfileImageType,
        file: MultipartBody.Part
    ): Result<String>
}

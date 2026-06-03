package com.urmyfood.shop.domain.repository

import com.urmyfood.shared.domain.model.Result
import com.urmyfood.shop.domain.model.ShopRegistrationData

interface ShopVerificationRepository {
    suspend fun submitVerification(token: String, data: ShopRegistrationData): Result<Unit>
}

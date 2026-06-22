package com.urmyfood.user.domain.repository

import com.urmyfood.user.data.model.ShopFollowResponse
import com.urmyfood.user.data.model.ShopProfileResponse
import com.urmyfood.user.domain.model.Result

interface ShopRepository {
    suspend fun getProfile(token: String, shopId: Long): Result<ShopProfileResponse>
    suspend fun getFollowState(token: String, shopId: Long): Result<ShopFollowResponse>
    suspend fun follow(token: String, shopId: Long): Result<ShopFollowResponse>
    suspend fun unfollow(token: String, shopId: Long): Result<ShopFollowResponse>
}

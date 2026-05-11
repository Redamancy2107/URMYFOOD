package com.urmyfood.user.domain.repository

import com.urmyfood.user.domain.model.FoodPost
import com.urmyfood.user.domain.model.Result

interface PostRepository {
    suspend fun getPosts(): Result<List<FoodPost>>
}

package com.urmyfood.shop.presentation.main.posts

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

data class ShopPost(
    val postId: String,
    val dishName: String,
    val price: Long,
    val originalPrice: Long? = null,
    val content: String? = null,
    val imageUrl: String,
    val stock: Int,
    val maxStock: Int = stock,
    val isActive: Boolean,
    val isFlashSale: Boolean = false,
    val likeCount: Int = 0,
    val commentCount: Int = 0,
    val category: String? = "Món chính"
)

class PostsViewModel : ViewModel() {

    private val _posts = MutableLiveData<List<ShopPost>>()
    val posts: LiveData<List<ShopPost>> = _posts

    private val _searchQuery = MutableLiveData<String>()

    companion object {
        // Mock data - aligned with user app's mock posts
        val allPosts = mutableListOf(
            ShopPost(
                postId = "post_1",
                dishName = "Bún bò Huế đặc biệt",
                price = 40000L,
                originalPrice = 45000L,
                content = "Nước lèo đậm đà hầm xương ống 12 tiếng thơm lừng, thịt bò gầu nạm Úc thái mỏng chín mềm ngọt lịm.",
                imageUrl = "https://images.unsplash.com/photo-1625398407796-82650a8c135f?w=500",
                stock = 15,
                maxStock = 30,
                isActive = true,
                isFlashSale = false,
                likeCount = 12,
                commentCount = 3,
                category = "Bún/Phở"
            ),
            ShopPost(
                postId = "post_2",
                dishName = "Cơm tấm sườn bì chả",
                price = 35000L,
                originalPrice = 40000L,
                content = "Sự kết hợp hoàn hảo giữa cơm tấm truyền thống thơm dẻo và thịt ba chỉ nướng giòn rụm tẩm sốt Teriyaki đặc biệt.",
                imageUrl = "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=500",
                stock = 20,
                maxStock = 40,
                isActive = true,
                isFlashSale = true,
                likeCount = 28,
                commentCount = 7,
                category = "Cơm"
            ),
            ShopPost(
                postId = "post_3",
                dishName = "Trà sữa trân châu đường đen",
                price = 25000L,
                originalPrice = 35000L,
                content = "Trà sữa trân châu đường đen siêu béo, vị ngọt thanh từ đường đen tự nhiên, trân châu dai giòn sần sật ngập tràn.",
                imageUrl = "https://images.unsplash.com/photo-1541658016709-82535e94bc69?w=500",
                stock = 50,
                maxStock = 100,
                isActive = true,
                isFlashSale = true,
                likeCount = 89,
                commentCount = 5,
                category = "Trà sữa"
            ),
            ShopPost(
                postId = "post_4",
                dishName = "Bánh mì thịt nướng",
                price = 25000L,
                originalPrice = 25000L,
                content = "Bánh mì giòn rụm, thịt nướng thơm lừng đậm vị kết hợp dưa chua và nước sốt bí truyền đặc biệt ngọt ngào.",
                imageUrl = "https://images.unsplash.com/photo-1509722747041-616f39b57569?w=500",
                stock = 30,
                maxStock = 50,
                isActive = false,
                isFlashSale = false,
                likeCount = 5,
                commentCount = 0,
                category = "Bánh mì"
            ),
            ShopPost(
                postId = "post_5",
                dishName = "Gỏi cuốn tôm thịt",
                price = 30000L,
                originalPrice = 35000L,
                content = "Gỏi cuốn tôm thịt tươi ngon, rau sống thanh mát, ăn kèm nước chấm tương đậu phộng béo ngậy cực kỳ kích thích vị giác.",
                imageUrl = "https://images.unsplash.com/photo-1534422298391-e4f8c172dddb?w=500",
                stock = 8,
                maxStock = 15,
                isActive = true,
                isFlashSale = false,
                likeCount = 14,
                commentCount = 2,
                category = "Ăn vặt"
            )
        )
    }

    init {
        _posts.value = allPosts.toList()
    }

    fun toggleActive(postId: String) {
        val index = allPosts.indexOfFirst { it.postId == postId }
        if (index != -1) {
            val post = allPosts[index]
            allPosts[index] = post.copy(isActive = !post.isActive)
            filterPosts(_searchQuery.value.orEmpty())
        }
    }

    fun deletePost(postId: String) {
        val index = allPosts.indexOfFirst { it.postId == postId }
        if (index != -1) {
            allPosts.removeAt(index)
            filterPosts(_searchQuery.value.orEmpty())
        }
    }

    fun updateStock(postId: String, newStock: Int) {
        val index = allPosts.indexOfFirst { it.postId == postId }
        if (index != -1) {
            val post = allPosts[index]
            allPosts[index] = post.copy(stock = newStock)
        }
    }

    fun filterPosts(query: String) {
        _searchQuery.value = query
        if (query.isBlank()) {
            _posts.value = allPosts.toList()
        } else {
            _posts.value = allPosts.filter {
                it.dishName.contains(query, ignoreCase = true)
            }
        }
    }
}

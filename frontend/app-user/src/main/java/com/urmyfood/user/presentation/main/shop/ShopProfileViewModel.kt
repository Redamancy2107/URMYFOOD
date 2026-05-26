package com.urmyfood.user.presentation.main.shop

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.urmyfood.user.domain.model.FoodPost

data class ShopVoucher(
    val voucherId: String,
    val title: String,
    val description: String,
    val expiryDate: String
)

class ShopProfileViewModel : ViewModel() {

    private val _shopName = MutableLiveData<String>()
    val shopName: LiveData<String> = _shopName

    private val _shopAvatarUrl = MutableLiveData<String?>()
    val shopAvatarUrl: LiveData<String?> = _shopAvatarUrl

    private val _followers = MutableLiveData<String>()
    val followers: LiveData<String> = _followers

    private val _isFollowing = MutableLiveData<Boolean>()
    val isFollowing: LiveData<Boolean> = _isFollowing

    private val _productsCount = MutableLiveData<Int>()
    val productsCount: LiveData<Int> = _productsCount

    private val _responseRate = MutableLiveData<String>()
    val responseRate: LiveData<String> = _responseRate

    private val _activeDuration = MutableLiveData<String>()
    val activeDuration: LiveData<String> = _activeDuration

    private val _vouchers = MutableLiveData<List<ShopVoucher>>()
    val vouchers: LiveData<List<ShopVoucher>> = _vouchers

    private val _posts = MutableLiveData<List<FoodPost>>()
    val posts: LiveData<List<FoodPost>> = _posts

    private val _products = MutableLiveData<List<FoodPost>>()
    val products: LiveData<List<FoodPost>> = _products

    private val _categories = MutableLiveData<List<String>>()
    val categories: LiveData<List<String>> = _categories

    // Shop metadata required by SRS (FR_SHOP_010)
    private val _address = MutableLiveData<String>()
    val address: LiveData<String> = _address

    private val _operatingHours = MutableLiveData<String>()
    val operatingHours: LiveData<String> = _operatingHours

    private val _rating = MutableLiveData<String>()
    val rating: LiveData<String> = _rating

    // Filter-related states (clean architecture for BE integration)
    private val _selectedCategory = MutableLiveData<String?>()
    val selectedCategory: LiveData<String?> = _selectedCategory

    private val allProducts = mutableListOf<FoodPost>()

    fun initShop(name: String, avatarUrl: String?) {
        _shopName.value = name
        _shopAvatarUrl.value = avatarUrl

        // Mock shop metadata - remove when BE ready
        _address.value = when (name) {
            "Tiệm Trà Sữa Mây" -> "12 Cao Thắng, Phường 5, Quận 3, TP. Hồ Chí Minh"
            "Cơm Tấm Bụi" -> "378 Lê Văn Sỹ, Phường 14, Quận 3, TP. Hồ Chí Minh"
            "Quán Bà Chiểu" -> "Bùi Hữu Nghĩa, Phường 1, Quận Bình Thạnh, TP. Hồ Chí Minh"
            else -> "145 Nguyễn Huệ, Phường Bến Nghé, Quận 1, TP. Hồ Chí Minh"
        }
        
        _operatingHours.value = when (name) {
            "Tiệm Trà Sữa Mây" -> "08:00 - 22:00"
            "Cơm Tấm Bụi" -> "06:30 - 21:00"
            "Quán Bà Chiểu" -> "07:00 - 22:30"
            else -> "09:00 - 21:30"
        }

        _rating.value = when (name) {
            "Tiệm Trà Sữa Mây" -> "4.7"
            "Cơm Tấm Bụi" -> "4.5"
            "Quán Bà Chiểu" -> "4.8"
            else -> "4.6"
        }
        
        // Generate mock shop stats based on shop name
        val hash = name.hashCode().let { if (it < 0) -it else it }
        val mockFollowersCount = 500 + (hash % 4500)
        _followers.value = String.format("%.1fk Người theo dõi", mockFollowersCount / 1000f)
        _isFollowing.value = hash % 2 == 0
        
        val mockProdCount = 15 + (hash % 45)
        _productsCount.value = mockProdCount
        
        _responseRate.value = "${90 + (hash % 10)}%"
        _activeDuration.value = "${5 + (hash % 25)}p"

        // Mock vouchers
        _vouchers.value = listOf(
            ShopVoucher("v1", "Giảm 10%", "Tối đa 50K", "HSD: 15/12/2026"),
            ShopVoucher("v2", "Freeship 15K", "Đơn từ 100K", "HSD: 31/12/2026"),
            ShopVoucher("v3", "Giảm 20K", "Cho khách mới", "HSD: 30/06/2026")
        )

        // Mock posts
        val mockPosts = mutableListOf<FoodPost>()
        for (i in 1..3) {
            val dishName = when (name) {
                "Tiệm Trà Sữa Mây" -> if (i == 1) "Trà sữa trân châu đường đen" else if (i == 2) "Trà sữa matcha đậu đỏ" else "Trà hoa quả nhiệt đới"
                "Cơm Tấm Bụi" -> if (i == 1) "Cơm tấm sườn bì chả" else if (i == 2) "Cơm tấm gà nướng" else "Cơm tấm ba chỉ cháy cạnh"
                "Quán Bà Chiểu" -> if (i == 1) "Bún bò Huế đặc biệt" else if (i == 2) "Mỳ Quảng gà ta" else "Bún thịt nướng chả giò"
                else -> if (i == 1) "Cơm Gà Hải Nam đặc biệt" else if (i == 2) "Cơm gà xối mỡ" else "Cơm gà quay tiêu"
            }
            val price = when (i) {
                1 -> 35000.0
                2 -> 28000.0
                else -> 30000.0
            }
            val originalPrice = price + 10000.0
            mockPosts.add(
                FoodPost(
                    postId = "shop_${hash}_post_$i",
                    dishName = dishName,
                    price = price,
                    originalPrice = originalPrice,
                    maxQuantity = 50,
                    remainingQuantity = 12 + i * 5,
                    endTime = if (i == 1) "16:00" else null,
                    isFlashSale = i == 1,
                    status = "ACTIVE",
                    content = if (i == 1) "⚡ FLASH SALE GIỜ VÀNG - Món siêu ngon giá rẻ tụt quần, mua ngay kẻo lỡ!" else "Món bán chạy nhất của quán ngày hôm nay. Đảm bảo vệ sinh an toàn thực phẩm.",
                    imageUrl = when (name) {
                        "Tiệm Trà Sữa Mây" -> "https://images.unsplash.com/photo-1541658016709-82535e94bc69?w=500"
                        "Cơm Tấm Bụi" -> "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=500"
                        "Quán Bà Chiểu" -> "https://images.unsplash.com/photo-1625398407796-82650a8c135f?w=500"
                        else -> "https://images.unsplash.com/photo-1562967914-608f82629710?w=500"
                    },
                    shopName = name,
                    shopAvatarUrl = avatarUrl
                )
            )
        }
        _posts.value = mockPosts

        // Mock categories - remove when BE ready
        val categoriesList = when (name) {
            "Tiệm Trà Sữa Mây" -> listOf("Tất cả", "Trà Sữa Truyền Thống", "Trà Trái Cây", "Đồ Uống Đá Xay", "Toppings Thêm")
            "Cơm Tấm Bụi" -> listOf("Tất cả", "Cơm Tấm Đặc Biệt", "Món Ăn Kèm", "Canh Nóng", "Đồ Uống Giải Khát")
            "Quán Bà Chiểu" -> listOf("Tất cả", "Bún & Phở Nước", "Món Trộn & Khô", "Mỳ Quảng", "Trà Đá & Khăn Lạnh")
            else -> listOf("Tất cả", "Cơm Gà Các Loại", "Món Gỏi & Salad", "Canh & Món Thêm", "Giải Khát")
        }
        _categories.value = categoriesList

        // Mock products (grid of menu dishes) - remove when BE ready
        allProducts.clear()
        val dishesList = when (name) {
            "Tiệm Trà Sữa Mây" -> listOf("Hồng trà sữa", "Lục trà nhài", "Trà sữa thái xanh", "Trà sữa khoai môn", "Trà đào cam sả", "Trà dâu tây")
            "Cơm Tấm Bụi" -> listOf("Cơm tấm sườn", "Cơm tấm bì chả", "Cơm tấm đùi gà", "Cơm tấm trứng ốp la", "Cơm tấm xá xíu", "Canh khổ qua nhồi thịt")
            "Quán Bà Chiểu" -> listOf("Bún bò tái nạm", "Bún giò heo", "Bún mọc sườn", "Bún thịt nướng", "Mỳ Quảng tôm thịt", "Phở bò chín")
            else -> listOf("Cơm gà luộc", "Cơm gà xối mỡ", "Cơm gà nướng mật ong", "Gỏi gà xé phay", "Canh lá giang", "Nước sâm dứa")
        }
        dishesList.forEachIndexed { index, dish ->
            val productCategory = when (dish) {
                // Tiệm Trà Sữa Mây
                "Hồng trà sữa", "Trà sữa thái xanh", "Trà sữa khoai môn" -> "Trà Sữa Truyền Thống"
                "Lục trà nhài", "Trà đào cam sả", "Trà dâu tây" -> "Trà Trái Cây"
                
                // Cơm Tấm Bụi
                "Cơm tấm sườn", "Cơm tấm bì chả", "Cơm tấm đùi gà", "Cơm tấm trứng ốp la", "Cơm tấm xá xíu" -> "Cơm Tấm Đặc Biệt"
                "Canh khổ qua nhồi thịt" -> "Canh Nóng"
                
                // Quán Bà Chiểu
                "Bún bò tái nạm", "Bún giò heo", "Bún mọc sườn", "Phở bò chín" -> "Bún & Phở Nước"
                "Bún thịt nướng" -> "Món Trộn & Khô"
                "Mỳ Quảng tôm thịt" -> "Mỳ Quảng"
                
                // Other / Cơm Gà
                "Cơm gà luộc", "Cơm gà xối mỡ", "Cơm gà nướng mật ong" -> "Cơm Gà Các Loại"
                "Gỏi gà xé phay" -> "Món Gỏi & Salad"
                "Canh lá giang" -> "Canh & Món Thêm"
                "Nước sâm dứa" -> "Giải Khát"
                
                else -> categoriesList.getOrNull(1) ?: ""
            }
            allProducts.add(
                FoodPost(
                    postId = "shop_${hash}_prod_$index",
                    dishName = dish,
                    price = 25000.0 + (index * 2000),
                    originalPrice = 25000.0 + (index * 2000),
                    maxQuantity = 100,
                    remainingQuantity = 45,
                    endTime = null,
                    isFlashSale = false,
                    status = "ACTIVE",
                    content = null,
                    imageUrl = when (name) {
                        "Tiệm Trà Sữa Mây" -> "https://images.unsplash.com/photo-1541658016709-82535e94bc69?w=500"
                        "Cơm Tấm Bụi" -> "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=500"
                        "Quán Bà Chiểu" -> "https://images.unsplash.com/photo-1625398407796-82650a8c135f?w=500"
                        else -> "https://images.unsplash.com/photo-1562967914-608f82629710?w=500"
                    },
                    shopName = name,
                    shopAvatarUrl = avatarUrl,
                    category = productCategory
                )
            )
        }
        _products.value = allProducts
    }

    /**
     * Filter products by category name.
     * This logic is fully encapsulated in ViewModel to ensure that when Backend APIs are integrated,
     * developers can easily swap local filtering with network requests without breaking UI components.
     */
    fun filterProductsByCategory(categoryName: String?) {
        _selectedCategory.value = categoryName
        if (categoryName == null || categoryName == "Tất cả") {
            _products.value = allProducts
        } else {
            _products.value = allProducts.filter { it.category == categoryName }
        }
    }

    fun toggleFollow() {
        _isFollowing.value = _isFollowing.value?.not()
    }

    fun toggleLike(postId: String) {
        val currentPosts = _posts.value ?: return
        val updatedPosts = currentPosts.map { post ->
            if (post.postId == postId) {
                val newIsLiked = !post.isLiked
                val newLikeCount = if (newIsLiked) post.likeCount + 1 else (post.likeCount - 1).coerceAtLeast(0)
                post.copy(isLiked = newIsLiked, likeCount = newLikeCount)
            } else {
                post
            }
        }
        _posts.value = updatedPosts
    }
}

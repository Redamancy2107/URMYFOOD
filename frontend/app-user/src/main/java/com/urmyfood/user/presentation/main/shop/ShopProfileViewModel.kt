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

    fun initShop(name: String, avatarUrl: String?) {
        _shopName.value = name
        _shopAvatarUrl.value = avatarUrl
        
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

        // Mock products (grid of menu dishes)
        val mockProducts = mutableListOf<FoodPost>()
        val dishesList = when (name) {
            "Tiệm Trà Sữa Mây" -> listOf("Hồng trà sữa", "Lục trà nhài", "Trà sữa thái xanh", "Trà sữa khoai môn", "Trà đào cam sả", "Trà dâu tây")
            "Cơm Tấm Bụi" -> listOf("Cơm tấm sườn", "Cơm tấm bì chả", "Cơm tấm đùi gà", "Cơm tấm trứng ốp la", "Cơm tấm xá xíu", "Canh khổ qua nhồi thịt")
            "Quán Bà Chiểu" -> listOf("Bún bò tái nạm", "Bún giò heo", "Bún mọc sườn", "Bún thịt nướng", "Mỳ Quảng tôm thịt", "Phở bò chín")
            else -> listOf("Cơm gà luộc", "Cơm gà xối mỡ", "Cơm gà nướng mật ong", "Gỏi gà xé phay", "Canh lá giang", "Nước sâm dứa")
        }
        dishesList.forEachIndexed { index, dish ->
            mockProducts.add(
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
                    shopAvatarUrl = avatarUrl
                )
            )
        }
        _products.value = mockProducts

        // Mock categories
        _categories.value = when (name) {
            "Tiệm Trà Sữa Mây" -> listOf("Trà Sữa Truyền Thống", "Trà Trái Cây", "Đồ Uống Đá Xay", "Toppings Thêm")
            "Cơm Tấm Bụi" -> listOf("Cơm Tấm Đặc Biệt", "Món Ăn Kèm", "Canh Nóng", "Đồ Uống Giải Khát")
            "Quán Bà Chiểu" -> listOf("Bún & Phở Nước", "Món Trộn & Khô", "Mỳ Quảng", "Trà Đá & Khăn Lạnh")
            else -> listOf("Cơm Gà Các Loại", "Món Gỏi & Salad", "Canh & Món Thêm", "Giải Khát")
        }
    }

    fun toggleFollow() {
        _isFollowing.value = _isFollowing.value?.not()
    }
}

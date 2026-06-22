package com.urmyfood.user.presentation.main.shop

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.urmyfood.shared.domain.model.Result
import com.urmyfood.user.data.model.VoucherResponse
import com.urmyfood.user.domain.model.FoodPost
import com.urmyfood.user.domain.model.Result as UserResult
import com.urmyfood.user.domain.usecase.FollowShopUseCase
import com.urmyfood.user.domain.usecase.GetChatSessionUseCase
import com.urmyfood.user.domain.usecase.GetShopPostsUseCase
import com.urmyfood.user.domain.usecase.GetShopProfileUseCase
import com.urmyfood.user.domain.usecase.GetVouchersUseCase
import com.urmyfood.user.domain.usecase.SavePostUseCase
import com.urmyfood.user.domain.usecase.SaveVoucherUseCase
import com.urmyfood.user.domain.usecase.ToggleLikeUseCase
import com.urmyfood.user.domain.usecase.UnfollowShopUseCase
import com.urmyfood.user.domain.usecase.UnsavePostUseCase
import kotlinx.coroutines.launch

data class ShopVoucher(
    val voucherId: Long,
    val title: String,
    val description: String,
    val expiryDate: String,
    val isSaved: Boolean
)

class ShopProfileViewModel(
    private val getChatSessionUseCase: GetChatSessionUseCase,
    private val getShopProfileUseCase: GetShopProfileUseCase,
    private val getShopPostsUseCase: GetShopPostsUseCase,
    private val getVouchersUseCase: GetVouchersUseCase,
    private val followShopUseCase: FollowShopUseCase,
    private val unfollowShopUseCase: UnfollowShopUseCase,
    private val toggleLikeUseCase: ToggleLikeUseCase,
    private val savePostUseCase: SavePostUseCase,
    private val unsavePostUseCase: UnsavePostUseCase,
    private val saveVoucherUseCase: SaveVoucherUseCase
) : ViewModel() {

    sealed class ChatUiState {
        object Idle : ChatUiState()
        object Loading : ChatUiState()
        data class Success(val sessionId: Long) : ChatUiState()
        data class Error(val message: String) : ChatUiState()
    }

    private val _chatState = MutableLiveData<ChatUiState>(ChatUiState.Idle)
    val chatState: LiveData<ChatUiState> = _chatState

    fun startChat(shopId: Long) {
        if (_chatState.value is ChatUiState.Loading) return
        _chatState.value = ChatUiState.Loading
        viewModelScope.launch {
            when (val result = getChatSessionUseCase(shopId)) {
                is Result.Success -> _chatState.value = ChatUiState.Success(result.data.id)
                is Result.Error -> _chatState.value = ChatUiState.Error(result.message)
            }
        }
    }

    fun resetChatState() {
        _chatState.value = ChatUiState.Idle
    }

    class Factory(
        private val getChatSessionUseCase: GetChatSessionUseCase,
        private val getShopProfileUseCase: GetShopProfileUseCase,
        private val getShopPostsUseCase: GetShopPostsUseCase,
        private val getVouchersUseCase: GetVouchersUseCase,
        private val followShopUseCase: FollowShopUseCase,
        private val unfollowShopUseCase: UnfollowShopUseCase,
        private val toggleLikeUseCase: ToggleLikeUseCase,
        private val savePostUseCase: SavePostUseCase,
        private val unsavePostUseCase: UnsavePostUseCase,
        private val saveVoucherUseCase: SaveVoucherUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            ShopProfileViewModel(
                getChatSessionUseCase,
                getShopProfileUseCase,
                getShopPostsUseCase,
                getVouchersUseCase,
                followShopUseCase,
                unfollowShopUseCase,
                toggleLikeUseCase,
                savePostUseCase,
                unsavePostUseCase,
                saveVoucherUseCase
            ) as T
    }

    private val _shopName = MutableLiveData<String>()
    val shopName: LiveData<String> = _shopName

    private val _shopAvatarUrl = MutableLiveData<String?>()
    val shopAvatarUrl: LiveData<String?> = _shopAvatarUrl

    private val _followers = MutableLiveData<String>()
    val followers: LiveData<String> = _followers

    private val _isFollowing = MutableLiveData<Boolean>()
    val isFollowing: LiveData<Boolean> = _isFollowing

    private val _followError = MutableLiveData<String?>()
    val followError: LiveData<String?> = _followError

    private val _actionError = MutableLiveData<String?>()
    val actionError: LiveData<String?> = _actionError

    private val _productsCount = MutableLiveData<Int>()
    val productsCount: LiveData<Int> = _productsCount

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

    private val _shopCategory = MutableLiveData<String>()
    val shopCategory: LiveData<String> = _shopCategory

    private val _isOpen = MutableLiveData<Boolean>()
    val isOpen: LiveData<Boolean> = _isOpen

    // Filter-related states (clean architecture for BE integration)
    private val _selectedCategory = MutableLiveData<String?>()
    val selectedCategory: LiveData<String?> = _selectedCategory

    private val allProducts = mutableListOf<FoodPost>()

    fun initShop(shopId: Long, fallbackName: String, fallbackAvatarUrl: String?) {
        _shopName.value = fallbackName
        _shopAvatarUrl.value = fallbackAvatarUrl
        loadProfile(shopId)
        loadShopContent(shopId)
    }

    private fun loadShopContent(shopId: Long) {
        viewModelScope.launch {
            when (val result = getShopPostsUseCase(shopId)) {
                is UserResult.Success -> {
                    val posts = result.data.items
                    _posts.value = posts
                    allProducts.clear()
                    allProducts.addAll(posts)
                    _products.value = posts
                    _categories.value = listOf("Tất cả") + posts.mapNotNull { it.category }.distinct()
                    _productsCount.value = posts.size
                }
                is UserResult.Error -> Unit
            }
        }
        viewModelScope.launch {
            when (val result = getVouchersUseCase()) {
                is UserResult.Success -> _vouchers.value = result.data.map { it.toShopVoucher() }
                is UserResult.Error -> Unit
            }
        }
    }

    private fun VoucherResponse.toShopVoucher() = ShopVoucher(
        voucherId = id,
        title = title,
        description = description.orEmpty(),
        expiryDate = "HSD: $expiryDate",
        isSaved = isSaved
    )

    private fun loadProfile(shopId: Long) {
        if (shopId <= 0L) return
        viewModelScope.launch {
            when (val result = getShopProfileUseCase(shopId)) {
                is UserResult.Success -> {
                    val profile = result.data
                    _shopName.value = profile.shopName
                    _shopAvatarUrl.value = profile.logoUrl
                    _address.value = profile.address.orEmpty()
                    _operatingHours.value = profile.openingHours.orEmpty()
                    _shopCategory.value = profile.category.orEmpty()
                    _isOpen.value = com.urmyfood.shared.util.TimeUtils.isShopCurrentlyOpen(profile.isOpen, profile.openingHours)
                    _followers.value = formatFollowerCount(profile.followerCount)
                    _isFollowing.value = profile.isFollowing
                    applyFollowStateToPosts(profile.shopId, profile.isFollowing)
                }
                is UserResult.Error -> {
                    _followError.value = result.message
                }
            }
        }
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

    fun toggleFollow(shopId: Long) {
        if (shopId <= 0L) return
        val previous = _isFollowing.value ?: false
        applyFollowState(shopId, !previous, null)
        com.urmyfood.user.di.ServiceLocator.shopFollowEvent.postValue(Pair(shopId, !previous))

        viewModelScope.launch {
            val result = if (previous) {
                unfollowShopUseCase(shopId)
            } else {
                followShopUseCase(shopId)
            }
            when (result) {
                is UserResult.Success -> {
                    applyFollowState(result.data.shopId, result.data.isFollowing, result.data.followerCount)
                    com.urmyfood.user.di.ServiceLocator.shopFollowEvent.postValue(
                        Pair(result.data.shopId, result.data.isFollowing)
                    )
                }
                is UserResult.Error -> {
                    applyFollowState(shopId, previous, null)
                    com.urmyfood.user.di.ServiceLocator.shopFollowEvent.postValue(Pair(shopId, previous))
                    _followError.value = result.message
                }
            }
        }
    }

    fun applyFollowState(shopId: Long, isFollowing: Boolean, followerCount: Long?) {
        val previous = _isFollowing.value ?: false
        _isFollowing.value = isFollowing
        if (followerCount != null) {
            _followers.value = formatFollowerCount(followerCount)
        } else if (previous != isFollowing) {
            val currentCount = parseFollowerCount(_followers.value)
            val nextCount = if (isFollowing) currentCount + 1 else (currentCount - 1).coerceAtLeast(0)
            _followers.value = formatFollowerCount(nextCount)
        }
        applyFollowStateToPosts(shopId, isFollowing)
    }

    fun clearFollowError() {
        _followError.value = null
    }

    fun clearActionError() {
        _actionError.value = null
    }

    private fun applyFollowStateToPosts(shopId: Long, isFollowing: Boolean) {
        _posts.value = _posts.value?.map { post ->
            if (post.shopAccountId == shopId) post.copy(isFollowingShop = isFollowing) else post
        }
        for (index in allProducts.indices) {
            val post = allProducts[index]
            if (post.shopAccountId == shopId && post.isFollowingShop != isFollowing) {
                allProducts[index] = post.copy(isFollowingShop = isFollowing)
            }
        }
        filterProductsByCategory(_selectedCategory.value)
    }

    private fun formatFollowerCount(count: Long): String {
        return if (count >= 1000) {
            String.format("%.1fk Người theo dõi", count / 1000f)
        } else {
            "$count Người theo dõi"
        }
    }

    private fun parseFollowerCount(text: String?): Long {
        if (text.isNullOrBlank()) return 0L
        val raw = text.substringBefore(" ").replace(",", ".").trim()
        return if (raw.endsWith("k", ignoreCase = true)) {
            ((raw.dropLast(1).toFloatOrNull() ?: 0f) * 1000).toLong()
        } else {
            raw.toLongOrNull() ?: 0L
        }
    }

    fun toggleLike(post: FoodPost) {
        val currentCount = post.likeCount
        val optimisticCount = if (post.isLiked) (currentCount - 1).coerceAtLeast(0) else currentCount + 1
        applyLikeState(post.postId, !post.isLiked, optimisticCount)
        com.urmyfood.user.di.ServiceLocator.postLikeEvent.postValue(Pair(post.postId, Pair(!post.isLiked, optimisticCount)))

        viewModelScope.launch {
            when (val result = toggleLikeUseCase(post.postId, post.isLiked)) {
                is UserResult.Success -> {
                    applyLikeState(post.postId, result.data.isLiked, result.data.likeCount)
                    com.urmyfood.user.di.ServiceLocator.postLikeEvent.postValue(Pair(post.postId, Pair(result.data.isLiked, result.data.likeCount)))
                }
                is UserResult.Error -> {
                    applyLikeState(post.postId, post.isLiked, currentCount)
                    com.urmyfood.user.di.ServiceLocator.postLikeEvent.postValue(Pair(post.postId, Pair(post.isLiked, currentCount)))
                    _actionError.value = result.message
                }
            }
        }
    }

    fun toggleSave(post: FoodPost) {
        applySavedState(post.postId, !post.isSaved)
        com.urmyfood.user.di.ServiceLocator.postSavedEvent.postValue(Pair(post.postId, !post.isSaved))

        viewModelScope.launch {
            val result = if (post.isSaved) unsavePostUseCase(post.postId) else savePostUseCase(post.postId)
            when (result) {
                is UserResult.Success -> {
                    applySavedState(result.data.postId, result.data.isSaved)
                    com.urmyfood.user.di.ServiceLocator.postSavedEvent.postValue(Pair(result.data.postId, result.data.isSaved))
                }
                is UserResult.Error -> {
                    applySavedState(post.postId, post.isSaved)
                    com.urmyfood.user.di.ServiceLocator.postSavedEvent.postValue(Pair(post.postId, post.isSaved))
                    _actionError.value = result.message
                }
            }
        }
    }

    fun saveVoucher(voucherId: Long) {
        applyVoucherSaved(voucherId, true)
        com.urmyfood.user.di.ServiceLocator.voucherSavedEvent.postValue(Pair(voucherId, true))

        viewModelScope.launch {
            when (val result = saveVoucherUseCase(voucherId)) {
                is UserResult.Success -> {
                    applyVoucherSaved(result.data.voucherId, result.data.isSaved)
                    com.urmyfood.user.di.ServiceLocator.voucherSavedEvent.postValue(Pair(result.data.voucherId, result.data.isSaved))
                }
                is UserResult.Error -> {
                    applyVoucherSaved(voucherId, false)
                    com.urmyfood.user.di.ServiceLocator.voucherSavedEvent.postValue(Pair(voucherId, false))
                    _actionError.value = result.message
                }
            }
        }
    }

    fun applyLikeState(postId: String, isLiked: Boolean, likeCount: Int) {
        updatePosts { post ->
            if (post.postId == postId) post.copy(isLiked = isLiked, likeCount = likeCount) else post
        }
    }

    fun applySavedState(postId: String, isSaved: Boolean) {
        updatePosts { post ->
            if (post.postId == postId) post.copy(isSaved = isSaved) else post
        }
    }

    fun applyVoucherSaved(voucherId: Long, isSaved: Boolean) {
        _vouchers.value = _vouchers.value?.map { voucher ->
            if (voucher.voucherId == voucherId) voucher.copy(isSaved = isSaved) else voucher
        }
    }

    private fun updatePosts(transform: (FoodPost) -> FoodPost) {
        _posts.value = _posts.value?.map(transform)
        for (index in allProducts.indices) {
            allProducts[index] = transform(allProducts[index])
        }
        filterProductsByCategory(_selectedCategory.value)
    }
}

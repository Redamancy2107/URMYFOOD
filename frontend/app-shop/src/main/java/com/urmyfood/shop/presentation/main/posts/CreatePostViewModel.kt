package com.urmyfood.shop.presentation.main.posts

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class CreatePostViewModel : ViewModel() {

    private val _dishName = MutableLiveData("")
    val dishName: LiveData<String> = _dishName

    private val _price = MutableLiveData("")
    val price: LiveData<String> = _price

    private val _originalPrice = MutableLiveData("")
    val originalPrice: LiveData<String> = _originalPrice

    private val _category = MutableLiveData("Món chính")
    val category: LiveData<String> = _category

    private val _description = MutableLiveData("")
    val description: LiveData<String> = _description

    private val _isAvailable = MutableLiveData(true)
    val isAvailable: LiveData<Boolean> = _isAvailable

    private val _isFlashSale = MutableLiveData(false)
    val isFlashSale: LiveData<Boolean> = _isFlashSale

    private val _stockCount = MutableLiveData(10)
    val stockCount: LiveData<Int> = _stockCount

    var isEditMode = false
        private set
    var editingPostId: String? = null
        private set

    val categories = listOf("Món chính", "Đồ uống", "Tráng miệng", "Ăn vặt", "Combo")

    fun setDishName(name: String) {
        _dishName.value = name
    }

    fun setPrice(price: String) {
        _price.value = price
    }

    fun setOriginalPrice(price: String) {
        _originalPrice.value = price
    }

    fun setCategory(category: String) {
        _category.value = category
    }

    fun setDescription(desc: String) {
        _description.value = desc
    }

    fun setAvailable(available: Boolean) {
        _isAvailable.value = available
    }

    fun setFlashSale(flashSale: Boolean) {
        _isFlashSale.value = flashSale
    }

    fun incrementStock() {
        val current = _stockCount.value ?: 0
        _stockCount.value = current + 1
    }

    fun decrementStock() {
        val current = _stockCount.value ?: 0
        if (current > 0) {
            _stockCount.value = current - 1
        }
    }

    fun setupForEdit(post: ShopPost) {
        isEditMode = true
        editingPostId = post.postId
        _dishName.value = post.dishName
        _price.value = post.price.toString()
        _originalPrice.value = post.originalPrice?.toString().orEmpty()
        _category.value = post.category ?: "Món chính"
        _description.value = post.content.orEmpty()
        _isAvailable.value = post.isActive
        _isFlashSale.value = post.isFlashSale
        _stockCount.value = post.stock
    }

    fun validate(): String? {
        if (_dishName.value.isNullOrBlank()) {
            return "Tên món ăn không được để trống"
        }
        val priceStr = _price.value
        if (priceStr.isNullOrBlank()) {
            return "Giá không được để trống"
        }
        val priceVal = priceStr.toLongOrNull()
        if (priceVal == null || priceVal <= 0) {
            return "Giá món ăn phải lớn hơn 0"
        }
        val origPriceStr = _originalPrice.value
        if (!origPriceStr.isNullOrBlank()) {
            val origPriceVal = origPriceStr.toLongOrNull()
            if (origPriceVal == null || origPriceVal < priceVal) {
                return "Giá gốc phải lớn hơn hoặc bằng giá bán"
            }
        }
        return null
    }
}

package com.urmyfood.shop.presentation.main.posts

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.urmyfood.shared.domain.model.Result
import com.urmyfood.shop.data.model.CreatePostRequest
import com.urmyfood.shop.data.model.UpdatePostRequest
import com.urmyfood.shop.domain.model.Post
import com.urmyfood.shop.domain.usecase.CreatePostUseCase
import com.urmyfood.shop.domain.usecase.GetPostByIdUseCase
import com.urmyfood.shop.domain.usecase.UpdatePostUseCase
import com.urmyfood.shop.domain.usecase.UploadPostImageUseCase
import kotlinx.coroutines.launch
import okhttp3.MultipartBody

sealed class ImageUploadState {
    object Idle : ImageUploadState()
    object Uploading : ImageUploadState()
    data class Success(val url: String) : ImageUploadState()
    data class Error(val message: String) : ImageUploadState()
}

class CreatePostViewModel(
    private val initialPostId: String?,
    private val getPostByIdUseCase: GetPostByIdUseCase,
    private val createPostUseCase: CreatePostUseCase,
    private val updatePostUseCase: UpdatePostUseCase,
    private val uploadPostImageUseCase: UploadPostImageUseCase
) : ViewModel() {

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

    private val _isFlashSale = MutableLiveData(false)
    val isFlashSale: LiveData<Boolean> = _isFlashSale

    private val _stockCount = MutableLiveData(10)
    val stockCount: LiveData<Int> = _stockCount

    private val _imageUrl = MutableLiveData<String?>(null)
    val imageUrl: LiveData<String?> = _imageUrl

    private val _imageUploadState = MutableLiveData<ImageUploadState>(ImageUploadState.Idle)
    val imageUploadState: LiveData<ImageUploadState> = _imageUploadState

    private val _saveResult = MutableLiveData<Result<Post>?>()
    val saveResult: LiveData<Result<Post>?> = _saveResult

    val isEditMode: Boolean get() = initialPostId != null
    val editingPostId: String? get() = initialPostId

    val categories = listOf("Món chính", "Đồ uống", "Tráng miệng", "Ăn vặt", "Combo", "Bún/Phở", "Cơm", "Trà sữa", "Bánh mì")

    init {
        if (initialPostId != null) {
            loadPostForEdit(initialPostId)
        }
    }

    private fun loadPostForEdit(postId: String) {
        viewModelScope.launch {
            when (val result = getPostByIdUseCase(postId)) {
                is Result.Success -> populateFields(result.data)
                is Result.Error -> _saveResult.value = Result.Error(result.message)
            }
        }
    }

    private fun populateFields(post: Post) {
        _dishName.value = post.dishName
        _price.value = post.price.toString()
        _originalPrice.value = post.originalPrice?.toString().orEmpty()
        _category.value = post.category ?: "Món chính"
        _description.value = post.content.orEmpty()
        _isFlashSale.value = post.isFlashSale
        _stockCount.value = post.maxQuantity
        _imageUrl.value = post.imageUrl
    }

    fun setDishName(name: String) { _dishName.value = name }
    fun setPrice(price: String) { _price.value = price }
    fun setOriginalPrice(price: String) { _originalPrice.value = price }
    fun setCategory(category: String) { _category.value = category }
    fun setDescription(desc: String) { _description.value = desc }
    fun setFlashSale(flashSale: Boolean) { _isFlashSale.value = flashSale }

    fun incrementStock() { _stockCount.value = (_stockCount.value ?: 0) + 1 }
    fun decrementStock() { _stockCount.value = maxOf(1, (_stockCount.value ?: 1) - 1) }
    fun setStock(stock: Int) { if (_stockCount.value != stock) _stockCount.value = stock }

    fun uploadImage(file: MultipartBody.Part) {
        _imageUploadState.value = ImageUploadState.Uploading
        viewModelScope.launch {
            when (val result = uploadPostImageUseCase(file)) {
                is Result.Success -> {
                    _imageUrl.value = result.data
                    _imageUploadState.value = ImageUploadState.Success(result.data)
                }
                is Result.Error -> _imageUploadState.value = ImageUploadState.Error(result.message)
            }
        }
    }

    fun savePost() {
        val error = validate()
        if (error != null) {
            _saveResult.value = Result.Error(error)
            return
        }

        val dishNameVal = _dishName.value!!.trim()
        val priceVal = _price.value!!.trim().toLong()
        val origPriceVal = _originalPrice.value?.trim()?.toLongOrNull()
        val stockVal = _stockCount.value ?: 1
        val flashSaleVal = _isFlashSale.value ?: false
        val contentVal = _description.value?.trim()?.ifBlank { null }
        val imageUrlVal = _imageUrl.value
        val categoryVal = _category.value?.trim()?.ifBlank { null }

        viewModelScope.launch {
            val result = if (isEditMode) {
                val request = UpdatePostRequest(
                    dishName = dishNameVal,
                    price = priceVal,
                    originalPrice = origPriceVal,
                    maxQuantity = stockVal,
                    endTime = null,
                    isFlashSale = flashSaleVal,
                    content = contentVal,
                    imageUrl = imageUrlVal,
                    category = categoryVal
                )
                updatePostUseCase(initialPostId!!, request)
            } else {
                val request = CreatePostRequest(
                    dishName = dishNameVal,
                    price = priceVal,
                    originalPrice = origPriceVal,
                    maxQuantity = stockVal,
                    endTime = null,
                    isFlashSale = flashSaleVal,
                    content = contentVal,
                    imageUrl = imageUrlVal,
                    category = categoryVal
                )
                createPostUseCase(request)
            }
            _saveResult.value = result
        }
    }

    fun clearSaveResult() {
        _saveResult.value = null
    }

    fun validate(): String? {
        if (_dishName.value.isNullOrBlank()) return "Tên món ăn không được để trống"
        val priceStr = _price.value
        if (priceStr.isNullOrBlank()) return "Giá không được để trống"
        val priceVal = priceStr.toLongOrNull()
        if (priceVal == null || priceVal <= 0) return "Giá món ăn phải lớn hơn 0"
        val origPriceStr = _originalPrice.value
        if (!origPriceStr.isNullOrBlank()) {
            val origPriceVal = origPriceStr.toLongOrNull()
            if (origPriceVal == null || origPriceVal < priceVal) return "Giá gốc phải lớn hơn hoặc bằng giá bán"
        }
        val stockVal = _stockCount.value ?: 0
        if (stockVal < 1) return "Số lượng phải lớn hơn 0"
        return null
    }

    class Factory(
        private val initialPostId: String?,
        private val getPostByIdUseCase: GetPostByIdUseCase,
        private val createPostUseCase: CreatePostUseCase,
        private val updatePostUseCase: UpdatePostUseCase,
        private val uploadPostImageUseCase: UploadPostImageUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CreatePostViewModel::class.java)) {
                return CreatePostViewModel(
                    initialPostId, getPostByIdUseCase, createPostUseCase,
                    updatePostUseCase, uploadPostImageUseCase
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

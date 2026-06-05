package com.urmyfood.shop.presentation.auth.registration

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.urmyfood.shared.domain.model.Result
import com.urmyfood.shop.domain.model.ShopCategory
import com.urmyfood.shop.domain.model.ShopRegistrationData
import com.urmyfood.shop.domain.usecase.SubmitShopVerificationUseCase
import kotlinx.coroutines.launch

class ShopRegistrationFlowViewModel(
    private val submitShopVerificationUseCase: SubmitShopVerificationUseCase
) : ViewModel() {

    companion object {
        const val MAX_PHOTOS = 5
    }

    // --- Step 1 UiState ---
    sealed class Step1UiState {
        object Idle : Step1UiState()
        data class ValidationError(val fields: Map<Field, String>) : Step1UiState()
        object Proceed : Step1UiState()
    }

    // --- Step 2 UiState ---
    sealed class Step2UiState {
        object Idle : Step2UiState()
        data class ValidationError(val fields: Map<Field, String>) : Step2UiState()
        object Proceed : Step2UiState()
    }

    // --- Step 3 UiState ---
    sealed class Step3UiState {
        object Idle : Step3UiState()
        data class ValidationError(val fields: Map<Field, String>) : Step3UiState()
        object Loading : Step3UiState()
        object Success : Step3UiState()
        data class Error(val message: String) : Step3UiState()
    }

    enum class Field { SHOP_NAME, CATEGORY, ADDRESS, CCCD_FRONT, CCCD_BACK, SHOP_PHOTOS }

    // --- Fields ---
    private val _shopName = MutableLiveData("")
    val shopName: LiveData<String> = _shopName

    private val _selectedCategory = MutableLiveData<ShopCategory?>(null)
    val selectedCategory: LiveData<ShopCategory?> = _selectedCategory

    private val _address = MutableLiveData("")
    val address: LiveData<String> = _address

    private val _latitude = MutableLiveData<Double?>(null)
    val latitude: LiveData<Double?> = _latitude

    private val _longitude = MutableLiveData<Double?>(null)
    val longitude: LiveData<Double?> = _longitude

    private val _cccdFrontUri = MutableLiveData<String?>(null)
    val cccdFrontUri: LiveData<String?> = _cccdFrontUri

    private val _cccdBackUri = MutableLiveData<String?>(null)
    val cccdBackUri: LiveData<String?> = _cccdBackUri

    private val _shopPhotoUris = MutableLiveData<List<String>>(emptyList())
    val shopPhotoUris: LiveData<List<String>> = _shopPhotoUris

    // --- UiState per step ---
    private val _step1UiState = MutableLiveData<Step1UiState>(Step1UiState.Idle)
    val step1UiState: LiveData<Step1UiState> = _step1UiState

    private val _step2UiState = MutableLiveData<Step2UiState>(Step2UiState.Idle)
    val step2UiState: LiveData<Step2UiState> = _step2UiState

    private val _step3UiState = MutableLiveData<Step3UiState>(Step3UiState.Idle)
    val step3UiState: LiveData<Step3UiState> = _step3UiState

    // --- Setters ---
    fun setShopName(value: String) { _shopName.value = value }
    fun selectCategory(category: ShopCategory) { _selectedCategory.value = category }
    fun updateAddressText(addr: String) { _address.value = addr }

    fun setAddress(addr: String, lat: Double, lng: Double) {
        _address.value = addr
        _latitude.value = lat
        _longitude.value = lng
    }

    fun setCccdFront(uri: String) { _cccdFrontUri.value = uri }
    fun setCccdBack(uri: String) { _cccdBackUri.value = uri }

    fun addShopPhotos(uris: List<String>) {
        val current = _shopPhotoUris.value ?: emptyList()
        val available = MAX_PHOTOS - current.size
        if (available <= 0) return
        _shopPhotoUris.value = current + uris.take(available)
    }

    fun removeShopPhoto(index: Int) {
        val current = _shopPhotoUris.value ?: return
        if (index < 0 || index >= current.size) return
        _shopPhotoUris.value = current.toMutableList().also { it.removeAt(index) }
    }

    // --- Validation ---
    fun validateStep1() {
        val errors = mutableMapOf<Field, String>()
        if (_shopName.value.isNullOrBlank()) errors[Field.SHOP_NAME] = "Vui lòng nhập tên quán"
        if (_selectedCategory.value == null) errors[Field.CATEGORY] = "Vui lòng chọn danh mục"
        if (_address.value.isNullOrBlank()) errors[Field.ADDRESS] = "Vui lòng nhập địa chỉ"
        _step1UiState.value = if (errors.isEmpty()) Step1UiState.Proceed
        else Step1UiState.ValidationError(errors)
    }

    fun validateStep2() {
        val errors = mutableMapOf<Field, String>()
        if (_cccdFrontUri.value == null) errors[Field.CCCD_FRONT] = "Vui lòng thêm ảnh mặt trước CCCD"
        if (_cccdBackUri.value == null) errors[Field.CCCD_BACK] = "Vui lòng thêm ảnh mặt sau CCCD"
        _step2UiState.value = if (errors.isEmpty()) Step2UiState.Proceed
        else Step2UiState.ValidationError(errors)
    }

    fun validateStep3() {
        val errors = mutableMapOf<Field, String>()
        if (_shopPhotoUris.value.isNullOrEmpty()) errors[Field.SHOP_PHOTOS] = "Vui lòng thêm ít nhất 1 ảnh quán"
        if (errors.isNotEmpty()) {
            _step3UiState.value = Step3UiState.ValidationError(errors)
        } else {
            submit()
        }
    }

    fun resetStep1State() { _step1UiState.value = Step1UiState.Idle }
    fun resetStep2State() { _step2UiState.value = Step2UiState.Idle }
    fun resetStep3State() { _step3UiState.value = Step3UiState.Idle }

    private fun submit() {
        _step3UiState.value = Step3UiState.Loading
        viewModelScope.launch {
            val data = ShopRegistrationData(
                shopName = _shopName.value.orEmpty().trim(),
                category = _selectedCategory.value ?: ShopCategory.KHAC,
                address = _address.value.orEmpty().trim(),
                latitude = _latitude.value,
                longitude = _longitude.value,
                cccdFrontUri = _cccdFrontUri.value,
                cccdBackUri = _cccdBackUri.value,
                shopPhotoUris = _shopPhotoUris.value.orEmpty()
            )
            _step3UiState.value = when (val result = submitShopVerificationUseCase(data)) {
                is Result.Success -> Step3UiState.Success
                is Result.Error -> Step3UiState.Error(result.message)
            }
        }
    }

    class Factory(
        private val submitShopVerificationUseCase: SubmitShopVerificationUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            ShopRegistrationFlowViewModel(submitShopVerificationUseCase) as T
    }
}

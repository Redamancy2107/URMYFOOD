package com.urmyfood.shop.presentation.main.account

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.urmyfood.shared.domain.model.Result
import com.urmyfood.shop.data.util.toUserMessage
import com.urmyfood.shop.domain.model.ShopProfile
import com.urmyfood.shop.domain.model.ShopProfileImageType
import com.urmyfood.shop.domain.usecase.GetShopProfileUseCase
import com.urmyfood.shop.domain.usecase.UpdateShopProfileUseCase
import com.urmyfood.shop.domain.usecase.UploadShopProfileImageUseCase
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import okhttp3.MultipartBody

sealed class ProfileUiState {
    object Idle : ProfileUiState()
    object Loading : ProfileUiState()
    data class Success(val profile: ShopProfile) : ProfileUiState()
    data class Error(val message: String) : ProfileUiState()
}

sealed class ProfileEditUiState {
    object Idle : ProfileEditUiState()
    data class Loading(val message: String) : ProfileEditUiState()
    data class Success(val profile: ShopProfile) : ProfileEditUiState()
    data class Error(val message: String) : ProfileEditUiState()
}

class ShopProfileEditViewModel(
    private val getShopProfileUseCase: GetShopProfileUseCase,
    private val updateShopProfileUseCase: UpdateShopProfileUseCase,
    private val uploadShopProfileImageUseCase: UploadShopProfileImageUseCase
) : ViewModel() {

    private companion object {
        const val SAVE_TIMEOUT_MS = 45_000L
    }

    private val _loadState = MutableLiveData<ProfileUiState>(ProfileUiState.Idle)
    val loadState: LiveData<ProfileUiState> = _loadState

    private val _updateState = MutableLiveData<ProfileEditUiState>(ProfileEditUiState.Idle)
    val updateState: LiveData<ProfileEditUiState> = _updateState

    private var currentProfile: ShopProfile? = null

    fun loadProfileIfNeeded() {
        val profile = currentProfile
        if (profile != null) {
            _loadState.value = ProfileUiState.Success(profile)
            return
        }
        loadProfile()
    }

    fun loadProfile() {
        _loadState.value = ProfileUiState.Loading
        viewModelScope.launch {
            when (val result = getShopProfileUseCase()) {
                is Result.Success -> {
                    currentProfile = result.data
                    com.urmyfood.shop.di.ServiceLocator.cachedShopProfile = result.data
                    _loadState.value = ProfileUiState.Success(result.data)
                }
                is Result.Error -> _loadState.value = ProfileUiState.Error(result.message)
            }
        }
    }

    fun updateProfile(
        shopName: String,
        logoUrl: String?,
        coverUrl: String?,
        category: String,
        address: String,
        latitude: Double?,
        longitude: Double?,
        description: String?,
        openingHours: String,
        isOpen: Boolean,
        logoImagePart: MultipartBody.Part? = null,
        coverImagePart: MultipartBody.Part? = null
    ) {
        val trimmedShopName = shopName.trim()
        val trimmedCategory = category.trim()
        val trimmedAddress = address.trim()
        if (trimmedShopName.isEmpty()) {
            _updateState.value = ProfileEditUiState.Error("Vui lòng nhập tên quán")
            return
        }
        if (trimmedCategory.isEmpty()) {
            _updateState.value = ProfileEditUiState.Error("Vui lòng nhập danh mục quán")
            return
        }
        if (trimmedAddress.isEmpty()) {
            _updateState.value = ProfileEditUiState.Error("Vui lòng nhập địa chỉ quán")
            return
        }

        _updateState.value = ProfileEditUiState.Loading("Đang tải ảnh...")
        viewModelScope.launch {
            try {
                withTimeout(SAVE_TIMEOUT_MS) {
                    val uploadedLogoUrl = uploadIfNeeded(ShopProfileImageType.LOGO, logoImagePart, logoUrl)
                        ?: return@withTimeout
                    val uploadedCoverUrl = uploadIfNeeded(ShopProfileImageType.COVER, coverImagePart, coverUrl)
                        ?: return@withTimeout

                    _updateState.value = ProfileEditUiState.Loading("Đang lưu hồ sơ...")
                    val baseProfile = currentProfile
                    val profile = ShopProfile(
                        id = baseProfile?.id,
                        shopId = baseProfile?.shopId,
                        shopName = trimmedShopName,
                        logoUrl = uploadedLogoUrl.value?.trimToNull(),
                        coverUrl = uploadedCoverUrl.value?.trimToNull(),
                        category = trimmedCategory,
                        address = trimmedAddress,
                        latitude = latitude,
                        longitude = longitude,
                        description = description?.trimToNull(),
                        openingHours = openingHours.trimToNull() ?: "08:00 - 22:00",
                        isOpen = isOpen,
                        verificationStatus = baseProfile?.verificationStatus ?: "NOT_SUBMITTED"
                    )

                    when (val result = updateShopProfileUseCase(profile)) {
                        is Result.Success -> {
                            currentProfile = result.data
                            com.urmyfood.shop.di.ServiceLocator.cachedShopProfile = result.data
                            _updateState.value = ProfileEditUiState.Success(result.data)
                        }
                        is Result.Error -> _updateState.value = ProfileEditUiState.Error(result.message)
                    }
                }
            } catch (e: TimeoutCancellationException) {
                _updateState.value = ProfileEditUiState.Error("Lưu hồ sơ quá lâu. Vui lòng thử lại")
            } catch (e: Exception) {
                _updateState.value = ProfileEditUiState.Error(e.toUserMessage())
            }
        }
    }

    private suspend fun uploadIfNeeded(
        type: ShopProfileImageType,
        imagePart: MultipartBody.Part?,
        fallbackUrl: String?
    ): UploadValue? {
        if (imagePart == null) {
            return UploadValue(fallbackUrl)
        }
        return when (val result = uploadShopProfileImageUseCase(type, imagePart)) {
            is Result.Success -> UploadValue(result.data)
            is Result.Error -> {
                _updateState.value = ProfileEditUiState.Error(result.message)
                null
            }
        }
    }

    private data class UploadValue(val value: String?)

    private fun String.trimToNull(): String? {
        val value = trim()
        return value.ifEmpty { null }
    }

    class Factory(
        private val getShopProfileUseCase: GetShopProfileUseCase,
        private val updateShopProfileUseCase: UpdateShopProfileUseCase,
        private val uploadShopProfileImageUseCase: UploadShopProfileImageUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ShopProfileEditViewModel::class.java)) {
                return ShopProfileEditViewModel(
                    getShopProfileUseCase,
                    updateShopProfileUseCase,
                    uploadShopProfileImageUseCase
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

package com.urmyfood.user.presentation.main.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.urmyfood.user.domain.model.Result
import com.urmyfood.user.domain.model.UserProfile
import com.urmyfood.user.domain.usecase.GetUserProfileUseCase
import com.urmyfood.user.domain.usecase.UpdateUserProfileUseCase
import com.urmyfood.user.domain.usecase.UploadUserAvatarUseCase
import kotlinx.coroutines.launch
import okhttp3.MultipartBody

sealed class ProfileEditUiState {
    object Idle : ProfileEditUiState()
    object Loading : ProfileEditUiState()
    data class Success(val profile: UserProfile) : ProfileEditUiState()
    data class Error(val message: String) : ProfileEditUiState()
}

class ProfileEditViewModel(
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val updateUserProfileUseCase: UpdateUserProfileUseCase,
    private val uploadUserAvatarUseCase: UploadUserAvatarUseCase
) : ViewModel() {

    private val _loadState = MutableLiveData<ProfileUiState>(ProfileUiState.Idle)
    val loadState: LiveData<ProfileUiState> = _loadState

    private val _updateState = MutableLiveData<ProfileEditUiState>(ProfileEditUiState.Idle)
    val updateState: LiveData<ProfileEditUiState> = _updateState

    fun loadProfile() {
        _loadState.value = ProfileUiState.Loading
        viewModelScope.launch {
            when (val result = getUserProfileUseCase()) {
                is Result.Success -> _loadState.value = ProfileUiState.Success(result.data)
                is Result.Error -> _loadState.value = ProfileUiState.Error(result.message)
            }
        }
    }

    /**
     * Lưu hồ sơ: cập nhật tên/SĐT trước (không đụng avatar), sau đó nếu người dùng chọn ảnh mới
     * thì upload ảnh lên Supabase qua backend để lấy URL HTTPS thật.
     */
    fun saveProfile(fullName: String?, phone: String?, avatarPart: MultipartBody.Part?) {
        _updateState.value = ProfileEditUiState.Loading
        viewModelScope.launch {
            val profileResult = updateUserProfileUseCase(fullName, phone, null)
            if (profileResult is Result.Error) {
                _updateState.value = ProfileEditUiState.Error(profileResult.message)
                return@launch
            }
            var profile = (profileResult as Result.Success).data

            if (avatarPart != null) {
                when (val avatarResult = uploadUserAvatarUseCase(avatarPart)) {
                    is Result.Success -> profile = avatarResult.data
                    is Result.Error -> {
                        _updateState.value = ProfileEditUiState.Error(avatarResult.message)
                        return@launch
                    }
                }
            }

            _updateState.value = ProfileEditUiState.Success(profile)
        }
    }

    class Factory(
        private val getUserProfileUseCase: GetUserProfileUseCase,
        private val updateUserProfileUseCase: UpdateUserProfileUseCase,
        private val uploadUserAvatarUseCase: UploadUserAvatarUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ProfileEditViewModel::class.java)) {
                return ProfileEditViewModel(
                    getUserProfileUseCase,
                    updateUserProfileUseCase,
                    uploadUserAvatarUseCase
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

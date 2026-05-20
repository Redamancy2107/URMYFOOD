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
import kotlinx.coroutines.launch

sealed class ProfileEditUiState {
    object Idle : ProfileEditUiState()
    object Loading : ProfileEditUiState()
    data class Success(val profile: UserProfile) : ProfileEditUiState()
    data class Error(val message: String) : ProfileEditUiState()
}

class ProfileEditViewModel(
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val updateUserProfileUseCase: UpdateUserProfileUseCase
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

    fun updateProfile(fullName: String?, phone: String?, avatarUrl: String?) {
        _updateState.value = ProfileEditUiState.Loading
        viewModelScope.launch {
            when (val result = updateUserProfileUseCase(fullName, phone, avatarUrl)) {
                is Result.Success -> _updateState.value = ProfileEditUiState.Success(result.data)
                is Result.Error -> _updateState.value = ProfileEditUiState.Error(result.message)
            }
        }
    }

    class Factory(
        private val getUserProfileUseCase: GetUserProfileUseCase,
        private val updateUserProfileUseCase: UpdateUserProfileUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ProfileEditViewModel::class.java)) {
                return ProfileEditViewModel(getUserProfileUseCase, updateUserProfileUseCase) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

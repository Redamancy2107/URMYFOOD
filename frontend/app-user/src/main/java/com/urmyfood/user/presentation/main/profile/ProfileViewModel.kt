package com.urmyfood.user.presentation.main.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.urmyfood.user.data.local.TokenManager
import com.urmyfood.user.domain.model.Result
import com.urmyfood.user.domain.repository.GuestRepository
import com.urmyfood.user.domain.usecase.GetUserProfileUseCase
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val tokenManager: TokenManager,
    private val guestRepository: GuestRepository,
    private val getUserProfileUseCase: GetUserProfileUseCase
) : ViewModel() {

    private val _profileState = MutableLiveData<ProfileUiState>(ProfileUiState.Idle)
    val profileState: LiveData<ProfileUiState> = _profileState

    private val _userName = MutableLiveData<String?>()
    val userName: LiveData<String?> = _userName

    private val _isGuest = MutableLiveData<Boolean>()
    val isGuest: LiveData<Boolean> = _isGuest

    private val _isStudentVerified = MutableLiveData<Boolean>()
    val isStudentVerified: LiveData<Boolean> = _isStudentVerified

    init {
        loadUserInfo()
    }

    fun loadUserInfo() {
        val guest = guestRepository.isGuest()
        _isGuest.value = guest
        if (!guest) {
            _userName.value = tokenManager.getFullName()
            _isStudentVerified.value = false
        } else {
            _isStudentVerified.value = false
        }
    }

    fun loadProfile(force: Boolean = false) {
        if (guestRepository.isGuest()) return
        if (!force && _profileState.value is ProfileUiState.Success) {
            return
        }
        _profileState.value = ProfileUiState.Loading
        viewModelScope.launch {
            when (val result = getUserProfileUseCase()) {
                is Result.Success -> _profileState.value = ProfileUiState.Success(result.data)
                is Result.Error -> _profileState.value = ProfileUiState.Error(result.message)
            }
        }
    }

    fun logout() {
        tokenManager.clear()
        guestRepository.clearGuest()
    }

    class Factory(
        private val tokenManager: TokenManager,
        private val guestRepository: GuestRepository,
        private val getUserProfileUseCase: GetUserProfileUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
                return ProfileViewModel(tokenManager, guestRepository, getUserProfileUseCase) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

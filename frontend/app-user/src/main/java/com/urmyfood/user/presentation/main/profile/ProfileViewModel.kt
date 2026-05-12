package com.urmyfood.user.presentation.main.profile

/**
 * ViewModel for the Profile screen.
 */
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.urmyfood.user.data.local.TokenManager
import com.urmyfood.user.domain.repository.GuestRepository

/**
 * ViewModel for the Profile screen.
 */
class ProfileViewModel(
    private val tokenManager: TokenManager,
    private val guestRepository: GuestRepository
) : ViewModel() {

    private val _userName = MutableLiveData<String?>()
    val userName: LiveData<String?> = _userName

    private val _isGuest = MutableLiveData<Boolean>()
    val isGuest: LiveData<Boolean> = _isGuest

    init {
        loadUserInfo()
    }

    fun loadUserInfo() {
        val guest = guestRepository.isGuest()
        _isGuest.value = guest
        if (!guest) {
            _userName.value = tokenManager.getFullName()
        }
    }

    fun logout() {
        tokenManager.clear()
        guestRepository.clearGuest()
    }

    /**
     * Factory for creating ProfileViewModel.
     */
    class Factory(
        private val tokenManager: TokenManager,
        private val guestRepository: GuestRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
                return ProfileViewModel(tokenManager, guestRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

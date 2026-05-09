package com.urmyfood.user.presentation.main.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * ViewModel for the Profile screen.
 */
class ProfileViewModel : ViewModel() {
    
    /**
     * Factory for creating ProfileViewModel.
     */
    class Factory : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
                return ProfileViewModel() as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

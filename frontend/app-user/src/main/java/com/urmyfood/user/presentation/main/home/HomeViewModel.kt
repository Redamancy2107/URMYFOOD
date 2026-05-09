package com.urmyfood.user.presentation.main.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * ViewModel for the Home screen.
 */
class HomeViewModel : ViewModel() {
    
    /**
     * Factory for creating HomeViewModel.
     */
    class Factory : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
                return HomeViewModel() as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

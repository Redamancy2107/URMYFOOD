package com.urmyfood.user.presentation.main.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * ViewModel for the Favorites screen.
 */
class FavoritesViewModel : ViewModel() {
    
    /**
     * Factory for creating FavoritesViewModel.
     */
    class Factory : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(FavoritesViewModel::class.java)) {
                return FavoritesViewModel() as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

package com.urmyfood.user.presentation.main.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * ViewModel for the Search screen.
 */
class SearchViewModel : ViewModel() {
    
    /**
     * Factory for creating SearchViewModel.
     */
    class Factory : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SearchViewModel::class.java)) {
                return SearchViewModel() as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

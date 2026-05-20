package com.urmyfood.user.presentation.main.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class TermsPoliciesViewModel : ViewModel() {
    // Currently no state needed - terms are static content
    // Ready for future API integration if needed

    class Factory : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(TermsPoliciesViewModel::class.java)) {
                return TermsPoliciesViewModel() as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

package com.urmyfood.user.presentation.main.profile

import com.urmyfood.user.domain.model.UserProfile

sealed class ProfileUiState {
    object Idle : ProfileUiState()
    object Loading : ProfileUiState()
    data class Success(val profile: UserProfile) : ProfileUiState()
    data class Error(val message: String) : ProfileUiState()
}

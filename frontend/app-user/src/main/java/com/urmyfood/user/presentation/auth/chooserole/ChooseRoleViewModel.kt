package com.urmyfood.user.presentation.auth.chooserole

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.urmyfood.user.domain.model.AuthToken
import com.urmyfood.user.domain.usecase.LoginAsGuestUseCase

/**
 * ViewModel for the ChooseRole screen.
 */
class ChooseRoleViewModel(
    private val loginAsGuestUseCase: LoginAsGuestUseCase
) : ViewModel() {

    private val _uiState = MutableLiveData<ChooseRoleUiState>(ChooseRoleUiState.Idle)
    val uiState: LiveData<ChooseRoleUiState> = _uiState

    fun loginAsGuest() {
        loginAsGuestUseCase()
        _uiState.value = ChooseRoleUiState.GuestSuccess
    }

    class Factory(
        private val loginAsGuestUseCase: LoginAsGuestUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ChooseRoleViewModel::class.java)) {
                return ChooseRoleViewModel(loginAsGuestUseCase) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

sealed class ChooseRoleUiState {
    data object Idle : ChooseRoleUiState()
    data object Loading : ChooseRoleUiState()
    data class Success(val authToken: AuthToken) : ChooseRoleUiState()
    data object GuestSuccess : ChooseRoleUiState()
    data class Error(val message: String) : ChooseRoleUiState()
}

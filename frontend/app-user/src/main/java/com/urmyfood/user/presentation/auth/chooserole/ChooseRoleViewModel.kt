package com.urmyfood.user.presentation.auth.chooserole

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.urmyfood.user.domain.model.AuthToken

/**
 * ViewModel for the ChooseRole screen.
 */
class ChooseRoleViewModel : ViewModel() {

    private val _loginState = MutableLiveData<ChooseRoleUiState>(ChooseRoleUiState.Idle)
    val loginState: LiveData<ChooseRoleUiState> = _loginState

    // Google Login logic is currently a stub on the UI side

    class Factory : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ChooseRoleViewModel::class.java)) {
                return ChooseRoleViewModel() as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

sealed class ChooseRoleUiState {
    data object Idle : ChooseRoleUiState()
    data object Loading : ChooseRoleUiState()
    data class Success(val authToken: AuthToken) : ChooseRoleUiState()
    data class Error(val message: String) : ChooseRoleUiState()
}

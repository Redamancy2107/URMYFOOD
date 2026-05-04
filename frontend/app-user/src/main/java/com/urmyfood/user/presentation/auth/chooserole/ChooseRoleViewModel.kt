package com.urmyfood.user.presentation.auth.chooserole

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.urmyfood.user.domain.model.AuthToken
import com.urmyfood.user.domain.model.Result
import com.urmyfood.user.domain.usecase.LoginWithGoogleUseCase
import kotlinx.coroutines.launch

/**
 * ViewModel for the ChooseRole screen.
 * Manages Google login state.
 */
class ChooseRoleViewModel(
    private val loginWithGoogleUseCase: LoginWithGoogleUseCase
) : ViewModel() {

    private val _loginState = MutableLiveData<ChooseRoleUiState>(ChooseRoleUiState.Idle)
    val loginState: LiveData<ChooseRoleUiState> = _loginState

    fun loginWithGoogle(idToken: String) {
        _loginState.value = ChooseRoleUiState.Loading
        viewModelScope.launch {
            when (val result = loginWithGoogleUseCase(idToken)) {
                is Result.Success -> _loginState.value = ChooseRoleUiState.Success(result.data)
                is Result.Error -> _loginState.value = ChooseRoleUiState.Error(result.message)
            }
        }
    }

    /**
     * Factory for creating ChooseRoleViewModel.
     */
    class Factory(
        private val loginWithGoogleUseCase: LoginWithGoogleUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ChooseRoleViewModel::class.java)) {
                return ChooseRoleViewModel(loginWithGoogleUseCase) as T
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

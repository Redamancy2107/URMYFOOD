package com.urmyfood.user.presentation.auth.chooserole

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.urmyfood.user.domain.model.AuthToken
import com.urmyfood.user.domain.usecase.LoginAsGuestUseCase
import kotlinx.coroutines.launch

class ChooseRoleViewModel(
    private val loginAsGuestUseCase: LoginAsGuestUseCase,
    private val loginWithGoogleUseCase: com.urmyfood.user.domain.usecase.LoginWithGoogleUseCase,
    private val tokenManager: com.urmyfood.user.data.local.TokenManager
) : ViewModel() {

    private val _uiState = MutableLiveData<ChooseRoleUiState>(ChooseRoleUiState.Idle)
    val uiState: LiveData<ChooseRoleUiState> = _uiState

    fun loginAsGuest() {
        loginAsGuestUseCase()
        _uiState.value = ChooseRoleUiState.GuestSuccess
    }

    fun loginWithGoogle(idToken: String) {
        _uiState.value = ChooseRoleUiState.Loading
        viewModelScope.launch {
            when (val result = loginWithGoogleUseCase(idToken)) {
                is com.urmyfood.user.domain.model.Result.Success -> {
                    if (result.data.role != "CUSTOMER") {
                        _uiState.value = ChooseRoleUiState.WrongRole
                    } else {
                        saveToken(result.data)
                        _uiState.value = ChooseRoleUiState.Success(result.data)
                    }
                }
                is com.urmyfood.user.domain.model.Result.Error -> {
                    _uiState.value = ChooseRoleUiState.Error(result.message)
                }
            }
        }
    }

    fun resetState() { _uiState.value = ChooseRoleUiState.Idle }

    private fun saveToken(authToken: com.urmyfood.user.domain.model.AuthToken) {
        tokenManager.saveToken(
            token = authToken.accessToken,
            refreshToken = authToken.refreshToken,
            fullName = authToken.fullName,
            role = authToken.role
        )
    }

    class Factory(
        private val loginAsGuestUseCase: LoginAsGuestUseCase,
        private val loginWithGoogleUseCase: com.urmyfood.user.domain.usecase.LoginWithGoogleUseCase,
        private val tokenManager: com.urmyfood.user.data.local.TokenManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ChooseRoleViewModel::class.java)) {
                return ChooseRoleViewModel(loginAsGuestUseCase, loginWithGoogleUseCase, tokenManager) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

sealed class ChooseRoleUiState {
    data object Idle : ChooseRoleUiState()
    data object Loading : ChooseRoleUiState()
    data class Success(val authToken: com.urmyfood.user.domain.model.AuthToken) : ChooseRoleUiState()
    data object GuestSuccess : ChooseRoleUiState()
    data object WrongRole : ChooseRoleUiState()
    data class Error(val message: String) : ChooseRoleUiState()
}

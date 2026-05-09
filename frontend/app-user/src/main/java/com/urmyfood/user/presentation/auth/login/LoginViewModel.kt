package com.urmyfood.user.presentation.auth.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.urmyfood.user.domain.model.AuthToken
import com.urmyfood.user.domain.model.Result
import com.urmyfood.user.domain.usecase.LoginUseCase
import com.urmyfood.user.domain.usecase.LoginWithGoogleUseCase
import com.urmyfood.user.domain.usecase.LoginWithOtpUseCase
import com.urmyfood.user.domain.usecase.SendLoginOtpUseCase
import kotlinx.coroutines.launch

/**
 * ViewModel for the Login screen.
 */
class LoginViewModel(
    private val loginUseCase: LoginUseCase,
    private val loginWithGoogleUseCase: LoginWithGoogleUseCase,
    private val sendLoginOtpUseCase: SendLoginOtpUseCase,
    private val loginWithOtpUseCase: LoginWithOtpUseCase
) : ViewModel() {

    private val _loginState = MutableLiveData<LoginUiState>(LoginUiState.Idle)
    val loginState: LiveData<LoginUiState> = _loginState

    fun login(emailOrPhone: String, password: String) {
        _loginState.value = LoginUiState.Loading

        viewModelScope.launch {
            when (val result = loginUseCase(emailOrPhone, password)) {
                is Result.Success -> {
                    _loginState.value = LoginUiState.Success(result.data)
                }
                is Result.Error -> {
                    _loginState.value = LoginUiState.Error(result.message)
                }
            }
        }
    }

    fun loginWithGoogle(idToken: String) {
        _loginState.value = LoginUiState.Loading
        viewModelScope.launch {
            when (val result = loginWithGoogleUseCase(idToken)) {
                is Result.Success -> _loginState.value = LoginUiState.Success(result.data)
                is Result.Error -> _loginState.value = LoginUiState.Error(result.message)
            }
        }
    }

    class Factory(
        private val loginUseCase: LoginUseCase,
        private val loginWithGoogleUseCase: LoginWithGoogleUseCase,
        private val sendLoginOtpUseCase: SendLoginOtpUseCase,
        private val loginWithOtpUseCase: LoginWithOtpUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
                return LoginViewModel(
                    loginUseCase,
                    loginWithGoogleUseCase,
                    sendLoginOtpUseCase,
                    loginWithOtpUseCase
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

sealed class LoginUiState {
    data object Idle : LoginUiState()
    data object Loading : LoginUiState()
    data class Success(val authToken: AuthToken) : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}

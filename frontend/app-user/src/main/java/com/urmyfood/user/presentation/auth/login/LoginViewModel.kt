package com.urmyfood.user.presentation.auth.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.urmyfood.user.data.local.TokenManager
import com.urmyfood.user.domain.model.AuthToken
import com.urmyfood.user.domain.model.Result
import com.urmyfood.user.domain.usecase.LoginAsGuestUseCase
import com.urmyfood.user.domain.usecase.LoginUseCase
import com.urmyfood.user.domain.usecase.LoginWithGoogleUseCase
import com.urmyfood.user.domain.usecase.LoginWithOtpUseCase
import com.urmyfood.user.domain.usecase.SendLoginOtpUseCase
import kotlinx.coroutines.launch

class LoginViewModel(
    private val loginUseCase: LoginUseCase,
    private val loginWithGoogleUseCase: LoginWithGoogleUseCase,
    private val sendLoginOtpUseCase: SendLoginOtpUseCase,
    private val loginWithOtpUseCase: LoginWithOtpUseCase,
    private val loginAsGuestUseCase: LoginAsGuestUseCase,
    private val tokenManager: TokenManager,
    private val guestRepository: com.urmyfood.user.domain.repository.GuestRepository
) : ViewModel() {

    private val _loginState = MutableLiveData<LoginUiState>(LoginUiState.Idle)
    val loginState: LiveData<LoginUiState> = _loginState

    fun login(emailOrPhone: String, password: String) {
        _loginState.value = LoginUiState.Loading
        
        // --- BYPASS LOGIC FOR UI TESTING ---
        // Manually clear guest session and set success to bypass backend issues
        viewModelScope.launch {
            kotlinx.coroutines.delay(1000) // Small delay for feel
            guestRepository.clearGuest()
            _loginState.value = LoginUiState.Success(AuthToken("mock_access", "mock_refresh", 3600L))
        }
        
        /* Original logic muted
        viewModelScope.launch {
            when (val result = loginUseCase(emailOrPhone, password)) {
                is Result.Success -> {
                    saveToken(result.data)
                    _loginState.value = LoginUiState.Success(result.data)
                }
                is Result.Error -> _loginState.value = LoginUiState.Error(result.message)
            }
        }
        */
    }

    private fun saveToken(authToken: AuthToken) {
        tokenManager.saveToken(
            token = authToken.accessToken,
            refreshToken = authToken.refreshToken
        )
    }

    fun loginAsGuest() {
        loginAsGuestUseCase()
        _loginState.value = LoginUiState.GuestSuccess
    }

    fun loginWithGoogle(idToken: String) {
        _loginState.value = LoginUiState.Loading
        viewModelScope.launch {
            when (val result = loginWithGoogleUseCase(idToken)) {
                is Result.Success -> {
                    saveToken(result.data)
                    _loginState.value = LoginUiState.Success(result.data)
                }
                is Result.Error -> _loginState.value = LoginUiState.Error(result.message)
            }
        }
    }

    class Factory(
        private val loginUseCase: LoginUseCase,
        private val loginWithGoogleUseCase: LoginWithGoogleUseCase,
        private val sendLoginOtpUseCase: SendLoginOtpUseCase,
        private val loginWithOtpUseCase: LoginWithOtpUseCase,
        private val loginAsGuestUseCase: LoginAsGuestUseCase,
        private val tokenManager: TokenManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
                return LoginViewModel(
                    loginUseCase,
                    loginWithGoogleUseCase,
                    sendLoginOtpUseCase,
                    loginWithOtpUseCase,
                    loginAsGuestUseCase,
                    tokenManager,
                    com.urmyfood.user.di.ServiceLocator.guestSessionManager
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
    data object GuestSuccess : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}

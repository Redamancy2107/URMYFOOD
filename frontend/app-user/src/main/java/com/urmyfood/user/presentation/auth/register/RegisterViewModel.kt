package com.urmyfood.user.presentation.auth.register

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.urmyfood.user.domain.model.AuthToken
import com.urmyfood.user.domain.model.Result
import com.urmyfood.user.domain.usecase.RegisterUseCase
import com.urmyfood.user.domain.usecase.SendLoginOtpUseCase
import kotlinx.coroutines.launch

/**
 * ViewModel for the Registration screen.
 * Manages UI state and delegates business logic to RegisterUseCase.
 */
class RegisterViewModel(
    private val registerUseCase: RegisterUseCase,
    private val sendLoginOtpUseCase: SendLoginOtpUseCase
) : ViewModel() {

    private val _registerState = MutableLiveData<RegisterUiState>(RegisterUiState.Idle)
    val registerState: LiveData<RegisterUiState> = _registerState

    fun sendOtp(email: String) {
        if (email.isBlank()) {
            _registerState.value = RegisterUiState.Error("Vui lòng nhập email")
            return
        }
        _registerState.value = RegisterUiState.Loading
        viewModelScope.launch {
            when (val result = sendLoginOtpUseCase(email)) {
                is Result.Success -> _registerState.value = RegisterUiState.OtpSent
                is Result.Error -> _registerState.value = RegisterUiState.Error(result.message)
            }
        }
    }

    fun register(
        fullName: String,
        email: String,
        phone: String,
        password: String,
        confirmPassword: String,
        otpCode: String
    ) {
        _registerState.value = RegisterUiState.Loading

        viewModelScope.launch {
            when (val result = registerUseCase(fullName, email, phone, password, confirmPassword, otpCode)) {
                is Result.Success -> {
                    _registerState.value = RegisterUiState.Success(result.data)
                }
                is Result.Error -> {
                    _registerState.value = RegisterUiState.Error(result.message)
                }
            }
        }
    }

    /**
     * Factory for creating RegisterViewModel with dependencies.
     */
    class Factory(
        private val registerUseCase: RegisterUseCase,
        private val sendLoginOtpUseCase: SendLoginOtpUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(RegisterViewModel::class.java)) {
                return RegisterViewModel(registerUseCase, sendLoginOtpUseCase) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}

/**
 * Sealed class representing the UI state for the registration screen.
 */
sealed class RegisterUiState {
    data object Idle : RegisterUiState()
    data object Loading : RegisterUiState()
    data object OtpSent : RegisterUiState()
    data class Success(val authToken: AuthToken) : RegisterUiState()
    data class Error(val message: String) : RegisterUiState()
}

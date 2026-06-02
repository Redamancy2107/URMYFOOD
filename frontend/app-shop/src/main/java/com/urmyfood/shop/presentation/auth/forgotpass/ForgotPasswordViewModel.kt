package com.urmyfood.shop.presentation.auth.forgotpass

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.urmyfood.shared.domain.model.Result
import com.urmyfood.shared.domain.usecase.ForgotPasswordUseCase
import com.urmyfood.shared.domain.usecase.ResetPasswordUseCase
import com.urmyfood.shared.domain.usecase.VerifyOtpUseCase
import kotlinx.coroutines.launch

class ForgotPasswordViewModel(
    private val forgotPasswordUseCase: ForgotPasswordUseCase,
    private val verifyOtpUseCase: VerifyOtpUseCase,
    private val resetPasswordUseCase: ResetPasswordUseCase
) : ViewModel() {

    var email: String = ""
        private set
    var resetToken: String = ""
        private set

    sealed class ForgotUiState {
        object Idle : ForgotUiState()
        object Loading : ForgotUiState()
        object Success : ForgotUiState()
        data class Error(val message: String) : ForgotUiState()
    }

    sealed class OtpUiState {
        object Idle : OtpUiState()
        object Loading : OtpUiState()
        object Success : OtpUiState()
        data class Error(val message: String) : OtpUiState()
    }

    sealed class ResetUiState {
        object Idle : ResetUiState()
        object Loading : ResetUiState()
        object Success : ResetUiState()
        data class Error(val message: String) : ResetUiState()
    }

    private val _forgotUiState = MutableLiveData<ForgotUiState>(ForgotUiState.Idle)
    val forgotUiState: LiveData<ForgotUiState> = _forgotUiState

    private val _otpUiState = MutableLiveData<OtpUiState>(OtpUiState.Idle)
    val otpUiState: LiveData<OtpUiState> = _otpUiState

    private val _resetUiState = MutableLiveData<ResetUiState>(ResetUiState.Idle)
    val resetUiState: LiveData<ResetUiState> = _resetUiState

    fun sendForgotPassword(emailInput: String) {
        if (_forgotUiState.value is ForgotUiState.Loading) return
        _forgotUiState.value = ForgotUiState.Loading
        viewModelScope.launch {
            when (val result = forgotPasswordUseCase(emailInput)) {
                is Result.Success -> {
                    email = emailInput.trim()
                    _forgotUiState.value = ForgotUiState.Success
                }
                is Result.Error -> _forgotUiState.value = ForgotUiState.Error(result.message)
            }
        }
    }

    fun resendForgotPassword() {
        if (email.isBlank()) return
        viewModelScope.launch { forgotPasswordUseCase(email) }
    }

    fun verifyOtp(otpCode: String) {
        if (_otpUiState.value is OtpUiState.Loading) return
        _otpUiState.value = OtpUiState.Loading
        viewModelScope.launch {
            when (val result = verifyOtpUseCase(email, otpCode)) {
                is Result.Success -> {
                    resetToken = result.data
                    _otpUiState.value = OtpUiState.Success
                }
                is Result.Error -> _otpUiState.value = OtpUiState.Error(result.message)
            }
        }
    }

    fun resetPassword(newPassword: String, confirmPassword: String) {
        if (_resetUiState.value is ResetUiState.Loading) return
        _resetUiState.value = ResetUiState.Loading
        viewModelScope.launch {
            when (val result = resetPasswordUseCase(resetToken, newPassword, confirmPassword)) {
                is Result.Success -> _resetUiState.value = ResetUiState.Success
                is Result.Error -> _resetUiState.value = ResetUiState.Error(result.message)
            }
        }
    }

    fun resetForgotState() { _forgotUiState.value = ForgotUiState.Idle }
    fun resetOtpState() { _otpUiState.value = OtpUiState.Idle }
    fun resetResetState() { _resetUiState.value = ResetUiState.Idle }

    class Factory(
        private val forgotPasswordUseCase: ForgotPasswordUseCase,
        private val verifyOtpUseCase: VerifyOtpUseCase,
        private val resetPasswordUseCase: ResetPasswordUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            ForgotPasswordViewModel(forgotPasswordUseCase, verifyOtpUseCase, resetPasswordUseCase) as T
    }
}

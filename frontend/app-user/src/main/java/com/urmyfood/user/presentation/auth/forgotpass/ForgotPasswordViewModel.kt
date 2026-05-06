package com.urmyfood.user.presentation.auth.forgotpass

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.urmyfood.user.domain.model.Result
import com.urmyfood.user.domain.usecase.ForgotPasswordUseCase
import com.urmyfood.user.domain.usecase.ResetPasswordUseCase
import com.urmyfood.user.domain.usecase.VerifyOtpUseCase
import kotlinx.coroutines.launch

/**
 * Shared ViewModel for the Forgot Password flow (ForgotPassword → OTP → ResetPassword).
 * Manages the state across all three screens in the password recovery flow.
 */
class ForgotPasswordViewModel(
    private val forgotPasswordUseCase: ForgotPasswordUseCase,
    private val verifyOtpUseCase: VerifyOtpUseCase,
    private val resetPasswordUseCase: ResetPasswordUseCase
) : ViewModel() {

    // Email entered in ForgotPasswordFragment, shared with OtpFragment
    var email: String = ""
        private set

    // Reset token obtained after OTP verification, used in ResetPasswordFragment
    var resetToken: String = ""
        private set

    fun setEmail(email: String) {
        this.email = email
    }

    // ---- Forgot Password State ----
    private val _forgotPasswordState = MutableLiveData<ForgotPasswordUiState>(ForgotPasswordUiState.Idle)
    val forgotPasswordState: LiveData<ForgotPasswordUiState> = _forgotPasswordState

    // ---- OTP State ----
    private val _otpState = MutableLiveData<OtpUiState>(OtpUiState.Idle)
    val otpState: LiveData<OtpUiState> = _otpState

    // ---- Reset Password State ----
    private val _resetPasswordState = MutableLiveData<ResetPasswordUiState>(ResetPasswordUiState.Idle)
    val resetPasswordState: LiveData<ResetPasswordUiState> = _resetPasswordState

    fun sendOtp(email: String) {
        this.email = email
        _forgotPasswordState.value = ForgotPasswordUiState.Loading

        viewModelScope.launch {
            when (val result = forgotPasswordUseCase(email)) {
                is Result.Success -> {
                    _forgotPasswordState.value = ForgotPasswordUiState.Success
                }
                is Result.Error -> {
                    _forgotPasswordState.value = ForgotPasswordUiState.Error(result.message)
                }
            }
        }
    }

    fun verifyOtp(otpCode: String) {
        if (email.isBlank()) {
            _otpState.value = OtpUiState.Error("Email chưa được cung cấp. Vui lòng quay lại.")
            return
        }

        _otpState.value = OtpUiState.Loading

        viewModelScope.launch {
            when (val result = verifyOtpUseCase(email, otpCode)) {
                is Result.Success -> {
                    resetToken = result.data
                    _otpState.value = OtpUiState.Success
                }
                is Result.Error -> {
                    _otpState.value = OtpUiState.Error(result.message)
                }
            }
        }
    }

    fun resetPassword(newPassword: String, confirmPassword: String) {
        if (resetToken.isBlank()) {
            _resetPasswordState.value = ResetPasswordUiState.Error("Phiên xác thực đã hết hạn. Vui lòng thực hiện lại.")
            return
        }

        _resetPasswordState.value = ResetPasswordUiState.Loading

        viewModelScope.launch {
            when (val result = resetPasswordUseCase(resetToken, newPassword, confirmPassword)) {
                is Result.Success -> {
                    _resetPasswordState.value = ResetPasswordUiState.Success
                }
                is Result.Error -> {
                    _resetPasswordState.value = ResetPasswordUiState.Error(result.message)
                }
            }
        }
    }

    /**
     * Factory for creating ForgotPasswordViewModel with dependencies.
     */
    class Factory(
        private val forgotPasswordUseCase: ForgotPasswordUseCase,
        private val verifyOtpUseCase: VerifyOtpUseCase,
        private val resetPasswordUseCase: ResetPasswordUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ForgotPasswordViewModel::class.java)) {
                return ForgotPasswordViewModel(
                    forgotPasswordUseCase,
                    verifyOtpUseCase,
                    resetPasswordUseCase
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}

// ---- UI State sealed classes ----

sealed class ForgotPasswordUiState {
    data object Idle : ForgotPasswordUiState()
    data object Loading : ForgotPasswordUiState()
    data object Success : ForgotPasswordUiState()
    data class Error(val message: String) : ForgotPasswordUiState()
}

sealed class OtpUiState {
    data object Idle : OtpUiState()
    data object Loading : OtpUiState()
    data object Success : OtpUiState()
    data class Error(val message: String) : OtpUiState()
}

sealed class ResetPasswordUiState {
    data object Idle : ResetPasswordUiState()
    data object Loading : ResetPasswordUiState()
    data object Success : ResetPasswordUiState()
    data class Error(val message: String) : ResetPasswordUiState()
}

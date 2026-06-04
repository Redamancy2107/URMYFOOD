package com.urmyfood.shop.presentation.auth.register

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.urmyfood.shared.domain.usecase.RegisterUseCase
import com.urmyfood.shared.domain.usecase.SendOtpUseCase
import kotlinx.coroutines.launch

class RegisterViewModel(
    private val registerUseCase: RegisterUseCase,
    private val sendOtpUseCase: SendOtpUseCase
) : ViewModel() {

    private var fullName: String = ""
    var email: String = ""
        private set
    private var phone: String = ""
    private var password: String = ""
    private var confirmPassword: String = ""

    sealed class FormUiState {
        object Idle : FormUiState()
        object Loading : FormUiState()
        object OtpSent : FormUiState()
        data class Error(val message: String) : FormUiState()
    }

    sealed class RegisterUiState {
        object Idle : RegisterUiState()
        object Loading : RegisterUiState()
        object Success : RegisterUiState()
        data class Error(val message: String) : RegisterUiState()
    }

    private val _formUiState = MutableLiveData<FormUiState>(FormUiState.Idle)
    val formUiState: LiveData<FormUiState> = _formUiState

    private val _registerUiState = MutableLiveData<RegisterUiState>(RegisterUiState.Idle)
    val registerUiState: LiveData<RegisterUiState> = _registerUiState

    fun submitForm(
        fullName: String,
        email: String,
        phone: String,
        password: String,
        confirmPassword: String,
        termsAccepted: Boolean
    ) {
        if (_formUiState.value is FormUiState.Loading) return

        val validationError = validate(fullName, email, phone, password, confirmPassword, termsAccepted)
        if (validationError != null) {
            _formUiState.value = FormUiState.Error(validationError)
            return
        }

        this.fullName = fullName.trim()
        this.email = email.trim()
        this.phone = phone.trim()
        this.password = password
        this.confirmPassword = confirmPassword

        _formUiState.value = FormUiState.Loading
        viewModelScope.launch {
            _formUiState.value = FormUiState.OtpSent
        }
    }

    fun resendOtp() {
        viewModelScope.launch { sendOtpUseCase(email, phone) }
    }

    fun register(otpCode: String) {
        if (_registerUiState.value is RegisterUiState.Loading) return
        _registerUiState.value = RegisterUiState.Loading
        viewModelScope.launch {
            _registerUiState.value = if (otpCode == MOCK_OTP_CODE) {
                RegisterUiState.Success
            } else {
                RegisterUiState.Error("Mã OTP demo là $MOCK_OTP_CODE")
            }
        }
    }

    fun resetFormState() { _formUiState.value = FormUiState.Idle }
    fun resetRegisterState() { _registerUiState.value = RegisterUiState.Idle }

    private fun validate(
        fullName: String,
        email: String,
        phone: String,
        password: String,
        confirmPassword: String,
        termsAccepted: Boolean
    ): String? {
        if (fullName.isBlank() || email.isBlank() || phone.isBlank() || password.isBlank()) {
            return "Vui lòng điền đầy đủ thông tin"
        }
        if (!emailRegex.matches(email.trim())) return "Email không hợp lệ"
        if (phone.length < 9 || phone.length > 11 || !phone.all { it.isDigit() }) {
            return "Số điện thoại không hợp lệ (9 đến 11 chữ số)"
        }
        if (password.length < 6) return "Mật khẩu phải có ít nhất 6 ký tự"
        if (password != confirmPassword) return "Mật khẩu xác nhận không khớp"
        if (!termsAccepted) return "Vui lòng đồng ý với Điều khoản sử dụng"
        return null
    }

    companion object {
        private val emailRegex = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+\$")
        private const val MOCK_OTP_CODE = "123456"
    }

    class Factory(
        private val registerUseCase: RegisterUseCase,
        private val sendOtpUseCase: SendOtpUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            RegisterViewModel(registerUseCase, sendOtpUseCase) as T
    }
}

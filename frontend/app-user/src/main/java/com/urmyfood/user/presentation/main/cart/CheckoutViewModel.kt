package com.urmyfood.user.presentation.main.cart

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.urmyfood.user.domain.model.Result
import com.urmyfood.user.domain.usecase.CheckoutUseCase
import kotlinx.coroutines.launch

data class CheckoutUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val message: String? = null
)

class CheckoutViewModel(
    private val checkoutUseCase: CheckoutUseCase
) : ViewModel() {

    private val _uiState = MutableLiveData(CheckoutUiState())
    val uiState: LiveData<CheckoutUiState> = _uiState

    fun checkout(paymentMethod: String) {
        viewModelScope.launch {
            _uiState.value = CheckoutUiState(isLoading = true)
            when (val result = checkoutUseCase(paymentMethod, DEFAULT_DELIVERY_ADDRESS, DEFAULT_NOTE)) {
                is Result.Success -> _uiState.value = CheckoutUiState(
                    isSuccess = true,
                    message = "Đặt hàng thành công!"
                )
                is Result.Error -> _uiState.value = CheckoutUiState(message = result.message)
            }
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value?.copy(message = null, isSuccess = false)
    }

    class Factory(
        private val checkoutUseCase: CheckoutUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CheckoutViewModel::class.java)) {
                return CheckoutViewModel(checkoutUseCase) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

    companion object {
        private const val DEFAULT_DELIVERY_ADDRESS = "KTX Khu A, ĐHQG TP.HCM"
        private const val DEFAULT_NOTE = "Đặt từ app URMYFOOD"
    }
}

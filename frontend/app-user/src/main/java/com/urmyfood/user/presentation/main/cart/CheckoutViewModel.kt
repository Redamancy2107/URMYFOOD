package com.urmyfood.user.presentation.main.cart

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.urmyfood.user.data.model.AddressResponse
import com.urmyfood.user.data.model.VoucherResponse
import com.urmyfood.user.domain.model.Result
import com.urmyfood.user.domain.usecase.CheckoutUseCase
import com.urmyfood.user.domain.usecase.DirectCheckoutUseCase
import com.urmyfood.user.domain.usecase.GetAddressesUseCase
import com.urmyfood.user.domain.usecase.GetSavedVouchersUseCase
import com.urmyfood.user.domain.usecase.CreatePayOsPaymentUseCase
import kotlinx.coroutines.launch

data class CheckoutUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val message: String? = null,
    val orderId: String? = null,
    val finalAmount: Long = 0,
    val qrCode: String? = null,
    val paymentMethod: String? = null
)

class CheckoutViewModel(
    private val checkoutUseCase: CheckoutUseCase,
    private val directCheckoutUseCase: DirectCheckoutUseCase,
    private val getAddressesUseCase: GetAddressesUseCase,
    private val getSavedVouchersUseCase: GetSavedVouchersUseCase,
    private val createPayOsPaymentUseCase: CreatePayOsPaymentUseCase
) : ViewModel() {

    private val _uiState = MutableLiveData(CheckoutUiState())
    val uiState: LiveData<CheckoutUiState> = _uiState

    private val _addresses = MutableLiveData<List<AddressResponse>>(emptyList())
    val addresses: LiveData<List<AddressResponse>> = _addresses

    private val _vouchers = MutableLiveData<List<VoucherResponse>>(emptyList())
    val vouchers: LiveData<List<VoucherResponse>> = _vouchers

    private val _selectedAddress = MutableLiveData<AddressResponse?>()
    val selectedAddress: LiveData<AddressResponse?> = _selectedAddress

    private val _selectedVoucher = MutableLiveData<VoucherResponse?>()
    val selectedVoucher: LiveData<VoucherResponse?> = _selectedVoucher

    fun loadData() {
        viewModelScope.launch {
            // Load addresses
            when (val result = getAddressesUseCase()) {
                is Result.Success -> {
                    _addresses.value = result.data
                    val defaultAddr = result.data.find { it.isDefault } ?: result.data.firstOrNull()
                    _selectedAddress.value = defaultAddr
                }
                is Result.Error -> { /* silently ignore address load error */ }
            }

            // Load saved vouchers
            when (val result = getSavedVouchersUseCase()) {
                is Result.Success -> {
                    _vouchers.value = result.data
                }
                is Result.Error -> { /* silently ignore voucher load error */ }
            }
        }
    }

    fun selectAddress(address: AddressResponse) {
        _selectedAddress.value = address
    }

    fun selectVoucher(voucher: VoucherResponse?) {
        _selectedVoucher.value = voucher
    }

    fun checkout(paymentMethod: String, deliveryAddress: String, note: String? = null, voucherCode: String? = null) {
        viewModelScope.launch {
            _uiState.value = CheckoutUiState(isLoading = true)
            val address = if (deliveryAddress.isBlank()) DEFAULT_DELIVERY_ADDRESS else deliveryAddress
            val finalNote = if (note.isNullOrBlank()) DEFAULT_NOTE else note
            val finalVoucher = if (voucherCode.isNullOrBlank()) null else voucherCode.trim()

            when (val result = checkoutUseCase(paymentMethod, address, finalNote, finalVoucher)) {
                is Result.Success -> handleCheckoutSuccess(result.data, paymentMethod)
                is Result.Error -> _uiState.value = CheckoutUiState(message = result.message)
            }
        }
    }

    fun directCheckout(
        postId: String,
        quantity: Int,
        paymentMethod: String,
        deliveryAddress: String,
        note: String? = null,
        voucherCode: String? = null
    ) {
        viewModelScope.launch {
            _uiState.value = CheckoutUiState(isLoading = true)
            val address = if (deliveryAddress.isBlank()) DEFAULT_DELIVERY_ADDRESS else deliveryAddress
            val finalNote = if (note.isNullOrBlank()) DEFAULT_NOTE else note
            val finalVoucher = if (voucherCode.isNullOrBlank()) null else voucherCode.trim()

            when (val result = directCheckoutUseCase(postId, quantity, paymentMethod, address, finalNote, finalVoucher)) {
                is Result.Success -> handleCheckoutSuccess(result.data, paymentMethod)
                is Result.Error -> _uiState.value = CheckoutUiState(message = result.message)
            }
        }
    }

    private suspend fun handleCheckoutSuccess(
        order: com.urmyfood.user.data.model.OrderResponse,
        paymentMethod: String
    ) {
        val messageText = if (paymentMethod == PAYMENT_VIETQR) {
            "Đặt hàng thành công! Vui lòng chờ quán xác nhận trước khi thanh toán."
        } else {
            "Đặt hàng thành công! Vui lòng chờ quán xác nhận."
        }
        _uiState.value = CheckoutUiState(
            isSuccess = true,
            message = messageText,
            orderId = order.orderId,
            finalAmount = order.finalAmount.toLong(),
            paymentMethod = paymentMethod
        )
    }

    fun clearMessage() {
        _uiState.value = _uiState.value?.copy(message = null, isSuccess = false)
    }

    class Factory(
        private val checkoutUseCase: CheckoutUseCase,
        private val directCheckoutUseCase: DirectCheckoutUseCase,
        private val getAddressesUseCase: GetAddressesUseCase,
        private val getSavedVouchersUseCase: GetSavedVouchersUseCase,
        private val createPayOsPaymentUseCase: CreatePayOsPaymentUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CheckoutViewModel::class.java)) {
                return CheckoutViewModel(
                    checkoutUseCase,
                    directCheckoutUseCase,
                    getAddressesUseCase,
                    getSavedVouchersUseCase,
                    createPayOsPaymentUseCase
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

    companion object {
        private const val DEFAULT_DELIVERY_ADDRESS = "KTX Khu A, ĐHQG TP.HCM"
        private const val DEFAULT_NOTE = "Đặt từ app URMYFOOD"
        private const val PAYMENT_VIETQR = "VIETQR"
    }
}

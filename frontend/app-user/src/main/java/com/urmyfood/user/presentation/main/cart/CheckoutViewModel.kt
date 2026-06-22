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
import com.urmyfood.user.domain.usecase.GetAddressesUseCase
import com.urmyfood.user.domain.usecase.GetVouchersUseCase
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
    private val getAddressesUseCase: GetAddressesUseCase,
    private val getVouchersUseCase: GetVouchersUseCase,
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

            // Load vouchers
            when (val result = getVouchersUseCase()) {
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
                is Result.Success -> {
                    val order = result.data
                    if (paymentMethod == PAYMENT_VIETQR) {
                        when (val payOsResult = createPayOsPaymentUseCase(order.orderId)) {
                            is Result.Success -> {
                                _uiState.value = CheckoutUiState(
                                    isSuccess = true,
                                    message = "Tạo mã thanh toán thành công!",
                                    orderId = order.orderId,
                                    finalAmount = order.finalAmount.toLong(),
                                    qrCode = payOsResult.data.qrCode,
                                    paymentMethod = paymentMethod
                                )
                            }
                            is Result.Error -> {
                                _uiState.value = CheckoutUiState(
                                    isSuccess = true,
                                    message = "Đặt hàng thành công! Vui lòng chờ quán xác nhận trước khi thanh toán.",
                                    orderId = order.orderId,
                                    finalAmount = order.finalAmount.toLong(),
                                    paymentMethod = paymentMethod
                                )
                            }
                        }
                    } else {
                        _uiState.value = CheckoutUiState(
                            isSuccess = true,
                            message = "Đặt hàng thành công!",
                            orderId = order.orderId,
                            finalAmount = order.finalAmount.toLong(),
                            paymentMethod = paymentMethod
                        )
                    }
                }
                is Result.Error -> _uiState.value = CheckoutUiState(message = result.message)
            }
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value?.copy(message = null, isSuccess = false)
    }

    class Factory(
        private val checkoutUseCase: CheckoutUseCase,
        private val getAddressesUseCase: GetAddressesUseCase,
        private val getVouchersUseCase: GetVouchersUseCase,
        private val createPayOsPaymentUseCase: CreatePayOsPaymentUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CheckoutViewModel::class.java)) {
                return CheckoutViewModel(checkoutUseCase, getAddressesUseCase, getVouchersUseCase, createPayOsPaymentUseCase) as T
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

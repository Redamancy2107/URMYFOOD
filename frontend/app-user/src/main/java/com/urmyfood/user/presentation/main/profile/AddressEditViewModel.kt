package com.urmyfood.user.presentation.main.profile

import androidx.lifecycle.*
import com.urmyfood.user.data.model.AddressResponse
import com.urmyfood.user.domain.model.Result
import com.urmyfood.user.domain.usecase.CreateAddressUseCase
import com.urmyfood.user.domain.usecase.GetAddressesUseCase
import com.urmyfood.user.domain.usecase.UpdateAddressUseCase
import kotlinx.coroutines.launch

sealed class AddressEditUiState {
    object Idle : AddressEditUiState()
    object Loading : AddressEditUiState()
    object SaveSuccess : AddressEditUiState()
    data class Loaded(val address: AddressResponse) : AddressEditUiState()
    data class NewAddressInit(val isFirst: Boolean) : AddressEditUiState()
    data class Error(val message: String) : AddressEditUiState()
}

class AddressEditViewModel(
    private val getAddressesUseCase: GetAddressesUseCase,
    private val createAddressUseCase: CreateAddressUseCase,
    private val updateAddressUseCase: UpdateAddressUseCase
) : ViewModel() {

    private val _uiState = MutableLiveData<AddressEditUiState>(AddressEditUiState.Idle)
    val uiState: LiveData<AddressEditUiState> = _uiState

    private var _addressCount = 0
    val isFirstAddress: Boolean get() = _addressCount == 0

    fun loadAddressForEdit(addressId: Long) {
        _uiState.value = AddressEditUiState.Loading
        viewModelScope.launch {
            when (val result = getAddressesUseCase()) {
                is Result.Success -> {
                    _addressCount = result.data.size
                    val address = result.data.find { it.id == addressId }
                    if (address != null) {
                        _uiState.value = AddressEditUiState.Loaded(address)
                    } else {
                        _uiState.value = AddressEditUiState.Error("Không tìm thấy địa chỉ")
                    }
                }
                is Result.Error -> _uiState.value = AddressEditUiState.Error(result.message)
            }
        }
    }

    fun loadAddressCount() {
        _uiState.value = AddressEditUiState.Loading
        viewModelScope.launch {
            when (val result = getAddressesUseCase()) {
                is Result.Success -> {
                    _addressCount = result.data.size
                    _uiState.value = AddressEditUiState.NewAddressInit(_addressCount == 0)
                }
                is Result.Error -> _uiState.value = AddressEditUiState.Error(result.message)
            }
        }
    }

    fun saveAddress(
        addressId: Long?,
        label: String, name: String, phone: String, detail: String, isDefault: Boolean
    ) {
        _uiState.value = AddressEditUiState.Loading
        viewModelScope.launch {
            val result = if (addressId != null) {
                updateAddressUseCase(addressId, label, name, phone, detail, isDefault)
            } else {
                createAddressUseCase(label, name, phone, detail, isDefault)
            }
            when (result) {
                is Result.Success -> _uiState.value = AddressEditUiState.SaveSuccess
                is Result.Error -> _uiState.value = AddressEditUiState.Error(result.message)
            }
        }
    }

    class Factory(
        private val getAddressesUseCase: GetAddressesUseCase,
        private val createAddressUseCase: CreateAddressUseCase,
        private val updateAddressUseCase: UpdateAddressUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AddressEditViewModel(getAddressesUseCase, createAddressUseCase, updateAddressUseCase) as T
        }
    }
}

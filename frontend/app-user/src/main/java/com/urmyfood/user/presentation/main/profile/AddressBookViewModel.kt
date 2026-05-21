package com.urmyfood.user.presentation.main.profile

import androidx.lifecycle.*
import com.urmyfood.user.data.model.AddressResponse
import com.urmyfood.user.domain.model.Result
import com.urmyfood.user.domain.usecase.DeleteAddressUseCase
import com.urmyfood.user.domain.usecase.GetAddressesUseCase
import com.urmyfood.user.domain.usecase.SetDefaultAddressUseCase
import kotlinx.coroutines.launch

sealed class AddressBookUiState {
    object Idle : AddressBookUiState()
    object Loading : AddressBookUiState()
    data class Success(val addresses: List<AddressResponse>) : AddressBookUiState()
    data class Error(val message: String) : AddressBookUiState()
}

class AddressBookViewModel(
    private val getAddressesUseCase: GetAddressesUseCase,
    private val deleteAddressUseCase: DeleteAddressUseCase,
    private val setDefaultAddressUseCase: SetDefaultAddressUseCase
) : ViewModel() {

    private val _uiState = MutableLiveData<AddressBookUiState>(AddressBookUiState.Idle)
    val uiState: LiveData<AddressBookUiState> = _uiState

    private val _actionMessage = MutableLiveData<String?>()
    val actionMessage: LiveData<String?> = _actionMessage

    fun loadAddresses() {
        _uiState.value = AddressBookUiState.Loading
        viewModelScope.launch {
            when (val result = getAddressesUseCase()) {
                is Result.Success -> _uiState.value = AddressBookUiState.Success(result.data)
                is Result.Error -> _uiState.value = AddressBookUiState.Error(result.message)
            }
        }
    }

    fun deleteAddress(id: Long) {
        viewModelScope.launch {
            when (val result = deleteAddressUseCase(id)) {
                is Result.Success -> {
                    _actionMessage.value = "Đã xóa địa chỉ"
                    loadAddresses()
                }
                is Result.Error -> _actionMessage.value = result.message
            }
        }
    }

    fun setDefault(id: Long) {
        viewModelScope.launch {
            when (val result = setDefaultAddressUseCase(id)) {
                is Result.Success -> {
                    _actionMessage.value = "Đã đặt làm địa chỉ mặc định"
                    loadAddresses()
                }
                is Result.Error -> _actionMessage.value = result.message
            }
        }
    }

    fun clearActionMessage() {
        _actionMessage.value = null
    }

    class Factory(
        private val getAddressesUseCase: GetAddressesUseCase,
        private val deleteAddressUseCase: DeleteAddressUseCase,
        private val setDefaultAddressUseCase: SetDefaultAddressUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AddressBookViewModel(getAddressesUseCase, deleteAddressUseCase, setDefaultAddressUseCase) as T
        }
    }
}

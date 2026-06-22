package com.urmyfood.user.presentation.main.profile

import androidx.lifecycle.*
import com.urmyfood.user.data.model.VoucherResponse
import com.urmyfood.user.domain.model.Result
import com.urmyfood.user.domain.usecase.GetSavedVouchersUseCase
import kotlinx.coroutines.launch

sealed class VouchersUiState {
    object Idle : VouchersUiState()
    object Loading : VouchersUiState()
    data class Success(val vouchers: List<VoucherResponse>) : VouchersUiState()
    data class Error(val message: String) : VouchersUiState()
}

class VouchersViewModel(
    private val getSavedVouchersUseCase: GetSavedVouchersUseCase
) : ViewModel() {

    private val _uiState = MutableLiveData<VouchersUiState>(VouchersUiState.Idle)
    val uiState: LiveData<VouchersUiState> = _uiState

    fun loadVouchers() {
        _uiState.value = VouchersUiState.Loading
        viewModelScope.launch {
            when (val result = getSavedVouchersUseCase()) {
                is Result.Success -> _uiState.value = VouchersUiState.Success(result.data)
                is Result.Error -> _uiState.value = VouchersUiState.Error(result.message)
            }
        }
    }

    class Factory(
        private val getSavedVouchersUseCase: GetSavedVouchersUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return VouchersViewModel(getSavedVouchersUseCase) as T
        }
    }
}

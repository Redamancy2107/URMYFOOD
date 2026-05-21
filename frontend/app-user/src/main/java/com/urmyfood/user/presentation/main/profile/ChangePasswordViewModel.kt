package com.urmyfood.user.presentation.main.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.urmyfood.user.domain.model.Result
import com.urmyfood.user.domain.usecase.ChangePasswordUseCase
import kotlinx.coroutines.launch

sealed class ChangePasswordUiState {
    object Idle : ChangePasswordUiState()
    object Loading : ChangePasswordUiState()
    object Success : ChangePasswordUiState()
    data class Error(val message: String) : ChangePasswordUiState()
}

class ChangePasswordViewModel(
    private val changePasswordUseCase: ChangePasswordUseCase
) : ViewModel() {

    private val _uiState = MutableLiveData<ChangePasswordUiState>(ChangePasswordUiState.Idle)
    val uiState: LiveData<ChangePasswordUiState> = _uiState

    fun changePassword(currentPass: String, newPass: String) {
        _uiState.value = ChangePasswordUiState.Loading
        viewModelScope.launch {
            when (val result = changePasswordUseCase(currentPass, newPass)) {
                is Result.Success -> _uiState.value = ChangePasswordUiState.Success
                is Result.Error -> _uiState.value = ChangePasswordUiState.Error(result.message)
            }
        }
    }

    class Factory(
        private val changePasswordUseCase: ChangePasswordUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ChangePasswordViewModel::class.java)) {
                return ChangePasswordViewModel(changePasswordUseCase) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

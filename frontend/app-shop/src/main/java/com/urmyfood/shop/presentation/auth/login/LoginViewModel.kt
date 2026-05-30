package com.urmyfood.shop.presentation.auth.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.urmyfood.shared.data.local.TokenStore
import com.urmyfood.shared.domain.model.Result
import com.urmyfood.shared.domain.usecase.LoginUseCase
import kotlinx.coroutines.launch

class LoginViewModel(
    private val loginUseCase: LoginUseCase,
    private val tokenStore: TokenStore
) : ViewModel() {

    companion object {
        private const val ROLE_SHOP = "SHOP"
    }

    sealed class UiState {
        object Idle : UiState()
        object Loading : UiState()
        object Success : UiState()
        data class Error(val message: String) : UiState()
    }

    private val _uiState = MutableLiveData<UiState>(UiState.Idle)
    val uiState: LiveData<UiState> = _uiState

    fun login(emailOrPhone: String, password: String) {
        if (_uiState.value is UiState.Loading) return
        _uiState.value = UiState.Loading
        viewModelScope.launch {
            when (val result = loginUseCase(emailOrPhone, password)) {
                is Result.Success -> {
                    val token = result.data
                    if (token.role != ROLE_SHOP) {
                        _uiState.value = UiState.Error("Tài khoản không hợp lệ")
                    } else {
                        tokenStore.saveToken(
                            token = token.accessToken,
                            refreshToken = token.refreshToken,
                            fullName = token.fullName,
                            role = token.role
                        )
                        _uiState.value = UiState.Success
                    }
                }
                is Result.Error -> _uiState.value = UiState.Error(result.message)
            }
        }
    }

    fun resetState() { _uiState.value = UiState.Idle }

    class Factory(
        private val loginUseCase: LoginUseCase,
        private val tokenStore: TokenStore
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            LoginViewModel(loginUseCase, tokenStore) as T
    }
}

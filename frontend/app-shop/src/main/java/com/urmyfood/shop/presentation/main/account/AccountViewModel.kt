package com.urmyfood.shop.presentation.main.account

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.urmyfood.shared.data.local.TokenStore
import com.urmyfood.shared.domain.model.Result
import com.urmyfood.shop.domain.model.ShopProfile
import com.urmyfood.shop.domain.usecase.GetShopProfileUseCase
import kotlinx.coroutines.launch

sealed class AccountUiState {
    object Idle : AccountUiState()
    object Loading : AccountUiState()
    data class Success(val profile: ShopProfile) : AccountUiState()
    data class Error(val message: String) : AccountUiState()
}

class AccountViewModel(
    private val getShopProfileUseCase: GetShopProfileUseCase,
    private val tokenStore: TokenStore
) : ViewModel() {

    private val _uiState = MutableLiveData<AccountUiState>(AccountUiState.Idle)
    val uiState: LiveData<AccountUiState> = _uiState

    fun loadProfile() {
        _uiState.value = AccountUiState.Loading
        viewModelScope.launch {
            when (val result = getShopProfileUseCase()) {
                is Result.Success -> _uiState.value = AccountUiState.Success(result.data)
                is Result.Error -> _uiState.value = AccountUiState.Error(result.message)
            }
        }
    }

    fun logout() {
        tokenStore.clear()
    }

    class Factory(
        private val getShopProfileUseCase: GetShopProfileUseCase,
        private val tokenStore: TokenStore
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AccountViewModel::class.java)) {
                return AccountViewModel(getShopProfileUseCase, tokenStore) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

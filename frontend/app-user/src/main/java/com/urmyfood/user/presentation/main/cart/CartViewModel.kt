package com.urmyfood.user.presentation.main.cart

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.urmyfood.user.data.model.CartResponse
import com.urmyfood.user.data.model.toDomain
import com.urmyfood.user.domain.model.CartItem
import com.urmyfood.user.domain.model.Result
import com.urmyfood.user.domain.usecase.DeleteCartItemUseCase
import com.urmyfood.user.domain.usecase.GetCartUseCase
import com.urmyfood.user.domain.usecase.UpdateCartItemUseCase
import kotlinx.coroutines.launch

data class CartUiState(
    val isLoading: Boolean = false,
    val items: List<CartItem> = emptyList(),
    val totalAmount: Double = 0.0,
    val message: String? = null
)

class CartViewModel(
    private val getCartUseCase: GetCartUseCase,
    private val updateCartItemUseCase: UpdateCartItemUseCase,
    private val deleteCartItemUseCase: DeleteCartItemUseCase
) : ViewModel() {

    private val _uiState = MutableLiveData(CartUiState(isLoading = true))
    val uiState: LiveData<CartUiState> = _uiState

    fun loadCart() {
        viewModelScope.launch {
            _uiState.value = _uiState.value?.copy(isLoading = true, message = null)
            when (val result = getCartUseCase()) {
                is Result.Success -> applyCart(result.data)
                is Result.Error -> _uiState.value = CartUiState(message = result.message)
            }
        }
    }

    fun updateQuantity(item: CartItem, quantity: Int) {
        val itemId = item.cartItemId ?: return setMessage("Không tìm thấy mã món trong giỏ")
        if (quantity < 1) {
            deleteItem(item)
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value?.copy(isLoading = true, message = null)
            when (val result = updateCartItemUseCase(itemId, quantity)) {
                is Result.Success -> applyCart(result.data)
                is Result.Error -> {
                    _uiState.value = _uiState.value?.copy(isLoading = false, message = result.message)
                    loadCart()
                }
            }
        }
    }

    fun deleteItem(item: CartItem) {
        val itemId = item.cartItemId ?: return setMessage("Không tìm thấy mã món trong giỏ")
        viewModelScope.launch {
            _uiState.value = _uiState.value?.copy(isLoading = true, message = null)
            when (val result = deleteCartItemUseCase(itemId)) {
                is Result.Success -> loadCart()
                is Result.Error -> _uiState.value = _uiState.value?.copy(isLoading = false, message = result.message)
            }
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value?.copy(message = null)
    }

    private fun applyCart(cart: CartResponse) {
        _uiState.value = CartUiState(
            isLoading = false,
            items = cart.items.map { it.toDomain() },
            totalAmount = cart.totalAmount
        )
    }

    private fun setMessage(message: String) {
        _uiState.value = _uiState.value?.copy(message = message)
    }

    class Factory(
        private val getCartUseCase: GetCartUseCase,
        private val updateCartItemUseCase: UpdateCartItemUseCase,
        private val deleteCartItemUseCase: DeleteCartItemUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CartViewModel::class.java)) {
                return CartViewModel(getCartUseCase, updateCartItemUseCase, deleteCartItemUseCase) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

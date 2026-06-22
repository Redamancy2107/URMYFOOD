package com.urmyfood.shop.presentation.main.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.urmyfood.shared.domain.model.ChatSession
import com.urmyfood.shared.domain.model.Result
import com.urmyfood.shop.domain.usecase.GetChatSessionsUseCase
import kotlinx.coroutines.launch

class ChatViewModel(
    private val getChatSessionsUseCase: GetChatSessionsUseCase
) : ViewModel() {

    sealed class UiState {
        object Loading : UiState()
        data class Success(val sessions: List<ChatSession>) : UiState()
        data class Error(val message: String) : UiState()
    }

    private val _uiState = MutableLiveData<UiState>()
    val uiState: LiveData<UiState> = _uiState

    private var allSessions: List<ChatSession> = emptyList()

    init {
        loadSessions()
    }

    fun loadSessions() {
        _uiState.value = UiState.Loading
        viewModelScope.launch {
            when (val result = getChatSessionsUseCase()) {
                is Result.Success -> {
                    allSessions = result.data
                    _uiState.value = UiState.Success(result.data)
                }
                is Result.Error -> _uiState.value = UiState.Error(result.message)
            }
        }
    }

    fun filterSessions(query: String) {
        val filtered = if (query.isBlank()) allSessions
        else allSessions.filter { it.customerName.contains(query.trim(), ignoreCase = true) }
        _uiState.value = UiState.Success(filtered)
    }

    class Factory(
        private val getChatSessionsUseCase: GetChatSessionsUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            ChatViewModel(getChatSessionsUseCase) as T
    }
}

package com.urmyfood.shop.presentation.main.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.urmyfood.shared.data.remote.StompClient
import com.urmyfood.shared.domain.model.ChatMessage
import com.urmyfood.shared.domain.model.Result
import com.urmyfood.shared.util.Event
import com.urmyfood.shop.data.model.ChatMessageDto
import com.urmyfood.shop.data.model.toDomain
import com.urmyfood.shop.domain.repository.ChatRepository
import com.urmyfood.shop.domain.usecase.GetMessagesUseCase
import com.urmyfood.shop.domain.usecase.MarkAsReadUseCase
import com.urmyfood.shop.domain.usecase.SendMessageUseCase
import kotlinx.coroutines.launch

class ChatDetailViewModel(
    private val sessionId: Long,
    private val wsUrl: String,
    private val accessToken: String,
    private val chatRepository: ChatRepository,
    private val getMessagesUseCase: GetMessagesUseCase,
    private val sendMessageUseCase: SendMessageUseCase,
    private val markAsReadUseCase: MarkAsReadUseCase
) : ViewModel() {

    sealed class UiState {
        object Loading : UiState()
        data class Success(val messages: List<ChatMessage>) : UiState()
        data class Error(val message: String) : UiState()
    }

    private val _uiState = MutableLiveData<UiState>()
    val uiState: LiveData<UiState> = _uiState

    private val _incomingMessage = MutableLiveData<Event<ChatMessage>>()
    val incomingMessage: LiveData<Event<ChatMessage>> = _incomingMessage

    private val gson = Gson()

    fun loadHistory() {
        _uiState.value = UiState.Loading
        viewModelScope.launch {
            when (val result = getMessagesUseCase(sessionId)) {
                is Result.Success -> _uiState.value = UiState.Success(result.data)
                is Result.Error -> _uiState.value = UiState.Error(result.message)
            }
            markAsReadUseCase(sessionId)
        }
    }

    fun connectWebSocket() {
        chatRepository.connectWebSocket(wsUrl, accessToken)
        chatRepository.subscribeToSession(sessionId) { json ->
            try {
                val dto = gson.fromJson(json, ChatMessageDto::class.java)
                _incomingMessage.postValue(Event(dto.toDomain()))
            } catch (e: JsonSyntaxException) {
                // ignore malformed frames
            }
        }
    }

    fun sendMessage(content: String) {
        if (content.isBlank()) return
        sendMessageUseCase(sessionId, content)
    }

    fun disconnectWebSocket() {
        chatRepository.disconnectWebSocket()
    }

    class Factory(
        private val sessionId: Long,
        private val wsUrl: String,
        private val accessToken: String,
        private val chatRepository: ChatRepository,
        private val getMessagesUseCase: GetMessagesUseCase,
        private val sendMessageUseCase: SendMessageUseCase,
        private val markAsReadUseCase: MarkAsReadUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            ChatDetailViewModel(
                sessionId, wsUrl, accessToken,
                chatRepository, getMessagesUseCase, sendMessageUseCase, markAsReadUseCase
            ) as T
    }
}

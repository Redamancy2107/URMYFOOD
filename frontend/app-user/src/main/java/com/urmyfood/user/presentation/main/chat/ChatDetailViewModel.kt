package com.urmyfood.user.presentation.main.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.urmyfood.shared.util.ChatImageMultipartBuilder
import com.urmyfood.shared.domain.model.ChatMessage
import com.urmyfood.shared.domain.model.Result
import com.urmyfood.shared.util.Event
import com.urmyfood.user.data.model.ChatMessageDto
import com.urmyfood.user.data.model.toDomain
import com.urmyfood.user.domain.repository.ChatRepository
import com.urmyfood.user.domain.usecase.GetChatMessagesUseCase
import com.urmyfood.user.domain.usecase.MarkChatAsReadUseCase
import com.urmyfood.user.domain.usecase.SendChatMessageUseCase
import com.urmyfood.user.domain.usecase.UploadChatImageUseCase
import kotlinx.coroutines.launch
import android.content.Context
import android.net.Uri
import android.util.Log

class ChatDetailViewModel(
    private val sessionId: Long,
    private val wsUrl: String,
    private val accessToken: String,
    private val chatRepository: ChatRepository,
    private val getMessagesUseCase: GetChatMessagesUseCase,
    private val sendMessageUseCase: SendChatMessageUseCase,
    private val markAsReadUseCase: MarkChatAsReadUseCase,
    private val uploadChatImageUseCase: UploadChatImageUseCase
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

    private val _uploadError = MutableLiveData<Event<String>>()
    val uploadError: LiveData<Event<String>> = _uploadError

    private val gson = Gson()

    companion object {
        private const val TAG = "ChatDetailViewModel"
    }

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
                Log.e(TAG, "Failed to parse incoming chat message: ${json.take(200)}", e)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to handle incoming chat message: ${json.take(200)}", e)
            }
        }
    }

    fun sendMessage(content: String) {
        if (content.isBlank()) return
        sendMessageUseCase(sessionId, content)
    }

    fun sendImageMessage(uri: Uri, context: Context) {
        viewModelScope.launch {
            try {
                val filePart = when (val buildResult = ChatImageMultipartBuilder.build(context, uri)) {
                    is Result.Success -> buildResult.data
                    is Result.Error -> {
                        Log.e(TAG, "Invalid chat image: ${buildResult.message}")
                        _uploadError.value = Event(buildResult.message)
                        return@launch
                    }
                }

                when (val result = uploadChatImageUseCase(sessionId, filePart)) {
                    is Result.Success -> {
                        val imageUrl = result.data
                        if (imageUrl.isBlank()) {
                            _uploadError.value = Event("Không nhận được đường dẫn ảnh")
                        } else {
                            chatRepository.sendImageViaWebSocket(sessionId, imageUrl)
                        }
                    }
                    is Result.Error -> {
                        Log.e(TAG, "Chat image upload failed: ${result.message}")
                        _uploadError.value = Event(result.message.ifBlank { "Tải ảnh thất bại" })
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to upload chat image", e)
                _uploadError.value = Event("Tải ảnh thất bại")
            }
        }
    }

    fun disconnectWebSocket() {
        chatRepository.disconnectWebSocket()
    }

    class Factory(
        private val sessionId: Long,
        private val wsUrl: String,
        private val accessToken: String,
        private val chatRepository: ChatRepository,
        private val getMessagesUseCase: GetChatMessagesUseCase,
        private val sendMessageUseCase: SendChatMessageUseCase,
        private val markAsReadUseCase: MarkChatAsReadUseCase,
        private val uploadChatImageUseCase: UploadChatImageUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            ChatDetailViewModel(
                sessionId, wsUrl, accessToken,
                chatRepository, getMessagesUseCase, sendMessageUseCase, markAsReadUseCase,
                uploadChatImageUseCase
            ) as T
    }
}

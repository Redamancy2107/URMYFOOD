package com.urmyfood.shop.presentation.main.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ChatViewModel : ViewModel() {

    // Mock data - remove when BE ready
    data class ChatSession(
        val id: String,
        val customerName: String,
        val lastMessage: String,
        val timestamp: String,
        val unreadCount: Int
    )

    private val _chatSessions = MutableLiveData<List<ChatSession>>()
    val chatSessions: LiveData<List<ChatSession>> = _chatSessions

    init {
        loadMockData()
    }

    // Mock data - remove when BE ready
    private fun loadMockData() {
        _chatSessions.value = listOf(
            ChatSession(
                id = "chat_1",
                customerName = "Nguyễn Văn A",
                lastMessage = "Cho mình hỏi còn cơm tấm không?",
                timestamp = "10:30",
                unreadCount = 2
            ),
            ChatSession(
                id = "chat_2",
                customerName = "Trần Thị B",
                lastMessage = "Dạ mình đã nhận được đơn rồi, cảm ơn shop!",
                timestamp = "09:45",
                unreadCount = 0
            ),
            ChatSession(
                id = "chat_3",
                customerName = "Lê Hoàng C",
                lastMessage = "Ship tới ký túc xá được không ạ?",
                timestamp = "Hôm qua",
                unreadCount = 1
            ),
            ChatSession(
                id = "chat_4",
                customerName = "Phạm Minh D",
                lastMessage = "Mình muốn đặt 5 phần cho nhóm",
                timestamp = "Hôm qua",
                unreadCount = 3
            ),
            ChatSession(
                id = "chat_5",
                customerName = "Võ Thanh E",
                lastMessage = "Cảm ơn shop nhiều nha!",
                timestamp = "T2",
                unreadCount = 0
            )
        )
    }
}

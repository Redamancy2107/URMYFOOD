package com.urmyfood.user.domain.repository

interface SearchHistoryRepository {
    fun getHistory(): List<String>
    fun addQuery(query: String): List<String>
    fun removeQuery(query: String): List<String>
    fun clearHistory()
}

package com.urmyfood.user.domain.usecase

import com.urmyfood.user.domain.repository.SearchHistoryRepository

class GetSearchHistoryUseCase(private val repository: SearchHistoryRepository) {
    operator fun invoke(): List<String> = repository.getHistory()
}

class AddSearchHistoryUseCase(private val repository: SearchHistoryRepository) {
    operator fun invoke(query: String): List<String> = repository.addQuery(query)
}

class RemoveSearchHistoryUseCase(private val repository: SearchHistoryRepository) {
    operator fun invoke(query: String): List<String> = repository.removeQuery(query)
}

class ClearSearchHistoryUseCase(private val repository: SearchHistoryRepository) {
    operator fun invoke() = repository.clearHistory()
}

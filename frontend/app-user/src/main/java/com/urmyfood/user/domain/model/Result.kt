package com.urmyfood.user.domain.model

/**
 * Sealed class representing the result of an operation.
 * Used throughout the domain and presentation layers.
 */
sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val message: String, val code: Int? = null) : Result<Nothing>()
}

package com.cashi.challenge.domain.result

/**
 * Unified result type for operations across the app.
 * Replaces mixed use of Result/Exceptions to standardize error handling.
 */
sealed class OperationResult<out T> {
    data class Success<T>(val data: T) : OperationResult<T>()
    data class Failure(val error: Throwable, val message: String = error.message ?: "Unknown error") : OperationResult<Nothing>()
}

inline fun <T, R> OperationResult<T>.map(transform: (T) -> R): OperationResult<R> = when (this) {
    is OperationResult.Success -> OperationResult.Success(transform(data))
    is OperationResult.Failure -> this
}

inline fun <T> OperationResult<T>.onSuccess(action: (T) -> Unit): OperationResult<T> = apply {
    if (this is OperationResult.Success) {
        action(data)
    }
}

inline fun <T> OperationResult<T>.onFailure(action: (OperationResult.Failure) -> Unit): OperationResult<T> = apply {
    if (this is OperationResult.Failure) {
        action(this)
    }
}

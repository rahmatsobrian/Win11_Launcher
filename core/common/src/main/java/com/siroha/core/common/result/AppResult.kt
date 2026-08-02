package com.siroha.core.common.result

/**
 * Generic wrapper for operations that can succeed, fail, or still be in
 * flight. Used across repositories and ViewModels instead of throwing
 * exceptions across layer boundaries, so UI state can render loading/error
 * without try/catch scattered through Composables.
 */
sealed class AppResult<out T> {
    data class Success<T>(val data: T) : AppResult<T>()
    data class Error(val throwable: Throwable, val message: String? = null) : AppResult<Nothing>()
    data object Loading : AppResult<Nothing>()
}

inline fun <T, R> AppResult<T>.map(transform: (T) -> R): AppResult<R> = when (this) {
    is AppResult.Success -> AppResult.Success(transform(data))
    is AppResult.Error -> this
    is AppResult.Loading -> AppResult.Loading
}

inline fun <T> AppResult<T>.onSuccess(action: (T) -> Unit): AppResult<T> {
    if (this is AppResult.Success) action(data)
    return this
}

inline fun <T> AppResult<T>.onError(action: (Throwable, String?) -> Unit): AppResult<T> {
    if (this is AppResult.Error) action(throwable, message)
    return this
}

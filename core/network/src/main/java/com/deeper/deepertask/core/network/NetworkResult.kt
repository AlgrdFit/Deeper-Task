package com.deeper.deepertask.core.network

sealed interface NetworkResult<out T> {
    data class Success<out T>(val value: T) : NetworkResult<T>

    data class Failure(val error: NetworkError) : NetworkResult<Nothing>
}

sealed interface NetworkError {
    data object Timeout : NetworkError

    data object Transport : NetworkError

    data class Http(val statusCode: Int) : NetworkError

    data object Serialization : NetworkError

    data object Unexpected : NetworkError
}

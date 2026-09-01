package com.deeper.deepertask.core.network

import com.google.gson.JsonParseException
import com.google.gson.stream.MalformedJsonException
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import kotlin.coroutines.cancellation.CancellationException

suspend fun <T> networkCall(block: suspend () -> T): NetworkResult<T> =
    try {
        NetworkResult.Success(block())
    } catch (exception: CancellationException) {
        throw exception
    } catch (_: SocketTimeoutException) {
        NetworkResult.Failure(NetworkError.Timeout)
    } catch (exception: HttpException) {
        NetworkResult.Failure(NetworkError.Http(exception.code()))
    } catch (_: JsonParseException) {
        NetworkResult.Failure(NetworkError.Serialization)
    } catch (_: MalformedJsonException) {
        NetworkResult.Failure(NetworkError.Serialization)
    } catch (_: IOException) {
        NetworkResult.Failure(NetworkError.Transport)
    } catch (_: Exception) {
        NetworkResult.Failure(NetworkError.Unexpected)
    }

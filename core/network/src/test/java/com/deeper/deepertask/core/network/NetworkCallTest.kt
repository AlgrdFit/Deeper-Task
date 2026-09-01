package com.deeper.deepertask.core.network

import com.google.gson.JsonSyntaxException
import com.google.gson.stream.MalformedJsonException
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertThrows
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException
import kotlin.coroutines.cancellation.CancellationException

class NetworkCallTest {
    @Test
    fun `returns successful value`() = runTest {
        // Arrange
        val expected = "response"

        // Act
        val result = networkCall { expected }

        // Assert
        assertEquals(NetworkResult.Success(expected), result)
    }

    @Test
    fun `maps socket timeout to timeout error`() = runTest {
        // Arrange
        val exception = SocketTimeoutException("Timed out")

        // Act
        val result = networkCall<Unit> { throw exception }

        // Assert
        assertEquals(NetworkResult.Failure(NetworkError.Timeout), result)
    }

    @Test
    fun `maps HTTP exception and preserves status code`() = runTest {
        // Arrange
        val responseBody = "".toResponseBody("application/json".toMediaType())
        val exception = HttpException(Response.error<Unit>(401, responseBody))

        // Act
        val result = networkCall<Unit> { throw exception }

        // Assert
        assertEquals(NetworkResult.Failure(NetworkError.Http(401)), result)
    }

    @Test
    fun `maps JSON syntax exception to serialization error`() = runTest {
        // Arrange
        val exception = JsonSyntaxException("Malformed JSON")

        // Act
        val result = networkCall<Unit> { throw exception }

        // Assert
        assertEquals(NetworkResult.Failure(NetworkError.Serialization), result)
    }

    @Test
    fun `maps malformed JSON exception to serialization error`() = runTest {
        // Arrange
        val exception = MalformedJsonException("Malformed JSON")

        // Act
        val result = networkCall<Unit> { throw exception }

        // Assert
        assertEquals(NetworkResult.Failure(NetworkError.Serialization), result)
    }

    @Test
    fun `maps IO exception to transport error`() = runTest {
        // Arrange
        val exception = IOException("Connection failed")

        // Act
        val result = networkCall<Unit> { throw exception }

        // Assert
        assertEquals(NetworkResult.Failure(NetworkError.Transport), result)
    }

    @Test
    fun `maps other exceptions to unexpected error`() = runTest {
        // Arrange
        val exception = IllegalStateException("Unexpected failure")

        // Act
        val result = networkCall<Unit> { throw exception }

        // Assert
        assertEquals(NetworkResult.Failure(NetworkError.Unexpected), result)
    }

    @Test
    fun `rethrows coroutine cancellation`() {
        // Arrange
        val cancellation = CancellationException("Cancelled")

        // Act
        val thrown = assertThrows(CancellationException::class.java) {
            runTest {
                networkCall<Unit> { throw cancellation }
            }
        }

        // Assert
        assertSame(cancellation, thrown)
    }
}

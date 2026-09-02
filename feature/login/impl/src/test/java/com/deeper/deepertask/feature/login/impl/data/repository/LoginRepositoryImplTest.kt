package com.deeper.deepertask.feature.login.impl.data.repository

import com.deeper.deepertask.feature.login.impl.data.remote.LoginApi
import com.deeper.deepertask.feature.login.impl.data.remote.LoginDto
import com.deeper.deepertask.feature.login.impl.data.remote.LoginRequestDto
import com.deeper.deepertask.feature.login.impl.data.remote.LoginResponseDto
import com.deeper.deepertask.feature.login.impl.domain.model.LoginError
import com.deeper.deepertask.feature.login.impl.domain.model.LoginResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class LoginRepositoryImplTest {
    private val email = "angler@example.com"
    private val password = "password"
    private val wrongPassword = "wrong"
    private val sessionToken = "session-token"
    private val emptyResponseBody = ""
    private val jsonMediaType = "application/json".toMediaType()
    private val validRequest = LoginRequestDto(email, password)
    private val invalidCredentialsRequest = LoginRequestDto(email, wrongPassword)
    private val validLoginResponse = LoginResponseDto(
        login = LoginDto(
            token = sessionToken,
            validated = true,
            validTill = "2030-04-26T11:07:22Z",
        ),
        scans = emptyList(),
    )
    private val loginApi = mockk<LoginApi>()

    @Test
    fun `returns mapped token after successful API response`() = runTest {
        // Arrange
        coEvery {
            loginApi.login(validRequest)
        } returns validLoginResponse
        val fixture = fixture()

        // Act
        val result = fixture.login(email, password)

        // Assert
        assertEquals(LoginResult.Success(sessionToken), result)
        coVerify(exactly = 1) {
            loginApi.login(validRequest)
        }
    }

    @Test
    fun `maps unauthorized response to invalid credentials`() = runTest {
        // Arrange
        val unauthorizedException = HttpException(
            Response.error<Unit>(401, emptyResponseBody.toResponseBody(jsonMediaType)),
        )
        coEvery {
            loginApi.login(invalidCredentialsRequest)
        } throws unauthorizedException
        val fixture = fixture()

        // Act
        val result = fixture.login(email, wrongPassword)

        // Assert
        assertEquals(LoginResult.Failure(LoginError.InvalidCredentials), result)
        coVerify(exactly = 1) {
            loginApi.login(invalidCredentialsRequest)
        }
    }

    @Test
    fun `maps IO failure to connectivity error`() = runTest {
        // Arrange
        val connectivityException = IOException("Offline")
        coEvery {
            loginApi.login(validRequest)
        } throws connectivityException
        val fixture = fixture()

        // Act
        val result = fixture.login(email, password)

        // Assert
        assertEquals(LoginResult.Failure(LoginError.Connectivity), result)
        coVerify(exactly = 1) {
            loginApi.login(validRequest)
        }
    }

    @Test
    fun `maps server response to service error`() = runTest {
        // Arrange
        val serverException = HttpException(
            Response.error<Unit>(500, emptyResponseBody.toResponseBody(jsonMediaType)),
        )
        coEvery {
            loginApi.login(validRequest)
        } throws serverException
        val fixture = fixture()

        // Act
        val result = fixture.login(email, password)

        // Assert
        assertEquals(LoginResult.Failure(LoginError.Service), result)
        coVerify(exactly = 1) {
            loginApi.login(validRequest)
        }
    }

    @Test
    fun `maps malformed successful response to invalid response error`() = runTest {
        // Arrange
        val malformedLoginResponse = LoginResponseDto(
            login = LoginDto(token = null, validated = true, validTill = null),
            scans = emptyList(),
        )
        coEvery {
            loginApi.login(validRequest)
        } returns malformedLoginResponse
        val fixture = fixture()

        // Act
        val result = fixture.login(email, password)

        // Assert
        assertEquals(LoginResult.Failure(LoginError.InvalidResponse), result)
        coVerify(exactly = 1) {
            loginApi.login(validRequest)
        }
    }

    private fun fixture(): LoginRepositoryImpl = LoginRepositoryImpl(loginApi)
}

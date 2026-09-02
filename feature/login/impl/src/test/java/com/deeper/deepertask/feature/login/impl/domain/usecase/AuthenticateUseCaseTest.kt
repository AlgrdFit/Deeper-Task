package com.deeper.deepertask.feature.login.impl.domain.usecase

import com.deeper.deepertask.feature.login.impl.domain.model.LoginError
import com.deeper.deepertask.feature.login.impl.domain.model.LoginResult
import com.deeper.deepertask.feature.login.impl.domain.repository.LoginRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class AuthenticateUseCaseTest {
    private val email = "angler@example.com"
    private val emailWithWhitespace = "  $email  "
    private val passwordWithWhitespace = " password "
    private val wrongPassword = "wrong"
    private val sessionToken = "session-token"
    private val successfulLogin = LoginResult.Success(sessionToken)
    private val invalidCredentialsFailure = LoginResult.Failure(LoginError.InvalidCredentials)
    private val loginRepository = mockk<LoginRepository>()

    @Test
    fun `successful authentication trims email and returns repository result`() = runTest {
        // Arrange
        coEvery {
            loginRepository.login(email, passwordWithWhitespace)
        } returns successfulLogin
        val fixture = fixture()

        // Act
        val result = fixture(emailWithWhitespace, passwordWithWhitespace)

        // Assert
        assertEquals(successfulLogin, result)
        coVerify(exactly = 1) {
            loginRepository.login(email, passwordWithWhitespace)
        }
    }

    @Test
    fun `failed authentication returns repository error`() = runTest {
        // Arrange
        coEvery {
            loginRepository.login(email, wrongPassword)
        } returns invalidCredentialsFailure
        val fixture = fixture()

        // Act
        val result = fixture(email, wrongPassword)

        // Assert
        assertEquals(invalidCredentialsFailure, result)
        coVerify(exactly = 1) {
            loginRepository.login(email, wrongPassword)
        }
    }

    private fun fixture(): AuthenticateUseCase = AuthenticateUseCase(loginRepository)
}

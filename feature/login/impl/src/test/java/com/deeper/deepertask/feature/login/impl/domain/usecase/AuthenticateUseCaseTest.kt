package com.deeper.deepertask.feature.login.impl.domain.usecase

import com.deeper.deepertask.feature.login.api.TokenStore
import com.deeper.deepertask.feature.login.impl.domain.model.LoginError
import com.deeper.deepertask.feature.login.impl.domain.model.LoginResult
import com.deeper.deepertask.feature.login.impl.domain.repository.LoginRepository
import com.deeper.deepertask.feature.scans.api.ScanSummary
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class AuthenticateUseCaseTest {
    private val email = "angler@example.com"
    private val emailWithWhitespace = "  $email  "
    private val passwordWithWhitespace = " password "
    private val wrongPassword = "wrong"
    private val initialScans = listOf(
        ScanSummary(id = 42L, name = "Lake scan", date = null),
    )
    private val successfulLogin = LoginResult.Success(
        token = "token",
        scans = initialScans,
    )
    private val invalidCredentialsFailure = LoginResult.Failure(LoginError.InvalidCredentials)
    private val loginRepository = mockk<LoginRepository>()
    private val tokenStore = mockk<TokenStore>()

    @Test
    fun `successful authentication trims email and returns login result`() = runTest {
        // Arrange
        coEvery {
            loginRepository.login(email, passwordWithWhitespace)
        } returns successfulLogin
        every { tokenStore.save(successfulLogin.token) } just Runs
        val fixture = fixture()

        // Act
        val result = fixture(emailWithWhitespace, passwordWithWhitespace)

        // Assert
        assertEquals(successfulLogin, result)
        coVerify(exactly = 1) {
            loginRepository.login(email, passwordWithWhitespace)
        }
        verify(exactly = 1) {
            tokenStore.save(successfulLogin.token)
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
        verify(exactly = 0) {
            tokenStore.save(any())
        }
    }

    private fun fixture(): AuthenticateUseCase = AuthenticateUseCase(loginRepository, tokenStore)
}

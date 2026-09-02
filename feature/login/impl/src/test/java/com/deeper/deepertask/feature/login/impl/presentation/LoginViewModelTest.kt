package com.deeper.deepertask.feature.login.impl.presentation

import com.deeper.deepertask.feature.login.impl.domain.model.LoginError
import com.deeper.deepertask.feature.login.impl.domain.model.LoginResult
import com.deeper.deepertask.feature.login.impl.domain.usecase.AuthenticateUseCase
import com.deeper.deepertask.feature.scans.api.ScanSummary
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {
    private val email = "angler@example.com"
    private val password = "password"
    private val wrongPassword = "wrong"
    private val token = "token"
    private val initialScans = listOf(
        ScanSummary(id = 42L, name = "Lake scan", date = null),
    )
    private val successfulLogin = LoginResult.Success(
        token = token,
        scans = initialScans,
    )
    private val invalidCredentialsFailure = LoginResult.Failure(LoginError.InvalidCredentials)
    private val authenticate = mockk<AuthenticateUseCase>()

    @Test
    fun `empty submit shows field validation without authenticating`() = runTest {
        // Arrange
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        val fixture = fixture()
        fixture.onEmailChanged("")
        fixture.onPasswordChanged("")

        try {
            // Act
            fixture.onSubmit()

            // Assert
            assertTrue(fixture.uiState.value.isEmailMissing)
            assertTrue(fixture.uiState.value.isPasswordMissing)
            assertEquals(LoginStatus.Idle, fixture.uiState.value.status)
            coVerify(exactly = 0) {
                authenticate(any(), any())
            }
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun `successful submit transitions through loading to success`() = runTest {
        // Arrange
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        coEvery {
            authenticate(email, password)
        } returns successfulLogin
        val fixture = fixture()
        fixture.onEmailChanged(email)
        fixture.onPasswordChanged(password)

        try {
            // Act
            fixture.onSubmit()
            val loadingState = fixture.uiState.value
            advanceUntilIdle()
            val navigationEvent = fixture.events.first()

            // Assert
            assertEquals(LoginStatus.Loading, loadingState.status)
            assertEquals(LoginStatus.Success, fixture.uiState.value.status)
            assertEquals(LoginEvent.NavigateToScans(initialScans), navigationEvent)
            coVerify(exactly = 1) {
                authenticate(email, password)
            }
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun `failed submit exposes login error and editing clears it`() = runTest {
        // Arrange
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        coEvery {
            authenticate(email, wrongPassword)
        } returns invalidCredentialsFailure
        val fixture = fixture()
        fixture.onEmailChanged(email)
        fixture.onPasswordChanged(wrongPassword)

        try {
            // Act
            fixture.onSubmit()
            advanceUntilIdle()
            val errorState = fixture.uiState.value
            fixture.onPasswordChanged("new password")

            // Assert
            assertEquals(
                LoginStatus.Error(invalidCredentialsFailure.error),
                errorState.status,
            )
            assertEquals(LoginStatus.Idle, fixture.uiState.value.status)
            coVerify(exactly = 1) {
                authenticate(email, wrongPassword)
            }
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun `second submit is ignored while request is running`() = runTest {
        // Arrange
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        val requestGate = CompletableDeferred<Unit>()
        coEvery {
            authenticate(email, password)
        } coAnswers {
            requestGate.await()
            successfulLogin
        }
        val fixture = fixture()
        fixture.onEmailChanged(email)
        fixture.onPasswordChanged(password)

        try {
            // Act
            fixture.onSubmit()
            fixture.onSubmit()
            runCurrent()

            // Assert
            coVerify(exactly = 1) {
                authenticate(email, password)
            }
            assertEquals(LoginStatus.Loading, fixture.uiState.value.status)

            requestGate.complete(Unit)
            advanceUntilIdle()
            assertEquals(LoginStatus.Success, fixture.uiState.value.status)
        } finally {
            Dispatchers.resetMain()
        }
    }

    private fun fixture(): LoginViewModel = LoginViewModel(authenticate)
}

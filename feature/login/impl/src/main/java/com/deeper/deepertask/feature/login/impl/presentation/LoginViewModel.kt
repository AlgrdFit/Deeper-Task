package com.deeper.deepertask.feature.login.impl.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deeper.deepertask.feature.login.impl.domain.model.LoginResult
import com.deeper.deepertask.feature.login.impl.domain.usecase.AuthenticateUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class LoginViewModel @Inject constructor(
    private val authenticate: AuthenticateUseCase,
) : ViewModel() {
    private val mutableUiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = mutableUiState.asStateFlow()

    fun onEmailChanged(email: String) {
        mutableUiState.update { current ->
            current.copy(
                email = email,
                isEmailMissing = false,
                status = current.status.resetError(),
            )
        }
    }

    fun onPasswordChanged(password: String) {
        mutableUiState.update { current ->
            current.copy(
                password = password,
                isPasswordMissing = false,
                status = current.status.resetError(),
            )
        }
    }

    fun onSubmit() {
        val current = mutableUiState.value
        if (current.status is LoginStatus.Loading || current.status is LoginStatus.Success) {
            return
        }

        val isEmailMissing = current.email.isBlank()
        val isPasswordMissing = current.password.isBlank()
        if (isEmailMissing || isPasswordMissing) {
            mutableUiState.update {
                it.copy(
                    isEmailMissing = isEmailMissing,
                    isPasswordMissing = isPasswordMissing,
                    status = LoginStatus.Idle,
                )
            }
            return
        }

        mutableUiState.update {
            it.copy(
                isEmailMissing = false,
                isPasswordMissing = false,
                status = LoginStatus.Loading,
            )
        }
        viewModelScope.launch {
            val result = authenticate(
                email = current.email,
                password = current.password,
            )
            mutableUiState.update {
                it.copy(
                    status = when (result) {
                        is LoginResult.Success -> LoginStatus.Success
                        is LoginResult.Failure -> LoginStatus.Error(result.error)
                    },
                )
            }
        }
    }
}

private fun LoginStatus.resetError(): LoginStatus =
    if (this is LoginStatus.Error) LoginStatus.Idle else this

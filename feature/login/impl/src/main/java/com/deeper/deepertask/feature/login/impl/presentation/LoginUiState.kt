package com.deeper.deepertask.feature.login.impl.presentation

import com.deeper.deepertask.feature.login.impl.domain.model.LoginError

internal data class LoginUiState(
    val email: String = "deeperangler@gmail.com",
    val password: String = "Deeper10899",
    val isEmailMissing: Boolean = false,
    val isPasswordMissing: Boolean = false,
    val status: LoginStatus = LoginStatus.Idle,
)

internal sealed interface LoginStatus {
    data object Idle : LoginStatus

    data object Loading : LoginStatus

    data object Success : LoginStatus

    data class Error(val error: LoginError) : LoginStatus
}

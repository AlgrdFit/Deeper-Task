package com.deeper.deepertask.feature.login.impl.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.deeper.deepertask.core.designsystem.theme.DeeperTaskTheme
import com.deeper.deepertask.feature.login.impl.R
import com.deeper.deepertask.feature.login.impl.domain.model.LoginError
import com.deeper.deepertask.feature.scans.api.ScanSummary

@Composable
internal fun LoginScreen(
    onLoginSuccess: (List<ScanSummary>) -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: LoginViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is LoginEvent.NavigateToScans -> onLoginSuccess(event.scans)
            }
        }
    }

    LoginContent(
        uiState = uiState,
        onEmailChanged = viewModel::onEmailChanged,
        onPasswordChanged = viewModel::onPasswordChanged,
        onSubmit = viewModel::onSubmit,
        modifier = modifier,
    )
}

@Composable
internal fun LoginContent(
    uiState: LoginUiState,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isLoading = uiState.status is LoginStatus.Loading
    val isInteractionEnabled = !isLoading && uiState.status !is LoginStatus.Success

    Box(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(vertical = 32.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            OutlinedTextField(
                value = uiState.email,
                onValueChange = onEmailChanged,
                modifier = Modifier.fillMaxWidth(),
                enabled = isInteractionEnabled,
                singleLine = true,
                label = { Text(stringResource(R.string.login_email_label)) },
                isError = uiState.isEmailMissing,
                supportingText = if (uiState.isEmailMissing) {
                    { Text(stringResource(R.string.login_email_required)) }
                } else {
                    null
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next,
                ),
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = uiState.password,
                onValueChange = onPasswordChanged,
                modifier = Modifier.fillMaxWidth(),
                enabled = isInteractionEnabled,
                singleLine = true,
                label = { Text(stringResource(R.string.login_password_label)) },
                visualTransformation = PasswordVisualTransformation(),
                isError = uiState.isPasswordMissing,
                supportingText = if (uiState.isPasswordMissing) {
                    { Text(stringResource(R.string.login_password_required)) }
                } else {
                    null
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(onDone = { onSubmit() }),
            )

            when (val status = uiState.status) {
                is LoginStatus.Error -> {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = stringResource(status.error.messageResource),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                    )
                }

                LoginStatus.Success -> {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = stringResource(R.string.login_success),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }

                LoginStatus.Idle,
                LoginStatus.Loading,
                -> Unit
            }

            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onSubmit,
                modifier = Modifier.fillMaxWidth(),
                enabled = isInteractionEnabled,
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text(stringResource(R.string.login_button))
                }
            }
        }
    }
}

private val LoginError.messageResource: Int
    get() = when (this) {
        LoginError.InvalidCredentials -> R.string.login_error_invalid_credentials
        LoginError.Connectivity -> R.string.login_error_connectivity
        LoginError.Service -> R.string.login_error_service
        LoginError.InvalidResponse -> R.string.login_error_invalid_response
    }

@Preview(name = "Login", showBackground = true)
@Composable
private fun LoginPreview() {
    DeeperTaskTheme(dynamicColor = false) {
        LoginContent(
            uiState = LoginUiState(),
            onEmailChanged = {},
            onPasswordChanged = {},
            onSubmit = {},
        )
    }
}

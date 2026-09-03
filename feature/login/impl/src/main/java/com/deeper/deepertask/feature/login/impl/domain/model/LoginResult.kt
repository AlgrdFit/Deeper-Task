package com.deeper.deepertask.feature.login.impl.domain.model

import com.deeper.deepertask.feature.scans.api.ScanSummary

internal sealed interface LoginResult {
    data class Success(
        val token: String,
        val scans: List<ScanSummary>,
    ) : LoginResult

    data class Failure(val error: LoginError) : LoginResult
}

internal sealed interface LoginError {
    data object InvalidCredentials : LoginError

    data object Connectivity : LoginError

    data object Service : LoginError

    data object InvalidResponse : LoginError
}

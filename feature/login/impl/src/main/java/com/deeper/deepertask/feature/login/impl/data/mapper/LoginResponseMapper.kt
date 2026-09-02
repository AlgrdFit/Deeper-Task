package com.deeper.deepertask.feature.login.impl.data.mapper

import com.deeper.deepertask.feature.login.impl.data.remote.LoginResponseDto
import com.deeper.deepertask.feature.login.impl.domain.model.LoginResult
import com.deeper.deepertask.feature.scans.api.ScanSummary

internal fun LoginResponseDto.toLoginSuccessOrNull(): LoginResult.Success? {
    val login = login ?: return null
    if (login.validated != true) {
        return null
    }

    val token = login.token?.trim()?.takeIf(String::isNotEmpty) ?: return null
    return LoginResult.Success(
        token = token,
        scans = scans.orEmpty().mapNotNull { scan ->
            scan?.let {
                ScanSummary(
                    id = it.id,
                    name = it.name,
                    date = it.date,
                )
            }
        },
    )
}

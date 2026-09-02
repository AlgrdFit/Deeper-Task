package com.deeper.deepertask.feature.login.impl.data.mapper

import com.deeper.deepertask.feature.login.impl.data.remote.LoginResponseDto

internal fun LoginResponseDto.toTokenOrNull(): String? {
    val login = login ?: return null
    if (login.validated != true) {
        return null
    }

    return login.token?.trim()?.takeIf(String::isNotEmpty)
}

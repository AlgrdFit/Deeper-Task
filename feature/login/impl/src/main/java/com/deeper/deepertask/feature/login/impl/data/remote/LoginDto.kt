package com.deeper.deepertask.feature.login.impl.data.remote

internal data class LoginRequestDto(
    val email: String,
    val password: String,
)

internal data class LoginResponseDto(
    val login: LoginDto?,
    val scans: List<ScanDto?>?,
)

internal data class LoginDto(
    val token: String?,
    val validated: Boolean?,
    val validTill: String?,
)

internal data class ScanDto(
    val id: Long?,
    val name: String?,
    val date: String?,
)

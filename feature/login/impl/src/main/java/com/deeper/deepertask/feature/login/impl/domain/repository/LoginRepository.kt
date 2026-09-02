package com.deeper.deepertask.feature.login.impl.domain.repository

import com.deeper.deepertask.feature.login.impl.domain.model.LoginResult

internal interface LoginRepository {
    suspend fun login(email: String, password: String): LoginResult
}

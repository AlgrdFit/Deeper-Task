package com.deeper.deepertask.feature.login.impl.data.repository

import com.deeper.deepertask.core.network.NetworkError
import com.deeper.deepertask.core.network.NetworkResult
import com.deeper.deepertask.core.network.networkCall
import com.deeper.deepertask.feature.login.impl.data.mapper.toLoginSuccessOrNull
import com.deeper.deepertask.feature.login.impl.data.remote.LoginApi
import com.deeper.deepertask.feature.login.impl.data.remote.LoginRequestDto
import com.deeper.deepertask.feature.login.impl.domain.model.LoginError
import com.deeper.deepertask.feature.login.impl.domain.model.LoginResult
import com.deeper.deepertask.feature.login.impl.domain.repository.LoginRepository
import javax.inject.Inject

internal class LoginRepositoryImpl @Inject constructor(
    private val loginApi: LoginApi,
) : LoginRepository {
    override suspend fun login(email: String, password: String): LoginResult =
        when (
            val result = networkCall {
                loginApi.login(
                    LoginRequestDto(
                        email = email,
                        password = password,
                    ),
                )
            }
        ) {
            is NetworkResult.Success -> {
                val login = result.value.toLoginSuccessOrNull()
                login ?: LoginResult.Failure(LoginError.InvalidResponse)
            }

            is NetworkResult.Failure -> LoginResult.Failure(result.error.toLoginError())
        }
}

private fun NetworkError.toLoginError(): LoginError = when (this) {
    is NetworkError.Http -> {
        if (statusCode == 400 || statusCode == 401 || statusCode == 403) {
            LoginError.InvalidCredentials
        } else {
            LoginError.Service
        }
    }

    NetworkError.Timeout,
    NetworkError.Transport,
    -> LoginError.Connectivity

    NetworkError.Serialization,
    NetworkError.Unexpected,
    -> LoginError.Service
}

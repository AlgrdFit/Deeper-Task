package com.deeper.deepertask.feature.login.impl.domain.usecase

import com.deeper.deepertask.feature.login.impl.domain.model.LoginResult
import com.deeper.deepertask.feature.login.impl.domain.repository.LoginRepository
import com.deeper.deepertask.feature.login.api.TokenStore
import javax.inject.Inject

internal class AuthenticateUseCase @Inject constructor(
    private val loginRepository: LoginRepository,
    private val tokenStore: TokenStore,
) {
    suspend operator fun invoke(email: String, password: String): LoginResult {
        val result = loginRepository.login(
            email = email.trim(),
            password = password,
        )
        if (result is LoginResult.Success) {
            tokenStore.save(result.token)
        }
        return result
    }
}

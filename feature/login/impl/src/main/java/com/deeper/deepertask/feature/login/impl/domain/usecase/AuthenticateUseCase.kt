package com.deeper.deepertask.feature.login.impl.domain.usecase

import com.deeper.deepertask.feature.login.impl.domain.model.LoginResult
import com.deeper.deepertask.feature.login.impl.domain.repository.LoginRepository
import javax.inject.Inject

internal class AuthenticateUseCase @Inject constructor(
    private val loginRepository: LoginRepository,
) {
    suspend operator fun invoke(email: String, password: String): LoginResult =
        loginRepository.login(
            email = email.trim(),
            password = password,
        )
}

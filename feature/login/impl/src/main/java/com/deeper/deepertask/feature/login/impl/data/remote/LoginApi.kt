package com.deeper.deepertask.feature.login.impl.data.remote

import retrofit2.http.Body
import retrofit2.http.POST

internal interface LoginApi {
    @POST("login")
    suspend fun login(@Body request: LoginRequestDto): LoginResponseDto
}

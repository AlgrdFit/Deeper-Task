package com.deeper.deepertask.feature.login.impl.di

import com.deeper.deepertask.feature.login.impl.data.remote.LoginApi
import com.deeper.deepertask.feature.login.impl.data.repository.LoginRepositoryImpl
import com.deeper.deepertask.feature.login.impl.domain.repository.LoginRepository
import com.deeper.deepertask.feature.login.api.TokenStore
import com.deeper.deepertask.feature.login.impl.data.session.SharedPreferencesTokenStore
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class LoginBindingsModule {
    @Binds
    @Singleton
    abstract fun bindLoginRepository(implementation: LoginRepositoryImpl): LoginRepository

    @Binds
    @Singleton
    abstract fun bindTokenStore(implementation: SharedPreferencesTokenStore): TokenStore
}

@Module
@InstallIn(SingletonComponent::class)
internal object LoginProvidersModule {
    @Provides
    @Singleton
    fun provideLoginApi(retrofit: Retrofit): LoginApi = retrofit.create(LoginApi::class.java)
}

package com.deeper.deepertask.feature.scans.impl.di

import com.deeper.deepertask.feature.scans.impl.data.repository.ScansRepositoryImpl
import com.deeper.deepertask.feature.scans.impl.domain.repository.ScansRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class ScansBindingsModule {
    @Binds
    @Singleton
    abstract fun bindScansRepository(implementation: ScansRepositoryImpl): ScansRepository
}

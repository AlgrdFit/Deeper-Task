package com.deeper.deepertask.feature.bathymetry.impl.di

import com.deeper.deepertask.feature.bathymetry.impl.data.remote.BathymetryApi
import com.deeper.deepertask.feature.bathymetry.impl.data.repository.BathymetryRepositoryImpl
import com.deeper.deepertask.feature.bathymetry.impl.domain.repository.BathymetryRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import retrofit2.Retrofit
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
internal annotation class BathymetryDefaultDispatcher

@Module
@InstallIn(SingletonComponent::class)
internal abstract class BathymetryBindingsModule {
    @Binds
    @Singleton
    abstract fun bindBathymetryRepository(
        implementation: BathymetryRepositoryImpl,
    ): BathymetryRepository
}

@Module
@InstallIn(SingletonComponent::class)
internal object BathymetryProvidersModule {
    @Provides
    @Singleton
    fun provideBathymetryApi(retrofit: Retrofit): BathymetryApi =
        retrofit.create(BathymetryApi::class.java)

    @Provides
    @BathymetryDefaultDispatcher
    fun provideDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default
}

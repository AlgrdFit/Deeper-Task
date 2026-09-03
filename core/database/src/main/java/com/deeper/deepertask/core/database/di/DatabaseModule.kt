package com.deeper.deepertask.core.database.di

import android.content.Context
import androidx.room.Room
import com.deeper.deepertask.core.database.DeeperTaskDatabase
import com.deeper.deepertask.core.database.bathymetry.BathymetryDao
import com.deeper.deepertask.core.database.scans.ScanDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
    ): DeeperTaskDatabase = Room.databaseBuilder(
        context,
        DeeperTaskDatabase::class.java,
        DATABASE_NAME,
    ).build()

    @Provides
    fun provideScanDao(database: DeeperTaskDatabase): ScanDao = database.scanDao()

    @Provides
    fun provideBathymetryDao(database: DeeperTaskDatabase): BathymetryDao =
        database.bathymetryDao()

    private const val DATABASE_NAME = "deeper_task.db"
}

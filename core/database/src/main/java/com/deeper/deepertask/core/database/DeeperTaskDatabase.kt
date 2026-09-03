package com.deeper.deepertask.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.deeper.deepertask.core.database.bathymetry.BathymetryCacheEntity
import com.deeper.deepertask.core.database.bathymetry.BathymetryDao
import com.deeper.deepertask.core.database.scans.ScanDao
import com.deeper.deepertask.core.database.scans.ScanEntity

@Database(
    entities = [
        ScanEntity::class,
        BathymetryCacheEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
internal abstract class DeeperTaskDatabase : RoomDatabase() {
    abstract fun scanDao(): ScanDao

    abstract fun bathymetryDao(): BathymetryDao
}

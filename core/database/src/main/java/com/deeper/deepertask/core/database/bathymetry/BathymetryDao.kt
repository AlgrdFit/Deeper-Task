package com.deeper.deepertask.core.database.bathymetry

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface BathymetryDao {
    @Query("SELECT * FROM bathymetry WHERE scan_id = :scanId")
    suspend fun get(scanId: Long): BathymetryCacheEntity?

    @Query("SELECT * FROM bathymetry WHERE scan_id = :scanId")
    fun observe(scanId: Long): Flow<BathymetryCacheEntity?>

    @Upsert
    suspend fun upsert(entity: BathymetryCacheEntity)
}

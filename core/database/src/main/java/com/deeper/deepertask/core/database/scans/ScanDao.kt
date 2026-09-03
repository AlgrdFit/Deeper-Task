package com.deeper.deepertask.core.database.scans

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface ScanDao {
    @Query("SELECT * FROM scans ORDER BY position ASC")
    fun observeAll(): Flow<List<ScanEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(scans: List<ScanEntity>)

    @Query("DELETE FROM scans")
    suspend fun deleteAll()

    @Transaction
    suspend fun replaceAll(scans: List<ScanEntity>) {
        deleteAll()
        if (scans.isNotEmpty()) {
            insertAll(scans)
        }
    }
}

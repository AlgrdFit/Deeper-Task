package com.deeper.deepertask.core.database.scans

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface ScanDao {
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

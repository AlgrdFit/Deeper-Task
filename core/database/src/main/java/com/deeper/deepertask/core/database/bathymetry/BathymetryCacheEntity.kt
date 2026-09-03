package com.deeper.deepertask.core.database.bathymetry

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bathymetry")
data class BathymetryCacheEntity(
    @PrimaryKey
    @ColumnInfo(name = "scan_id")
    val scanId: Long,
    @ColumnInfo(name = "payload")
    val payload: String,
)

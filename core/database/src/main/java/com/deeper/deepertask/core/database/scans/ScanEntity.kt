package com.deeper.deepertask.core.database.scans

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scans")
data class ScanEntity(
    @PrimaryKey
    @ColumnInfo(name = "position")
    val position: Int,
    @ColumnInfo(name = "scan_id")
    val id: Long?,
    @ColumnInfo(name = "name")
    val name: String?,
    @ColumnInfo(name = "date")
    val date: String?,
)

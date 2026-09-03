package com.deeper.deepertask.feature.scans.impl.data.local

import com.deeper.deepertask.core.database.scans.ScanEntity
import com.deeper.deepertask.feature.scans.api.ScanSummary

internal fun ScanSummary.toEntity(position: Int): ScanEntity = ScanEntity(
    position = position,
    id = id,
    name = name,
    date = date,
)

internal fun ScanEntity.toScanSummary(): ScanSummary = ScanSummary(
    id = id,
    name = name,
    date = date,
)

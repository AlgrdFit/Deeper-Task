package com.deeper.deepertask.feature.scans.impl.domain.repository

import com.deeper.deepertask.feature.scans.api.ScanSummary

internal interface ScansRepository {
    suspend fun replaceScans(scans: List<ScanSummary>)
}

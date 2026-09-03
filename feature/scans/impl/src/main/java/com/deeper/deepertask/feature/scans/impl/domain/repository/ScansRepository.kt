package com.deeper.deepertask.feature.scans.impl.domain.repository

import com.deeper.deepertask.feature.scans.api.ScanSummary
import kotlinx.coroutines.flow.Flow

internal interface ScansRepository {
    fun observeScans(): Flow<List<ScanSummary>>

    suspend fun replaceScans(scans: List<ScanSummary>)
}

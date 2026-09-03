package com.deeper.deepertask.feature.scans.impl.data.repository

import com.deeper.deepertask.core.database.scans.ScanDao
import com.deeper.deepertask.feature.scans.api.ScanSummary
import com.deeper.deepertask.feature.scans.impl.data.local.toEntity
import com.deeper.deepertask.feature.scans.impl.data.local.toScanSummary
import com.deeper.deepertask.feature.scans.impl.domain.repository.ScansRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

internal class ScansRepositoryImpl @Inject constructor(
    private val scanDao: ScanDao,
) : ScansRepository {
    override fun observeScans(): Flow<List<ScanSummary>> = scanDao
        .observeAll()
        .map { entities -> entities.map { entity -> entity.toScanSummary() } }

    override suspend fun replaceScans(scans: List<ScanSummary>) {
        scanDao.replaceAll(
            scans.mapIndexed { position, scan -> scan.toEntity(position) },
        )
    }
}

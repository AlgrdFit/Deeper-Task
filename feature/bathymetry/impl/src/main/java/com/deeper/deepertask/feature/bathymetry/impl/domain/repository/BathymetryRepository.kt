package com.deeper.deepertask.feature.bathymetry.impl.domain.repository

import com.deeper.deepertask.feature.bathymetry.impl.domain.model.BathymetryResult

internal interface BathymetryRepository {
    suspend fun getBathymetry(scanId: Long): BathymetryResult
}

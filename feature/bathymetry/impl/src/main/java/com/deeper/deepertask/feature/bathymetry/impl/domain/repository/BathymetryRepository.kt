package com.deeper.deepertask.feature.bathymetry.impl.domain.repository

import com.deeper.deepertask.feature.bathymetry.impl.domain.model.BathymetryResult
import kotlinx.coroutines.flow.Flow

internal interface BathymetryRepository {
    fun getBathymetry(scanId: Long): Flow<BathymetryResult>
}

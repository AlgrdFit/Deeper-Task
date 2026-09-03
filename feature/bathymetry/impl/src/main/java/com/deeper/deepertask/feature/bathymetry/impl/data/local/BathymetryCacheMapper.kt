package com.deeper.deepertask.feature.bathymetry.impl.data.local

import com.deeper.deepertask.core.database.bathymetry.BathymetryCacheEntity
import com.deeper.deepertask.feature.bathymetry.impl.data.mapper.toBathymetryDataOrNull
import com.deeper.deepertask.feature.bathymetry.impl.data.remote.GeoDataResponseDto
import com.deeper.deepertask.feature.bathymetry.impl.domain.model.BathymetryData
import com.google.gson.Gson
import javax.inject.Inject

internal class BathymetryCacheMapper @Inject constructor(
    private val gson: Gson,
) {
    fun toEntity(
        scanId: Long,
        response: GeoDataResponseDto,
    ): BathymetryCacheEntity = BathymetryCacheEntity(
        scanId = scanId,
        payload = gson.toJson(response),
    )

    fun toDomainOrNull(entity: BathymetryCacheEntity): BathymetryData? {
        val response = runCatching {
            gson.fromJson(entity.payload, GeoDataResponseDto::class.java)
        }.getOrNull() ?: return null
        return response.toBathymetryDataOrNull()
    }
}

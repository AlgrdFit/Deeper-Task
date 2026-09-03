package com.deeper.deepertask.feature.bathymetry.impl.data.local

import com.deeper.deepertask.core.database.bathymetry.BathymetryCacheEntity
import com.deeper.deepertask.feature.bathymetry.impl.data.mapper.toBathymetryDataOrNull
import com.deeper.deepertask.feature.bathymetry.impl.data.remote.BathymetryDto
import com.deeper.deepertask.feature.bathymetry.impl.data.remote.BathymetryFeatureDto
import com.deeper.deepertask.feature.bathymetry.impl.data.remote.BathymetryGeometryDto
import com.deeper.deepertask.feature.bathymetry.impl.data.remote.BathymetryPropertiesDto
import com.deeper.deepertask.feature.bathymetry.impl.data.remote.GeoDataResponseDto
import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BathymetryCacheMapperTest {
    private val scanId = 42L
    private val response = GeoDataResponseDto(
        bathymetry = BathymetryDto(
            type = "FeatureCollection",
            bbox = null,
            features = listOf(
                BathymetryFeatureDto(
                    type = "Feature",
                    properties = BathymetryPropertiesDto(depth = 1.0, id = "polygon"),
                    geometry = BathymetryGeometryDto(
                        type = "Polygon",
                        bbox = null,
                        coordinates = listOf(
                            listOf(
                                listOf(20.0, 10.0, 1.0),
                                listOf(21.0, 10.0, 1.0),
                                listOf(20.0, 11.0, 1.0),
                            ),
                        ),
                    ),
                ),
            ),
        ),
    )

    @Test
    fun `serializes a response and restores equivalent domain data`() {
        // Arrange
        val fixture = fixture()

        // Act
        val entity = fixture.toEntity(scanId, response)
        val result = fixture.toDomainOrNull(entity)

        // Assert
        assertEquals(scanId, entity.scanId)
        assertEquals(response.toBathymetryDataOrNull(), result)
    }

    @Test
    fun `returns null for a corrupt cached payload`() {
        // Arrange
        val entity = BathymetryCacheEntity(scanId = scanId, payload = "not-json")
        val fixture = fixture()

        // Act
        val result = fixture.toDomainOrNull(entity)

        // Assert
        assertNull(result)
    }

    private fun fixture(): BathymetryCacheMapper = BathymetryCacheMapper(Gson())
}

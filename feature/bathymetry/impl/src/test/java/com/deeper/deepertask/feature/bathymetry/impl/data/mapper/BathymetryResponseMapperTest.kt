package com.deeper.deepertask.feature.bathymetry.impl.data.mapper

import com.deeper.deepertask.feature.bathymetry.impl.data.remote.BathymetryDto
import com.deeper.deepertask.feature.bathymetry.impl.data.remote.BathymetryFeatureDto
import com.deeper.deepertask.feature.bathymetry.impl.data.remote.BathymetryGeometryDto
import com.deeper.deepertask.feature.bathymetry.impl.data.remote.BathymetryPropertiesDto
import com.deeper.deepertask.feature.bathymetry.impl.data.remote.GeoDataResponseDto
import com.deeper.deepertask.feature.bathymetry.impl.domain.model.GeoBounds
import com.deeper.deepertask.feature.bathymetry.impl.domain.model.GeoPoint
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BathymetryResponseMapperTest {
    @Test
    fun `maps longitude latitude coordinates holes and API bbox order`() {
        // Arrange
        val outerRing = listOf(
            coordinate(longitude = -72.02, latitude = 43.43),
            coordinate(longitude = -72.01, latitude = 43.43),
            coordinate(longitude = -72.01, latitude = 43.44),
            coordinate(longitude = -72.02, latitude = 43.43),
        )
        val hole = listOf(
            coordinate(longitude = -72.018, latitude = 43.432),
            coordinate(longitude = -72.016, latitude = 43.432),
            coordinate(longitude = -72.017, latitude = 43.434),
        )
        val response = response(
            bbox = listOf(43.42, -72.03, 43.45, -72.00),
            features = listOf(feature(depth = 1.0, rings = listOf(outerRing, hole))),
        )

        // Act
        val result = response.toBathymetryDataOrNull()

        // Assert
        val polygon = requireNotNull(result).polygons.single()
        assertEquals(GeoPoint(latitude = 43.43, longitude = -72.02), polygon.outerRing.first())
        assertEquals(3, polygon.outerRing.size)
        assertEquals(GeoPoint(latitude = 43.432, longitude = -72.018), polygon.holes.single().first())
        assertEquals(
            GeoBounds(
                southWest = GeoPoint(latitude = 43.42, longitude = -72.03),
                northEast = GeoPoint(latitude = 43.45, longitude = -72.00),
            ),
            result.bounds,
        )
    }

    @Test
    fun `skips malformed features and calculates bounds from valid polygon`() {
        // Arrange
        val invalidFeature = feature(
            depth = 1.0,
            rings = listOf(
                listOf(
                    coordinate(longitude = 10.0, latitude = 200.0),
                    coordinate(longitude = 11.0, latitude = 20.0),
                    coordinate(longitude = 12.0, latitude = 21.0),
                ),
            ),
        )
        val validFeature = feature(
            depth = 2.0,
            rings = listOf(
                listOf(
                    coordinate(longitude = 20.0, latitude = 10.0),
                    coordinate(longitude = 22.0, latitude = 11.0),
                    coordinate(longitude = 21.0, latitude = 13.0),
                ),
            ),
        )
        val response = response(
            bbox = listOf(200.0, 200.0, 300.0, 300.0),
            features = listOf(invalidFeature, validFeature),
        )

        // Act
        val result = response.toBathymetryDataOrNull()

        // Assert
        assertEquals(1, requireNotNull(result).polygons.size)
        assertEquals(
            GeoBounds(
                southWest = GeoPoint(latitude = 10.0, longitude = 20.0),
                northEast = GeoPoint(latitude = 13.0, longitude = 22.0),
            ),
            result.bounds,
        )
    }

    @Test
    fun `returns null when response has no valid polygons`() {
        // Arrange
        val response = response(
            features = listOf(
                feature(depth = null, rings = listOf(validRing())),
                feature(depth = 1.0, rings = emptyList()),
            ),
        )

        // Act
        val result = response.toBathymetryDataOrNull()

        // Assert
        assertNull(result)
    }

    @Test
    fun `creates five equal depth bands and includes maximum in final band`() {
        // Arrange
        val response = response(
            features = listOf(5.0, 0.0, 4.0, 1.0, 3.0, 2.0).map { depth ->
                feature(depth = depth, rings = listOf(validRing(longitudeOffset = depth)))
            },
        )

        // Act
        val result = requireNotNull(response.toBathymetryDataOrNull())

        // Assert
        assertEquals(5, result.depthBands.size)
        assertEquals(listOf(0.0, 1.0, 2.0, 3.0, 4.0, 5.0), result.polygons.map { it.depth })
        assertEquals(listOf(0, 1, 2, 3, 4, 4), result.polygons.map { it.depthBandIndex })
        assertEquals(0.0, result.depthBands.first().minimumDepth, 0.0)
        assertEquals(5.0, result.depthBands.last().maximumDepth, 0.0)
        assertTrue(result.depthBands.last().includesMaximum)
    }

    @Test
    fun `uses one depth band when all polygon depths match`() {
        // Arrange
        val response = response(
            features = listOf(
                feature(depth = 2.5, rings = listOf(validRing())),
                feature(depth = 2.5, rings = listOf(validRing(longitudeOffset = 1.0))),
            ),
        )

        // Act
        val result = requireNotNull(response.toBathymetryDataOrNull())

        // Assert
        assertEquals(1, result.depthBands.size)
        assertEquals(listOf(0, 0), result.polygons.map { it.depthBandIndex })
        assertEquals(2.5, result.depthBands.single().minimumDepth, 0.0)
        assertEquals(2.5, result.depthBands.single().maximumDepth, 0.0)
    }

    private fun response(
        bbox: List<Double?>? = null,
        features: List<BathymetryFeatureDto?>,
    ): GeoDataResponseDto = GeoDataResponseDto(
        bathymetry = BathymetryDto(
            type = "FeatureCollection",
            bbox = bbox,
            features = features,
        ),
    )

    private fun feature(
        depth: Double?,
        rings: List<List<List<Double?>?>?>,
    ): BathymetryFeatureDto = BathymetryFeatureDto(
        type = "Feature",
        properties = BathymetryPropertiesDto(depth = depth, id = "feature-$depth"),
        geometry = BathymetryGeometryDto(
            type = "Polygon",
            bbox = null,
            coordinates = rings,
        ),
    )

    private fun validRing(longitudeOffset: Double = 0.0): List<List<Double?>> = listOf(
        coordinate(longitude = 20.0 + longitudeOffset, latitude = 10.0),
        coordinate(longitude = 21.0 + longitudeOffset, latitude = 10.0),
        coordinate(longitude = 20.0 + longitudeOffset, latitude = 11.0),
    )

    private fun coordinate(longitude: Double, latitude: Double): List<Double?> =
        listOf(longitude, latitude, 1.0)
}

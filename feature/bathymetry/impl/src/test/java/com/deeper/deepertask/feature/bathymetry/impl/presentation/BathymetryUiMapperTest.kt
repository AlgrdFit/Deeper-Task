package com.deeper.deepertask.feature.bathymetry.impl.presentation

import androidx.compose.ui.graphics.Color
import com.deeper.deepertask.feature.bathymetry.impl.domain.model.BathymetryData
import com.deeper.deepertask.feature.bathymetry.impl.domain.model.BathymetryPolygon
import com.deeper.deepertask.feature.bathymetry.impl.domain.model.DepthBand
import com.deeper.deepertask.feature.bathymetry.impl.domain.model.GeoBounds
import com.deeper.deepertask.feature.bathymetry.impl.domain.model.GeoPoint
import com.google.android.gms.maps.model.LatLng
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class BathymetryUiMapperTest {
    @Test
    fun `maps coordinates depth color legend and camera bounds`() {
        // Arrange
        val data = BathymetryData(
            polygons = listOf(
                BathymetryPolygon(
                    depth = 1.5,
                    outerRing = listOf(
                        GeoPoint(latitude = 10.0, longitude = 20.0),
                        GeoPoint(latitude = 10.0, longitude = 21.0),
                        GeoPoint(latitude = 11.0, longitude = 20.0),
                    ),
                    holes = listOf(
                        listOf(
                            GeoPoint(latitude = 10.2, longitude = 20.2),
                            GeoPoint(latitude = 10.2, longitude = 20.3),
                            GeoPoint(latitude = 10.3, longitude = 20.2),
                        ),
                    ),
                    depthBandIndex = 1,
                ),
            ),
            bounds = GeoBounds(
                southWest = GeoPoint(latitude = 10.0, longitude = 20.0),
                northEast = GeoPoint(latitude = 11.0, longitude = 21.0),
            ),
            depthBands = listOf(
                DepthBand(
                    index = 1,
                    minimumDepth = 1.0,
                    maximumDepth = 2.0,
                    includesMaximum = true,
                ),
            ),
        )

        // Act
        val result = BathymetryUiMapper()(data)

        // Assert
        assertEquals(LatLng(10.0, 20.0), result.polygons.single().points.first())
        assertEquals(LatLng(10.2, 20.2), result.polygons.single().holes.single().first())
        assertEquals(Color(0xFF4FC3F7), result.polygons.single().color)
        assertEquals(DepthLegendItemUi("1-2 m", Color(0xFF4FC3F7)), result.legend.single())
        assertEquals(LatLng(10.5, 20.5), result.camera.center)
        assertFalse(result.camera.isDegenerate)
    }
}

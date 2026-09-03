package com.deeper.deepertask.feature.bathymetry.impl.presentation

import androidx.compose.ui.graphics.Color
import com.deeper.deepertask.feature.bathymetry.impl.domain.model.BathymetryData
import com.deeper.deepertask.feature.bathymetry.impl.domain.model.DepthBand
import com.deeper.deepertask.feature.bathymetry.impl.domain.model.GeoPoint
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import javax.inject.Inject

internal class BathymetryUiMapper @Inject constructor() {
    operator fun invoke(data: BathymetryData): BathymetryMapUi {
        val bounds = LatLngBounds(
            data.bounds.southWest.toLatLng(),
            data.bounds.northEast.toLatLng(),
        )
        return BathymetryMapUi(
            polygons = data.polygons.map { polygon ->
                BathymetryPolygonUi(
                    points = polygon.outerRing.map(GeoPoint::toLatLng),
                    holes = polygon.holes.map { ring -> ring.map(GeoPoint::toLatLng) },
                    color = DEPTH_COLORS[polygon.depthBandIndex],
                )
            },
            camera = BathymetryCameraUi(
                bounds = bounds,
                center = bounds.center,
                isDegenerate = data.bounds.southWest == data.bounds.northEast,
            ),
            legend = data.depthBands.map { band ->
                DepthLegendItemUi(
                    label = band.toLabel(),
                    color = DEPTH_COLORS[band.index],
                )
            },
        )
    }
}

private fun GeoPoint.toLatLng(): LatLng = LatLng(latitude, longitude)

private fun DepthBand.toLabel(): String {
    val formatter = DecimalFormat("0.#", DecimalFormatSymbols(Locale.ROOT))
    return if (minimumDepth == maximumDepth) {
        "${formatter.format(minimumDepth)} m"
    } else {
        "${formatter.format(minimumDepth)}-${formatter.format(maximumDepth)} m"
    }
}

private val DEPTH_COLORS = listOf(
    Color(0xFFB3E5FC),
    Color(0xFF4FC3F7),
    Color(0xFF039BE5),
    Color(0xFF0277BD),
    Color(0xFF01579B),
)

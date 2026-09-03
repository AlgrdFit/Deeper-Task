package com.deeper.deepertask.feature.bathymetry.impl.domain.model

internal data class BathymetryData(
    val polygons: List<BathymetryPolygon>,
    val bounds: GeoBounds,
    val depthBands: List<DepthBand>,
)

internal data class BathymetryPolygon(
    val depth: Double,
    val outerRing: List<GeoPoint>,
    val holes: List<List<GeoPoint>>,
    val depthBandIndex: Int,
)

internal data class GeoPoint(
    val latitude: Double,
    val longitude: Double,
)

internal data class GeoBounds(
    val southWest: GeoPoint,
    val northEast: GeoPoint,
)

internal data class DepthBand(
    val index: Int,
    val minimumDepth: Double,
    val maximumDepth: Double,
    val includesMaximum: Boolean,
)

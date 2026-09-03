package com.deeper.deepertask.feature.bathymetry.impl.data.remote

internal data class GeoDataResponseDto(
    val bathymetry: BathymetryDto?,
)

internal data class BathymetryDto(
    val type: String?,
    val bbox: List<Double?>?,
    val features: List<BathymetryFeatureDto?>?,
)

internal data class BathymetryFeatureDto(
    val type: String?,
    val properties: BathymetryPropertiesDto?,
    val geometry: BathymetryGeometryDto?,
)

internal data class BathymetryPropertiesDto(
    val depth: Double?,
    val id: String?,
)

internal data class BathymetryGeometryDto(
    val type: String?,
    val bbox: List<Double?>?,
    val coordinates: List<List<List<Double?>?>?>?,
)

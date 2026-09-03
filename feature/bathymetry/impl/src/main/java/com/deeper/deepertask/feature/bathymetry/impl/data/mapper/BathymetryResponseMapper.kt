package com.deeper.deepertask.feature.bathymetry.impl.data.mapper

import com.deeper.deepertask.feature.bathymetry.impl.data.remote.BathymetryFeatureDto
import com.deeper.deepertask.feature.bathymetry.impl.data.remote.GeoDataResponseDto
import com.deeper.deepertask.feature.bathymetry.impl.domain.model.BathymetryData
import com.deeper.deepertask.feature.bathymetry.impl.domain.model.BathymetryPolygon
import com.deeper.deepertask.feature.bathymetry.impl.domain.model.DepthBand
import com.deeper.deepertask.feature.bathymetry.impl.domain.model.GeoBounds
import com.deeper.deepertask.feature.bathymetry.impl.domain.model.GeoPoint
import kotlin.math.floor

private const val FEATURE_COLLECTION_TYPE = "FeatureCollection"
private const val FEATURE_TYPE = "Feature"
private const val POLYGON_TYPE = "Polygon"
private const val DEPTH_BAND_COUNT = 5

internal fun GeoDataResponseDto.toBathymetryDataOrNull(): BathymetryData? {
    val bathymetry = bathymetry?.takeIf { it.type == FEATURE_COLLECTION_TYPE } ?: return null
    val mappedPolygons = bathymetry.features
        .orEmpty()
        .mapNotNull { feature -> feature?.toPolygonOrNull() }
        .sortedBy(BathymetryPolygon::depth)
    if (mappedPolygons.isEmpty()) {
        return null
    }

    val depthBands = mappedPolygons.createDepthBands()
    val polygons = mappedPolygons.map { polygon ->
        polygon.copy(
            depthBandIndex = depthBands.indexFor(polygon.depth),
        )
    }
    val bounds = bathymetry.bbox.toBoundsOrNull() ?: polygons.calculateBounds()

    return BathymetryData(
        polygons = polygons,
        bounds = bounds,
        depthBands = depthBands,
    )
}

private fun BathymetryFeatureDto.toPolygonOrNull(): BathymetryPolygon? {
    if (type != FEATURE_TYPE || geometry?.type != POLYGON_TYPE) {
        return null
    }
    val depth = properties?.depth?.takeIf { it.isFinite() && it >= 0.0 } ?: return null
    val rings = geometry.coordinates.orEmpty()
    val outerRing = rings.firstOrNull()?.toRingOrNull() ?: return null
    val holes = rings.drop(1).mapNotNull { ring -> ring.toRingOrNull() }

    return BathymetryPolygon(
        depth = depth,
        outerRing = outerRing,
        holes = holes,
        depthBandIndex = 0,
    )
}

private fun List<List<Double?>?>?.toRingOrNull(): List<GeoPoint>? {
    val coordinates = this ?: return null
    val points = coordinates.map { coordinate ->
        coordinate.toPointOrNull() ?: return null
    }
    val normalizedPoints = if (points.size > 3 && points.first() == points.last()) {
        points.dropLast(1)
    } else {
        points
    }
    return normalizedPoints.takeIf { ring ->
        ring.size >= 3 && ring.distinct().size >= 3
    }
}

private fun List<Double?>?.toPointOrNull(): GeoPoint? {
    val coordinate = this ?: return null
    val longitude = coordinate.getOrNull(0)?.takeIf(Double::isFinite) ?: return null
    val latitude = coordinate.getOrNull(1)?.takeIf(Double::isFinite) ?: return null
    if (longitude !in -180.0..180.0 || latitude !in -90.0..90.0) {
        return null
    }
    return GeoPoint(
        latitude = latitude,
        longitude = longitude,
    )
}

private fun List<Double?>?.toBoundsOrNull(): GeoBounds? {
    if (this == null || size != 4) {
        return null
    }
    val south = get(0)?.takeIf(Double::isFinite) ?: return null
    val west = get(1)?.takeIf(Double::isFinite) ?: return null
    val north = get(2)?.takeIf(Double::isFinite) ?: return null
    val east = get(3)?.takeIf(Double::isFinite) ?: return null
    if (
        south !in -90.0..90.0 || north !in -90.0..90.0 ||
        west !in -180.0..180.0 || east !in -180.0..180.0 ||
        south > north || west > east
    ) {
        return null
    }
    return GeoBounds(
        southWest = GeoPoint(latitude = south, longitude = west),
        northEast = GeoPoint(latitude = north, longitude = east),
    )
}

private fun List<BathymetryPolygon>.calculateBounds(): GeoBounds {
    val points = flatMap { polygon ->
        buildList {
            addAll(polygon.outerRing)
            polygon.holes.forEach(::addAll)
        }
    }
    return GeoBounds(
        southWest = GeoPoint(
            latitude = points.minOf(GeoPoint::latitude),
            longitude = points.minOf(GeoPoint::longitude),
        ),
        northEast = GeoPoint(
            latitude = points.maxOf(GeoPoint::latitude),
            longitude = points.maxOf(GeoPoint::longitude),
        ),
    )
}

private fun List<BathymetryPolygon>.createDepthBands(): List<DepthBand> {
    val minimumDepth = minOf(BathymetryPolygon::depth)
    val maximumDepth = maxOf(BathymetryPolygon::depth)
    if (minimumDepth == maximumDepth) {
        return listOf(
            DepthBand(
                index = 0,
                minimumDepth = minimumDepth,
                maximumDepth = maximumDepth,
                includesMaximum = true,
            ),
        )
    }

    val width = (maximumDepth - minimumDepth) / DEPTH_BAND_COUNT
    return List(DEPTH_BAND_COUNT) { index ->
        DepthBand(
            index = index,
            minimumDepth = minimumDepth + width * index,
            maximumDepth = if (index == DEPTH_BAND_COUNT - 1) {
                maximumDepth
            } else {
                minimumDepth + width * (index + 1)
            },
            includesMaximum = index == DEPTH_BAND_COUNT - 1,
        )
    }
}

private fun List<DepthBand>.indexFor(depth: Double): Int {
    if (size == 1) {
        return first().index
    }
    val minimumDepth = first().minimumDepth
    val maximumDepth = last().maximumDepth
    val width = (maximumDepth - minimumDepth) / size
    return floor((depth - minimumDepth) / width)
        .toInt()
        .coerceIn(0, lastIndex)
}

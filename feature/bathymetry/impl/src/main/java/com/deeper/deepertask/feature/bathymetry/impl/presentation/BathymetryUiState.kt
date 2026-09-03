package com.deeper.deepertask.feature.bathymetry.impl.presentation

import androidx.compose.ui.graphics.Color
import com.deeper.deepertask.feature.bathymetry.impl.domain.model.BathymetryError
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds

internal sealed interface BathymetryUiState {
    data object Loading : BathymetryUiState

    data class Content(val map: BathymetryMapUi) : BathymetryUiState

    data object AuthenticationRequired : BathymetryUiState

    data class Error(val error: BathymetryError) : BathymetryUiState
}

internal data class BathymetryMapUi(
    val polygons: List<BathymetryPolygonUi>,
    val camera: BathymetryCameraUi,
    val legend: List<DepthLegendItemUi>,
)

internal data class BathymetryPolygonUi(
    val points: List<LatLng>,
    val holes: List<List<LatLng>>,
    val color: Color,
)

internal data class BathymetryCameraUi(
    val bounds: LatLngBounds,
    val center: LatLng,
    val isDegenerate: Boolean,
)

internal data class DepthLegendItemUi(
    val label: String,
    val color: Color,
)

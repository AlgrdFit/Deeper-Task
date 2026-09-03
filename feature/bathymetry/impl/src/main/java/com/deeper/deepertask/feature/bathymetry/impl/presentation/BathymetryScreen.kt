package com.deeper.deepertask.feature.bathymetry.impl.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.deeper.deepertask.core.designsystem.theme.DeeperTaskTheme
import com.deeper.deepertask.feature.bathymetry.impl.R
import com.deeper.deepertask.feature.bathymetry.impl.domain.model.BathymetryError
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Polygon
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
internal fun BathymetryScreen(
    scanId: Long,
    onBack: () -> Unit,
    onAuthenticationRequired: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: BathymetryViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(scanId) {
        viewModel.load(scanId)
    }
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                BathymetryEvent.NavigateToLogin -> onAuthenticationRequired()
            }
        }
    }

    BathymetryContent(
        uiState = uiState,
        onBack = onBack,
        onRetry = viewModel::retry,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun BathymetryContent(
    uiState: BathymetryUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.bathymetry_title)) },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text(stringResource(R.string.bathymetry_back))
                    }
                },
            )
        },
    ) { contentPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
            contentAlignment = Alignment.Center,
        ) {
            when (uiState) {
                BathymetryUiState.Loading -> CircularProgressIndicator()
                is BathymetryUiState.Content -> BathymetryMap(uiState.map)
                BathymetryUiState.AuthenticationRequired -> Text(
                    text = stringResource(R.string.bathymetry_authentication_required),
                    style = MaterialTheme.typography.bodyLarge,
                )

                is BathymetryUiState.Error -> BathymetryErrorContent(
                    error = uiState.error,
                    onRetry = onRetry,
                )
            }
        }
    }
}

@Composable
private fun BathymetryMap(map: BathymetryMapUi) {
    val cameraPositionState = rememberCameraPositionState()
    val cameraPadding = with(LocalDensity.current) { 48.dp.roundToPx() }
    var mapLoaded by remember(map) { mutableStateOf(false) }

    LaunchedEffect(map, mapLoaded) {
        if (mapLoaded) {
            val update = if (map.camera.isDegenerate) {
                CameraUpdateFactory.newLatLngZoom(map.camera.center, 18f)
            } else {
                CameraUpdateFactory.newLatLngBounds(map.camera.bounds, cameraPadding)
            }
            cameraPositionState.animate(update)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = remember { MapProperties(mapType = MapType.NORMAL) },
            uiSettings = remember {
                MapUiSettings(
                    mapToolbarEnabled = false,
                    myLocationButtonEnabled = false,
                )
            },
            onMapLoaded = { mapLoaded = true },
        ) {
            map.polygons.forEach { polygon ->
                Polygon(
                    points = polygon.points,
                    holes = polygon.holes,
                    fillColor = polygon.color.copy(alpha = 0.65f),
                    strokeColor = polygon.color,
                    strokeWidth = 2f,
                )
            }
        }

        DepthLegend(
            items = map.legend,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
        )
    }
}

@Composable
private fun DepthLegend(
    items: List<DepthLegendItemUi>,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 4.dp,
        shadowElevation = 4.dp,
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = stringResource(R.string.bathymetry_depth_legend),
                style = MaterialTheme.typography.labelLarge,
            )
            items.forEach { item ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(MaterialTheme.shapes.extraSmall)
                            .background(item.color),
                    )
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
    }
}

@Composable
private fun BathymetryErrorContent(
    error: BathymetryError,
    onRetry: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = stringResource(error.messageResource),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error,
        )
        Button(onClick = onRetry) {
            Text(stringResource(R.string.bathymetry_retry))
        }
    }
}

private val BathymetryError.messageResource: Int
    get() = when (this) {
        BathymetryError.AuthenticationRequired -> R.string.bathymetry_authentication_required
        BathymetryError.Connectivity -> R.string.bathymetry_error_connectivity
        BathymetryError.Service -> R.string.bathymetry_error_service
        BathymetryError.InvalidData -> R.string.bathymetry_error_invalid_data
    }

@Preview(name = "Bathymetry error", showBackground = true)
@Composable
private fun BathymetryErrorPreview() {
    DeeperTaskTheme(dynamicColor = false) {
        BathymetryContent(
            uiState = BathymetryUiState.Error(BathymetryError.Connectivity),
            onBack = {},
            onRetry = {},
        )
    }
}

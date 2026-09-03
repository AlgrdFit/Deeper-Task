package com.deeper.deepertask.feature.scans.impl.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.deeper.deepertask.core.designsystem.theme.DeeperTaskTheme
import com.deeper.deepertask.feature.scans.api.ScanSummary
import com.deeper.deepertask.feature.scans.impl.R

@Composable
internal fun ScansScreen(
    scans: List<ScanSummary>,
    onScanSelected: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: ScansViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(scans) {
        viewModel.load(scans)
    }

    ScansContent(
        uiState = uiState,
        onScanSelected = onScanSelected,
        onRetry = viewModel::retry,
        modifier = modifier,
    )
}

@Composable
internal fun ScansContent(
    uiState: ScansUiState,
    onScanSelected: (Long) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 32.dp),
    ) {
        Text(
            text = stringResource(R.string.scans_title),
            style = MaterialTheme.typography.headlineMedium,
        )
        Spacer(modifier = Modifier.height(24.dp))

        when (uiState) {
            ScansUiState.Loading -> Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }

            is ScansUiState.Content -> LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(uiState.items) { item ->
                    ScanListItem(
                        item = item,
                        onClick = onScanSelected,
                    )
                }
            }

            ScansUiState.Empty -> Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.scans_empty),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            ScansUiState.StorageError -> Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = stringResource(R.string.scans_error_cache),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error,
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onRetry) {
                    Text(stringResource(R.string.scans_retry))
                }
            }

        }
    }
}

@Composable
private fun ScanListItem(
    item: ScanListItemUi,
    onClick: (Long) -> Unit,
) {
    Card(
        onClick = { item.id?.let(onClick) },
        modifier = Modifier.fillMaxWidth(),
        enabled = item.id != null,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = item.name ?: stringResource(R.string.scans_unnamed),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = item.createdAt ?: stringResource(R.string.scans_date_unavailable),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Preview(name = "Scans", showBackground = true)
@Composable
private fun ScansPreview() {
    DeeperTaskTheme(dynamicColor = false) {
        ScansContent(
            uiState = ScansUiState.Content(
                items = listOf(
                    ScanListItemUi(
                        id = 1L,
                        name = "Lake scan",
                        createdAt = "2026-09-02 18:30",
                    ),
                    ScanListItemUi(
                        id = 2L,
                        name = null,
                        createdAt = null,
                    ),
                ),
            ),
            onScanSelected = {},
            onRetry = {},
        )
    }
}

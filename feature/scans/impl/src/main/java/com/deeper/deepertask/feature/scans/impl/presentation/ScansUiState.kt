package com.deeper.deepertask.feature.scans.impl.presentation

internal sealed interface ScansUiState {
    data object Loading : ScansUiState

    data object Empty : ScansUiState

    data object StorageError : ScansUiState

    data class Content(
        val items: List<ScanListItemUi>,
    ) : ScansUiState
}

internal data class ScanListItemUi(
    val id: Long?,
    val name: String?,
    val createdAt: String?,
)

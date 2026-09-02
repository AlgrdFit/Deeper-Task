package com.deeper.deepertask.feature.scans.impl.presentation

import com.deeper.deepertask.feature.scans.api.ScanSummary
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.ZoneOffset

class ScanUiMapperTest {
    private val firstScanId = 1L
    private val secondScanId = 2L
    private val scanDate = "2030-04-26T11:07:22.332+00:00"

    @Test
    fun `maps empty scans to empty state`() {
        // Arrange
        val scans = emptyList<ScanSummary>()

        // Act
        val result = scans.toScansUiState(zoneId = ZoneOffset.UTC)

        // Assert
        assertEquals(ScansUiState.Empty, result)
    }

    @Test
    fun `maps scans in response order and formats ISO date`() {
        // Arrange
        val scans = listOf(
            ScanSummary(id = firstScanId, name = "  First scan  ", date = scanDate),
            ScanSummary(id = secondScanId, name = "Second scan", date = scanDate),
        )

        // Act
        val result = scans.toScansUiState(zoneId = ZoneOffset.UTC)

        // Assert
        assertEquals(
            ScansUiState.Content(
                items = listOf(
                    ScanListItemUi(
                        id = firstScanId,
                        name = "First scan",
                        createdAt = "2030-04-26 11:07",
                    ),
                    ScanListItemUi(
                        id = secondScanId,
                        name = "Second scan",
                        createdAt = "2030-04-26 11:07",
                    ),
                ),
            ),
            result,
        )
    }

    @Test
    fun `maps blank names and unavailable dates to fallback markers`() {
        // Arrange
        val scans = listOf(
            ScanSummary(id = firstScanId, name = "  ", date = null),
            ScanSummary(id = secondScanId, name = null, date = "not-a-date"),
        )

        // Act
        val result = scans.toScansUiState(zoneId = ZoneOffset.UTC)

        // Assert
        assertEquals(
            ScansUiState.Content(
                items = listOf(
                    ScanListItemUi(id = firstScanId, name = null, createdAt = null),
                    ScanListItemUi(id = secondScanId, name = null, createdAt = null),
                ),
            ),
            result,
        )
    }
}

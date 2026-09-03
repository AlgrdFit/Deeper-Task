package com.deeper.deepertask.feature.scans.impl.data.local

import com.deeper.deepertask.core.database.scans.ScanEntity
import com.deeper.deepertask.feature.scans.api.ScanSummary
import org.junit.Assert.assertEquals
import org.junit.Test

class ScanEntityMapperTest {
    private val scan = ScanSummary(
        id = null,
        name = "Lake scan",
        date = "2030-01-01T10:00:00Z",
    )

    @Test
    fun `maps scan to entity and back without losing nullable values`() {
        // Arrange
        val position = 3

        // Act
        val entity = scan.toEntity(position)
        val result = entity.toScanSummary()

        // Assert
        assertEquals(
            ScanEntity(
                position = position,
                id = scan.id,
                name = scan.name,
                date = scan.date,
            ),
            entity,
        )
        assertEquals(scan, result)
    }
}

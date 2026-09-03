package com.deeper.deepertask.feature.scans.impl.data.repository

import com.deeper.deepertask.core.database.scans.ScanDao
import com.deeper.deepertask.core.database.scans.ScanEntity
import com.deeper.deepertask.feature.scans.api.ScanSummary
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ScansRepositoryImplTest {
    private val scans = listOf(
        ScanSummary(id = 42L, name = "First", date = "2030-01-01T10:00:00Z"),
        ScanSummary(id = null, name = null, date = null),
    )
    private val entities = listOf(
        ScanEntity(position = 0, id = 42L, name = "First", date = "2030-01-01T10:00:00Z"),
        ScanEntity(position = 1, id = null, name = null, date = null),
    )
    private val scanDao = mockk<ScanDao>()

    @Test
    fun `observe maps the ordered database snapshot to scan summaries`() = runTest {
        // Arrange
        every { scanDao.observeAll() } returns flowOf(entities)
        val fixture = fixture()

        // Act
        val result = fixture.observeScans().first()

        // Assert
        assertEquals(scans, result)
    }

    @Test
    fun `replace assigns response positions and replaces the database snapshot`() = runTest {
        // Arrange
        coEvery { scanDao.replaceAll(entities) } just Runs
        val fixture = fixture()

        // Act
        fixture.replaceScans(scans)

        // Assert
        coVerify(exactly = 1) { scanDao.replaceAll(entities) }
    }

    private fun fixture(): ScansRepositoryImpl = ScansRepositoryImpl(scanDao)
}

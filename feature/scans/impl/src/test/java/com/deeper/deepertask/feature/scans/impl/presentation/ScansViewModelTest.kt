package com.deeper.deepertask.feature.scans.impl.presentation

import com.deeper.deepertask.feature.scans.api.ScanSummary
import com.deeper.deepertask.feature.scans.impl.domain.repository.ScansRepository
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class ScansViewModelTest {
    private val scans = listOf(
        ScanSummary(id = 42L, name = "Lake scan", date = null),
    )
    private val repository = mockk<ScansRepository>()

    @Test
    fun `load writes the route snapshot then reacts to database emissions`() = runTest {
        // Arrange
        val dispatcher = StandardTestDispatcher(testScheduler)
        val cachedScans = MutableStateFlow(emptyList<ScanSummary>())
        Dispatchers.setMain(dispatcher)
        coEvery { repository.replaceScans(scans) } just Runs
        every { repository.observeScans() } returns cachedScans
        val fixture = fixture()

        try {
            // Act
            fixture.load(scans)
            advanceUntilIdle()
            val emptyState = fixture.uiState.value
            cachedScans.value = scans
            advanceUntilIdle()

            // Assert
            assertEquals(ScansUiState.Empty, emptyState)
            assertEquals(
                ScansUiState.Content(
                    items = listOf(
                        ScanListItemUi(id = 42L, name = "Lake scan", createdAt = null),
                    ),
                ),
                fixture.uiState.value,
            )
            coVerify(exactly = 1) { repository.replaceScans(scans) }
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun `storage failure shows an error and retry starts a fresh load`() = runTest {
        // Arrange
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        coEvery { repository.replaceScans(scans) } throws IOException("Database unavailable")
        val fixture = fixture()

        try {
            fixture.load(scans)
            advanceUntilIdle()
            val errorState = fixture.uiState.value
            coEvery { repository.replaceScans(scans) } just Runs
            every { repository.observeScans() } returns MutableStateFlow(scans)

            // Act
            fixture.retry()
            advanceUntilIdle()

            // Assert
            assertEquals(ScansUiState.StorageError, errorState)
            assertEquals(
                ScansUiState.Content(
                    items = listOf(
                        ScanListItemUi(id = 42L, name = "Lake scan", createdAt = null),
                    ),
                ),
                fixture.uiState.value,
            )
            coVerify(exactly = 2) { repository.replaceScans(scans) }
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun `loading the same scans again does not restart collection`() = runTest {
        // Arrange
        val dispatcher = StandardTestDispatcher(testScheduler)
        val cachedScans = MutableStateFlow(scans)
        Dispatchers.setMain(dispatcher)
        coEvery { repository.replaceScans(scans) } just Runs
        every { repository.observeScans() } returns cachedScans
        val fixture = fixture()

        try {
            fixture.load(scans)
            advanceUntilIdle()

            // Act
            fixture.load(scans.toList())
            advanceUntilIdle()

            // Assert
            coVerify(exactly = 1) { repository.replaceScans(scans) }
            verify(exactly = 1) { repository.observeScans() }
        } finally {
            Dispatchers.resetMain()
        }
    }

    private fun fixture(): ScansViewModel = ScansViewModel(repository)
}

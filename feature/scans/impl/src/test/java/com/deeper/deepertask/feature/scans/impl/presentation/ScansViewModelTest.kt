package com.deeper.deepertask.feature.scans.impl.presentation

import com.deeper.deepertask.feature.scans.api.ScanSummary
import com.deeper.deepertask.feature.scans.impl.domain.repository.ScansRepository
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class ScansViewModelTest {
    private val scans = listOf(
        ScanSummary(id = 42L, name = "Lake scan", date = null),
    )
    private val repository = mockk<ScansRepository>()

    @Test
    fun `cache writes the route snapshot`() = runTest {
        // Arrange
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        coEvery { repository.replaceScans(scans) } just Runs
        val fixture = fixture()

        try {
            // Act
            fixture.cache(scans)
            advanceUntilIdle()

            // Assert
            coVerify(exactly = 1) { repository.replaceScans(scans) }
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun `cache failure is ignored without retrying`() = runTest {
        // Arrange
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        coEvery { repository.replaceScans(scans) } throws IOException("Database unavailable")
        val fixture = fixture()

        try {
            // Act
            fixture.cache(scans)
            advanceUntilIdle()

            // Assert
            coVerify(exactly = 1) { repository.replaceScans(scans) }
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun `caching the same scans again does not repeat the write`() = runTest {
        // Arrange
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        coEvery { repository.replaceScans(scans) } just Runs
        val fixture = fixture()

        try {
            fixture.cache(scans)
            advanceUntilIdle()

            // Act
            fixture.cache(scans.toList())
            advanceUntilIdle()

            // Assert
            coVerify(exactly = 1) { repository.replaceScans(scans) }
        } finally {
            Dispatchers.resetMain()
        }
    }

    private fun fixture(): ScansViewModel = ScansViewModel(repository)
}

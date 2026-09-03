package com.deeper.deepertask.feature.bathymetry.impl.presentation

import com.deeper.deepertask.feature.bathymetry.impl.domain.model.BathymetryData
import com.deeper.deepertask.feature.bathymetry.impl.domain.model.BathymetryError
import com.deeper.deepertask.feature.bathymetry.impl.domain.model.BathymetryResult
import com.deeper.deepertask.feature.bathymetry.impl.domain.model.DepthBand
import com.deeper.deepertask.feature.bathymetry.impl.domain.model.GeoBounds
import com.deeper.deepertask.feature.bathymetry.impl.domain.model.GeoPoint
import com.deeper.deepertask.feature.bathymetry.impl.domain.repository.BathymetryRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BathymetryViewModelTest {
    private val scanId = 42L
    private val repository = mockk<BathymetryRepository>()
    private val uiMapper = mockk<BathymetryUiMapper>()
    private val data = BathymetryData(
        polygons = emptyList(),
        bounds = GeoBounds(
            southWest = GeoPoint(latitude = 10.0, longitude = 20.0),
            northEast = GeoPoint(latitude = 11.0, longitude = 21.0),
        ),
        depthBands = listOf(
            DepthBand(
                index = 0,
                minimumDepth = 1.0,
                maximumDepth = 1.0,
                includesMaximum = true,
            ),
        ),
    )
    private val map = mockk<BathymetryMapUi>()

    @Test
    fun `successful load transitions from loading to content`() = runTest {
        // Arrange
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        coEvery { repository.getBathymetry(scanId) } returns BathymetryResult.Success(data)
        every { uiMapper(data) } returns map
        val fixture = fixture(dispatcher)

        try {
            // Act
            fixture.load(scanId)
            val loadingState = fixture.uiState.value
            advanceUntilIdle()

            // Assert
            assertEquals(BathymetryUiState.Loading, loadingState)
            assertEquals(BathymetryUiState.Content(map), fixture.uiState.value)
            coVerify(exactly = 1) { repository.getBathymetry(scanId) }
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun `authentication failure exposes state and navigation event`() = runTest {
        // Arrange
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        coEvery {
            repository.getBathymetry(scanId)
        } returns BathymetryResult.Failure(BathymetryError.AuthenticationRequired)
        val fixture = fixture(dispatcher)

        try {
            // Act
            fixture.load(scanId)
            advanceUntilIdle()
            val event = fixture.events.first()

            // Assert
            assertEquals(BathymetryUiState.AuthenticationRequired, fixture.uiState.value)
            assertEquals(BathymetryEvent.NavigateToLogin, event)
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun `retry reloads current scan after error`() = runTest {
        // Arrange
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        coEvery { repository.getBathymetry(scanId) } returnsMany listOf(
            BathymetryResult.Failure(BathymetryError.Connectivity),
            BathymetryResult.Success(data),
        )
        every { uiMapper(data) } returns map
        val fixture = fixture(dispatcher)

        try {
            // Act
            fixture.load(scanId)
            advanceUntilIdle()
            val errorState = fixture.uiState.value
            fixture.retry()
            advanceUntilIdle()

            // Assert
            assertEquals(BathymetryUiState.Error(BathymetryError.Connectivity), errorState)
            assertEquals(BathymetryUiState.Content(map), fixture.uiState.value)
            coVerify(exactly = 2) { repository.getBathymetry(scanId) }
        } finally {
            Dispatchers.resetMain()
        }
    }

    private fun fixture(dispatcher: CoroutineDispatcher): BathymetryViewModel =
        BathymetryViewModel(
            repository = repository,
            uiMapper = uiMapper,
            defaultDispatcher = dispatcher,
        )
}

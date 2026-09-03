package com.deeper.deepertask.feature.bathymetry.impl.presentation

import androidx.compose.ui.graphics.Color
import com.deeper.deepertask.feature.bathymetry.impl.domain.model.BathymetryData
import com.deeper.deepertask.feature.bathymetry.impl.domain.model.BathymetryError
import com.deeper.deepertask.feature.bathymetry.impl.domain.model.BathymetryResult
import com.deeper.deepertask.feature.bathymetry.impl.domain.model.DepthBand
import com.deeper.deepertask.feature.bathymetry.impl.domain.model.GeoBounds
import com.deeper.deepertask.feature.bathymetry.impl.domain.model.GeoPoint
import com.deeper.deepertask.feature.bathymetry.impl.domain.repository.BathymetryRepository
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
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
    private val cachedData = bathymetryData(10.0)
    private val updatedData = bathymetryData(20.0)
    private val cachedMap = bathymetryMap(10.0)
    private val updatedMap = bathymetryMap(20.0)
    private val repository = mockk<BathymetryRepository>()
    private val uiMapper = mockk<BathymetryUiMapper>()

    @Test
    fun `database changes update visible bathymetry`() = runTest {
        // Arrange
        val dispatcher = StandardTestDispatcher(testScheduler)
        val results = MutableStateFlow<BathymetryResult>(BathymetryResult.Success(cachedData))
        Dispatchers.setMain(dispatcher)
        every { repository.getBathymetry(scanId) } returns results
        every { uiMapper(cachedData) } returns cachedMap
        every { uiMapper(updatedData) } returns updatedMap
        val fixture = fixture(dispatcher)

        try {
            fixture.load(scanId)
            advanceUntilIdle()
            val cachedState = fixture.uiState.value

            // Act
            results.value = BathymetryResult.Success(updatedData)
            advanceUntilIdle()

            // Assert
            assertEquals(BathymetryUiState.Content(cachedMap), cachedState)
            assertEquals(BathymetryUiState.Content(updatedMap), fixture.uiState.value)
            verify(exactly = 1) { repository.getBathymetry(scanId) }
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun `loading the same scan again does not restart collection`() = runTest {
        // Arrange
        val dispatcher = StandardTestDispatcher(testScheduler)
        val results = MutableStateFlow<BathymetryResult>(BathymetryResult.Success(cachedData))
        Dispatchers.setMain(dispatcher)
        every { repository.getBathymetry(scanId) } returns results
        every { uiMapper(cachedData) } returns cachedMap
        val fixture = fixture(dispatcher)
        fixture.load(scanId)
        advanceUntilIdle()

        try {
            // Act
            fixture.load(scanId)
            advanceUntilIdle()

            // Assert
            verify(exactly = 1) { repository.getBathymetry(scanId) }
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun `authentication failure exposes state and navigation event`() = runTest {
        // Arrange
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        every { repository.getBathymetry(scanId) } returns flowOf(
            BathymetryResult.Failure(BathymetryError.AuthenticationRequired),
        )
        val fixture = fixture(dispatcher)

        try {
            // Act
            fixture.load(scanId)
            advanceUntilIdle()

            // Assert
            assertEquals(BathymetryUiState.AuthenticationRequired, fixture.uiState.value)
            assertEquals(BathymetryEvent.NavigateToLogin, fixture.events.first())
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun `retry recollects bathymetry after an error`() = runTest {
        // Arrange
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        every { repository.getBathymetry(scanId) } returnsMany listOf(
            flowOf(BathymetryResult.Failure(BathymetryError.Connectivity)),
            flowOf(BathymetryResult.Success(cachedData)),
        )
        every { uiMapper(cachedData) } returns cachedMap
        val fixture = fixture(dispatcher)
        fixture.load(scanId)
        advanceUntilIdle()
        val errorState = fixture.uiState.value

        try {
            // Act
            fixture.retry()
            advanceUntilIdle()

            // Assert
            assertEquals(BathymetryUiState.Error(BathymetryError.Connectivity), errorState)
            assertEquals(BathymetryUiState.Content(cachedMap), fixture.uiState.value)
            verify(exactly = 2) { repository.getBathymetry(scanId) }
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

    private companion object {
        fun bathymetryData(latitude: Double) = BathymetryData(
            polygons = emptyList(),
            bounds = GeoBounds(
                southWest = GeoPoint(latitude = latitude, longitude = 20.0),
                northEast = GeoPoint(latitude = latitude + 1.0, longitude = 21.0),
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

        fun bathymetryMap(latitude: Double) = BathymetryMapUi(
            polygons = emptyList(),
            camera = BathymetryCameraUi(
                bounds = LatLngBounds(
                    LatLng(latitude, 20.0),
                    LatLng(latitude + 1.0, 21.0),
                ),
                center = LatLng(latitude + 0.5, 20.5),
                isDegenerate = false,
            ),
            legend = listOf(DepthLegendItemUi(label = "1 m", color = Color.Blue)),
        )
    }
}

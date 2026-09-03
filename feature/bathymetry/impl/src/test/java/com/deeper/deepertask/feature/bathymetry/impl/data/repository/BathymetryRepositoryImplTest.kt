package com.deeper.deepertask.feature.bathymetry.impl.data.repository

import com.deeper.deepertask.core.database.bathymetry.BathymetryCacheEntity
import com.deeper.deepertask.core.database.bathymetry.BathymetryDao
import com.deeper.deepertask.feature.bathymetry.impl.data.local.BathymetryCacheMapper
import com.deeper.deepertask.feature.bathymetry.impl.data.remote.BathymetryApi
import com.deeper.deepertask.feature.bathymetry.impl.data.remote.BathymetryDto
import com.deeper.deepertask.feature.bathymetry.impl.data.remote.BathymetryFeatureDto
import com.deeper.deepertask.feature.bathymetry.impl.data.remote.BathymetryGeometryDto
import com.deeper.deepertask.feature.bathymetry.impl.data.remote.BathymetryPropertiesDto
import com.deeper.deepertask.feature.bathymetry.impl.data.remote.GeoDataResponseDto
import com.deeper.deepertask.feature.bathymetry.impl.domain.model.BathymetryData
import com.deeper.deepertask.feature.bathymetry.impl.domain.model.BathymetryError
import com.deeper.deepertask.feature.bathymetry.impl.domain.model.BathymetryResult
import com.deeper.deepertask.feature.bathymetry.impl.domain.model.DepthBand
import com.deeper.deepertask.feature.bathymetry.impl.domain.model.GeoBounds
import com.deeper.deepertask.feature.bathymetry.impl.domain.model.GeoPoint
import com.deeper.deepertask.feature.login.api.TokenStore
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class BathymetryRepositoryImplTest {
    private val scanId = 42L
    private val token = "token"
    private val cachedEntity = BathymetryCacheEntity(scanId, "cached")
    private val cachedData = BathymetryData(
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
    private val validResponse = GeoDataResponseDto(
        bathymetry = BathymetryDto(
            type = "FeatureCollection",
            bbox = null,
            features = listOf(
                BathymetryFeatureDto(
                    type = "Feature",
                    properties = BathymetryPropertiesDto(depth = 1.0, id = "id"),
                    geometry = BathymetryGeometryDto(
                        type = "Polygon",
                        bbox = null,
                        coordinates = listOf(
                            listOf(
                                listOf(20.0, 10.0, 1.0),
                                listOf(21.0, 10.0, 1.0),
                                listOf(20.0, 11.0, 1.0),
                            ),
                        ),
                    ),
                ),
            ),
        ),
    )
    private val bathymetryApi = mockk<BathymetryApi>()
    private val bathymetryDao = mockk<BathymetryDao>()
    private val cacheMapper = mockk<BathymetryCacheMapper>()
    private val tokenStore = mockk<TokenStore>()

    @Test
    fun `cache hit emits saved data without checking the token or network`() = runTest {
        // Arrange
        coEvery { bathymetryDao.get(scanId) } returns cachedEntity
        every { bathymetryDao.observe(scanId) } returns flowOf(cachedEntity)
        every { cacheMapper.toDomainOrNull(cachedEntity) } returns cachedData
        val fixture = fixture(UnconfinedTestDispatcher(testScheduler))

        // Act
        val result = fixture.getBathymetry(scanId).first()

        // Assert
        assertEquals(BathymetryResult.Success(cachedData), result)
        verify(exactly = 0) { tokenStore.read() }
        coVerify(exactly = 0) { bathymetryApi.getBathymetry(any(), any(), any(), any()) }
        coVerify(exactly = 0) { bathymetryDao.upsert(any()) }
    }

    @Test
    fun `cache miss without a token returns authentication required`() = runTest {
        // Arrange
        coEvery { bathymetryDao.get(scanId) } returns null
        every { tokenStore.read() } returns null
        val fixture = fixture(UnconfinedTestDispatcher(testScheduler))

        // Act
        val result = fixture.getBathymetry(scanId).first()

        // Assert
        assertEquals(BathymetryResult.Failure(BathymetryError.AuthenticationRequired), result)
        coVerify(exactly = 0) { bathymetryApi.getBathymetry(any(), any(), any(), any()) }
        coVerify(exactly = 0) { bathymetryDao.upsert(any()) }
    }

    @Test
    fun `cache miss fetches once stores the response and emits the saved data`() = runTest {
        // Arrange
        coEvery { bathymetryDao.get(scanId) } returns null
        every { tokenStore.read() } returns token
        coEvery {
            bathymetryApi.getBathymetry("FAST", "BS", scanId, token)
        } returns validResponse
        every { cacheMapper.toEntity(scanId, validResponse) } returns cachedEntity
        coEvery { bathymetryDao.upsert(cachedEntity) } just Runs
        every { bathymetryDao.observe(scanId) } returns flowOf(cachedEntity)
        every { cacheMapper.toDomainOrNull(cachedEntity) } returns cachedData
        val fixture = fixture(UnconfinedTestDispatcher(testScheduler))

        // Act
        val result = fixture.getBathymetry(scanId).first()

        // Assert
        assertEquals(BathymetryResult.Success(cachedData), result)
        coVerify(exactly = 1) {
            bathymetryApi.getBathymetry("FAST", "BS", scanId, token)
        }
        coVerify(exactly = 1) { bathymetryDao.upsert(cachedEntity) }
        verify(exactly = 0) { tokenStore.clear() }
    }

    @Test
    fun `invalid network response is not cached`() = runTest {
        // Arrange
        val invalidResponse = GeoDataResponseDto(bathymetry = null)
        coEvery { bathymetryDao.get(scanId) } returns null
        every { tokenStore.read() } returns token
        coEvery {
            bathymetryApi.getBathymetry(any(), any(), any(), any())
        } returns invalidResponse
        val fixture = fixture(UnconfinedTestDispatcher(testScheduler))

        // Act
        val result = fixture.getBathymetry(scanId).first()

        // Assert
        assertEquals(BathymetryResult.Failure(BathymetryError.InvalidData), result)
        coVerify(exactly = 0) { bathymetryDao.upsert(any()) }
    }

    @Test
    fun `network failure on a cache miss does not write data`() = runTest {
        // Arrange
        coEvery { bathymetryDao.get(scanId) } returns null
        every { tokenStore.read() } returns token
        coEvery {
            bathymetryApi.getBathymetry(any(), any(), any(), any())
        } throws IOException("Offline")
        val fixture = fixture(UnconfinedTestDispatcher(testScheduler))

        // Act
        val result = fixture.getBathymetry(scanId).first()

        // Assert
        assertEquals(BathymetryResult.Failure(BathymetryError.Connectivity), result)
        coVerify(exactly = 0) { bathymetryDao.upsert(any()) }
        verify(exactly = 0) { tokenStore.clear() }
    }

    @Test
    fun `unauthorized response clears the token on an uncached request`() = runTest {
        // Arrange
        coEvery { bathymetryDao.get(scanId) } returns null
        every { tokenStore.read() } returns token
        coEvery {
            bathymetryApi.getBathymetry(any(), any(), any(), any())
        } throws httpException(401)
        every { tokenStore.clear() } just Runs
        val fixture = fixture(UnconfinedTestDispatcher(testScheduler))

        // Act
        val result = fixture.getBathymetry(scanId).first()

        // Assert
        assertEquals(BathymetryResult.Failure(BathymetryError.AuthenticationRequired), result)
        verify(exactly = 1) { tokenStore.clear() }
        coVerify(exactly = 0) { bathymetryDao.upsert(any()) }
    }

    @Test
    fun `database write failure returns storage error`() = runTest {
        // Arrange
        coEvery { bathymetryDao.get(scanId) } returns null
        every { tokenStore.read() } returns token
        coEvery {
            bathymetryApi.getBathymetry(any(), any(), any(), any())
        } returns validResponse
        every { cacheMapper.toEntity(scanId, validResponse) } returns cachedEntity
        coEvery { bathymetryDao.upsert(cachedEntity) } throws IOException("Disk unavailable")
        val fixture = fixture(UnconfinedTestDispatcher(testScheduler))

        // Act
        val result = fixture.getBathymetry(scanId).first()

        // Assert
        assertEquals(BathymetryResult.Failure(BathymetryError.Storage), result)
    }

    @Test
    fun `database read failure does not check the token or network`() = runTest {
        // Arrange
        coEvery { bathymetryDao.get(scanId) } throws IOException("Disk unavailable")
        val fixture = fixture(UnconfinedTestDispatcher(testScheduler))

        // Act
        val result = fixture.getBathymetry(scanId).first()

        // Assert
        assertEquals(BathymetryResult.Failure(BathymetryError.Storage), result)
        verify(exactly = 0) { tokenStore.read() }
        coVerify(exactly = 0) { bathymetryApi.getBathymetry(any(), any(), any(), any()) }
    }

    private fun fixture(dispatcher: CoroutineDispatcher): BathymetryRepositoryImpl =
        BathymetryRepositoryImpl(
            bathymetryApi = bathymetryApi,
            bathymetryDao = bathymetryDao,
            cacheMapper = cacheMapper,
            tokenStore = tokenStore,
            defaultDispatcher = dispatcher,
        )

    private fun httpException(statusCode: Int): HttpException = HttpException(
        Response.error<Unit>(
            statusCode,
            "".toResponseBody("application/json".toMediaType()),
        ),
    )
}

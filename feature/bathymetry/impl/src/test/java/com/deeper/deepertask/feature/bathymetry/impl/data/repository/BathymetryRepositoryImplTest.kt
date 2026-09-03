package com.deeper.deepertask.feature.bathymetry.impl.data.repository

import com.deeper.deepertask.feature.bathymetry.impl.data.remote.BathymetryApi
import com.deeper.deepertask.feature.bathymetry.impl.data.remote.BathymetryDto
import com.deeper.deepertask.feature.bathymetry.impl.data.remote.BathymetryFeatureDto
import com.deeper.deepertask.feature.bathymetry.impl.data.remote.BathymetryGeometryDto
import com.deeper.deepertask.feature.bathymetry.impl.data.remote.BathymetryPropertiesDto
import com.deeper.deepertask.feature.bathymetry.impl.data.remote.GeoDataResponseDto
import com.deeper.deepertask.feature.bathymetry.impl.domain.model.BathymetryError
import com.deeper.deepertask.feature.bathymetry.impl.domain.model.BathymetryResult
import com.deeper.deepertask.feature.login.api.TokenStore
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class BathymetryRepositoryImplTest {
    private val scanId = 42L
    private val token = "token"
    private val bathymetryApi = mockk<BathymetryApi>()
    private val tokenStore = mockk<TokenStore>()

    @Test
    fun `missing token returns authentication required without API call`() = runTest {
        // Arrange
        every { tokenStore.read() } returns null
        val fixture = fixture(StandardTestDispatcher(testScheduler))

        // Act
        val result = fixture.getBathymetry(scanId)

        // Assert
        assertEquals(BathymetryResult.Failure(BathymetryError.AuthenticationRequired), result)
        coVerify(exactly = 0) { bathymetryApi.getBathymetry(any(), any(), any(), any()) }
    }

    @Test
    fun `successful response uses fixed query arguments and stored token`() = runTest {
        // Arrange
        every { tokenStore.read() } returns token
        coEvery {
            bathymetryApi.getBathymetry("FAST", "BS", scanId, token)
        } returns validResponse()
        val fixture = fixture(StandardTestDispatcher(testScheduler))

        // Act
        val result = fixture.getBathymetry(scanId)

        // Assert
        assertTrue(result is BathymetryResult.Success)
        coVerify(exactly = 1) {
            bathymetryApi.getBathymetry("FAST", "BS", scanId, token)
        }
        verify(exactly = 0) { tokenStore.clear() }
    }

    @Test
    fun `unauthorized response clears token and returns authentication required`() = runTest {
        // Arrange
        every { tokenStore.read() } returns token
        coEvery {
            bathymetryApi.getBathymetry(any(), any(), any(), any())
        } throws httpException(401)
        every { tokenStore.clear() } just Runs
        val fixture = fixture(StandardTestDispatcher(testScheduler))

        // Act
        val result = fixture.getBathymetry(scanId)

        // Assert
        assertEquals(BathymetryResult.Failure(BathymetryError.AuthenticationRequired), result)
        verify(exactly = 1) { tokenStore.clear() }
    }

    @Test
    fun `forbidden response clears token and returns authentication required`() = runTest {
        // Arrange
        every { tokenStore.read() } returns token
        coEvery {
            bathymetryApi.getBathymetry(any(), any(), any(), any())
        } throws httpException(403)
        every { tokenStore.clear() } just Runs
        val fixture = fixture(StandardTestDispatcher(testScheduler))

        // Act
        val result = fixture.getBathymetry(scanId)

        // Assert
        assertEquals(BathymetryResult.Failure(BathymetryError.AuthenticationRequired), result)
        verify(exactly = 1) { tokenStore.clear() }
    }

    @Test
    fun `transport failure preserves token and returns connectivity error`() = runTest {
        // Arrange
        every { tokenStore.read() } returns token
        coEvery {
            bathymetryApi.getBathymetry(any(), any(), any(), any())
        } throws IOException("Offline")
        val fixture = fixture(StandardTestDispatcher(testScheduler))

        // Act
        val result = fixture.getBathymetry(scanId)

        // Assert
        assertEquals(BathymetryResult.Failure(BathymetryError.Connectivity), result)
        verify(exactly = 0) { tokenStore.clear() }
    }

    private fun fixture(dispatcher: CoroutineDispatcher): BathymetryRepositoryImpl =
        BathymetryRepositoryImpl(
            bathymetryApi = bathymetryApi,
            tokenStore = tokenStore,
            defaultDispatcher = dispatcher,
        )

    private fun validResponse(): GeoDataResponseDto = GeoDataResponseDto(
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

    private fun httpException(statusCode: Int): HttpException = HttpException(
        Response.error<Unit>(
            statusCode,
            "".toResponseBody("application/json".toMediaType()),
        ),
    )
}

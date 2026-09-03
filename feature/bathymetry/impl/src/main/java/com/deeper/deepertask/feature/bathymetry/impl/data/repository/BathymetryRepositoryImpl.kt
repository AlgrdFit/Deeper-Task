package com.deeper.deepertask.feature.bathymetry.impl.data.repository

import com.deeper.deepertask.core.coroutines.DefaultDispatcher
import com.deeper.deepertask.core.network.NetworkError
import com.deeper.deepertask.core.network.NetworkResult
import com.deeper.deepertask.core.network.networkCall
import com.deeper.deepertask.feature.bathymetry.impl.data.mapper.toBathymetryDataOrNull
import com.deeper.deepertask.feature.bathymetry.impl.data.remote.BathymetryApi
import com.deeper.deepertask.feature.bathymetry.impl.domain.model.BathymetryError
import com.deeper.deepertask.feature.bathymetry.impl.domain.model.BathymetryResult
import com.deeper.deepertask.feature.bathymetry.impl.domain.repository.BathymetryRepository
import com.deeper.deepertask.feature.login.api.TokenStore
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

internal class BathymetryRepositoryImpl @Inject constructor(
    private val bathymetryApi: BathymetryApi,
    private val tokenStore: TokenStore,
    @param:DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
) : BathymetryRepository {
    override suspend fun getBathymetry(scanId: Long): BathymetryResult {
        val token = tokenStore.read()?.takeIf(String::isNotBlank)
            ?: return BathymetryResult.Failure(BathymetryError.AuthenticationRequired)

        return when (
            val result = networkCall {
                bathymetryApi.getBathymetry(
                    grid = GRID,
                    generator = GENERATOR,
                    scanId = scanId,
                    token = token,
                )
            }
        ) {
            is NetworkResult.Success -> {
                val data = withContext(defaultDispatcher) {
                    result.value.toBathymetryDataOrNull()
                }
                data?.let(BathymetryResult::Success)
                    ?: BathymetryResult.Failure(BathymetryError.InvalidData)
            }

            is NetworkResult.Failure -> result.error.toBathymetryResult(tokenStore)
        }
    }

    private companion object {
        const val GRID = "FAST"
        const val GENERATOR = "BS"
    }
}

private fun NetworkError.toBathymetryResult(tokenStore: TokenStore): BathymetryResult.Failure =
    when (this) {
        is NetworkError.Http -> if (statusCode == 401 || statusCode == 403) {
            tokenStore.clear()
            BathymetryResult.Failure(BathymetryError.AuthenticationRequired)
        } else {
            BathymetryResult.Failure(BathymetryError.Service)
        }

        NetworkError.Timeout,
        NetworkError.Transport,
        -> BathymetryResult.Failure(BathymetryError.Connectivity)

        NetworkError.Serialization,
        NetworkError.Unexpected,
        -> BathymetryResult.Failure(BathymetryError.Service)
    }

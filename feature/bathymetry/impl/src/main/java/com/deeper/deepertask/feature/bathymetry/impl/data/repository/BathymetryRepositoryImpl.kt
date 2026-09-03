package com.deeper.deepertask.feature.bathymetry.impl.data.repository

import com.deeper.deepertask.core.coroutines.DefaultDispatcher
import com.deeper.deepertask.core.database.bathymetry.BathymetryCacheEntity
import com.deeper.deepertask.core.database.bathymetry.BathymetryDao
import com.deeper.deepertask.core.network.NetworkError
import com.deeper.deepertask.core.network.NetworkResult
import com.deeper.deepertask.core.network.networkCall
import com.deeper.deepertask.feature.bathymetry.impl.data.local.BathymetryCacheMapper
import com.deeper.deepertask.feature.bathymetry.impl.data.mapper.toBathymetryDataOrNull
import com.deeper.deepertask.feature.bathymetry.impl.data.remote.BathymetryApi
import com.deeper.deepertask.feature.bathymetry.impl.domain.model.BathymetryError
import com.deeper.deepertask.feature.bathymetry.impl.domain.model.BathymetryResult
import com.deeper.deepertask.feature.bathymetry.impl.domain.repository.BathymetryRepository
import com.deeper.deepertask.feature.login.api.TokenStore
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlin.coroutines.cancellation.CancellationException
import javax.inject.Inject

internal class BathymetryRepositoryImpl @Inject constructor(
    private val bathymetryApi: BathymetryApi,
    private val bathymetryDao: BathymetryDao,
    private val cacheMapper: BathymetryCacheMapper,
    private val tokenStore: TokenStore,
    @param:DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
) : BathymetryRepository {
    override fun getBathymetry(scanId: Long): Flow<BathymetryResult> = flow {
        try {
            if (bathymetryDao.get(scanId) == null) {
                when (val result = fetchAndCacheBathymetry(scanId)) {
                    is BathymetryResult.Success -> Unit
                    is BathymetryResult.Failure -> {
                        emit(result)
                        return@flow
                    }
                }
            }

            emitAll(
                bathymetryDao.observe(scanId).map { entity ->
                    entity.toBathymetryResult()
                },
            )
        } catch (exception: CancellationException) {
            throw exception
        } catch (_: Exception) {
            emit(BathymetryResult.Failure(BathymetryError.Storage))
        }
    }.flowOn(defaultDispatcher)

    private suspend fun fetchAndCacheBathymetry(scanId: Long): BathymetryResult {
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
                val data = result.value.toBathymetryDataOrNull()
                    ?: return BathymetryResult.Failure(BathymetryError.InvalidData)
                try {
                    bathymetryDao.upsert(cacheMapper.toEntity(scanId, result.value))
                    BathymetryResult.Success(data)
                } catch (exception: CancellationException) {
                    throw exception
                } catch (_: Exception) {
                    BathymetryResult.Failure(BathymetryError.Storage)
                }
            }

            is NetworkResult.Failure -> result.error.toBathymetryResult(tokenStore)
        }
    }

    private fun BathymetryCacheEntity?.toBathymetryResult(): BathymetryResult = when {
        this == null -> BathymetryResult.Failure(BathymetryError.Storage)
        else -> cacheMapper.toDomainOrNull(this)
            ?.let(BathymetryResult::Success)
            ?: BathymetryResult.Failure(BathymetryError.InvalidData)
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

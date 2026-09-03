package com.deeper.deepertask.feature.bathymetry.impl.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deeper.deepertask.core.coroutines.DefaultDispatcher
import com.deeper.deepertask.feature.bathymetry.impl.domain.model.BathymetryError
import com.deeper.deepertask.feature.bathymetry.impl.domain.model.BathymetryResult
import com.deeper.deepertask.feature.bathymetry.impl.domain.repository.BathymetryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.cancellation.CancellationException
import javax.inject.Inject

@HiltViewModel
internal class BathymetryViewModel @Inject constructor(
    private val repository: BathymetryRepository,
    private val uiMapper: BathymetryUiMapper,
    @param:DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
) : ViewModel() {
    private val mutableUiState = MutableStateFlow<BathymetryUiState>(BathymetryUiState.Loading)
    val uiState: StateFlow<BathymetryUiState> = mutableUiState.asStateFlow()

    private val eventsChannel = Channel<BathymetryEvent>(capacity = Channel.BUFFERED)
    val events = eventsChannel.receiveAsFlow()

    private var currentScanId: Long? = null

    fun load(scanId: Long) {
        if (currentScanId == scanId) {
            return
        }
        currentScanId = scanId
        startLoad(scanId)
    }

    fun retry() {
        currentScanId?.let(::startLoad)
    }

    private fun startLoad(scanId: Long) {
        viewModelScope.launch {
            mutableUiState.value = BathymetryUiState.Loading
            try {
                repository.getBathymetry(scanId).collect { result ->
                    when (result) {
                        is BathymetryResult.Success -> {
                            val map = withContext(defaultDispatcher) {
                                uiMapper(result.data)
                            }
                            mutableUiState.value = BathymetryUiState.Content(map)
                        }

                        is BathymetryResult.Failure -> handleFailure(result.error)
                    }
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: Exception) {
                mutableUiState.value = BathymetryUiState.Error(BathymetryError.Storage)
            }
        }
    }

    private suspend fun handleFailure(error: BathymetryError) {
        if (error == BathymetryError.AuthenticationRequired) {
            mutableUiState.value = BathymetryUiState.AuthenticationRequired
            eventsChannel.send(BathymetryEvent.NavigateToLogin)
        } else {
            mutableUiState.value = BathymetryUiState.Error(error)
        }
    }
}

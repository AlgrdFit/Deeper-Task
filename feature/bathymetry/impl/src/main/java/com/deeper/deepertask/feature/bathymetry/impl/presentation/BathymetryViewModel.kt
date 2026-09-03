package com.deeper.deepertask.feature.bathymetry.impl.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deeper.deepertask.feature.bathymetry.impl.di.BathymetryDefaultDispatcher
import com.deeper.deepertask.feature.bathymetry.impl.domain.model.BathymetryError
import com.deeper.deepertask.feature.bathymetry.impl.domain.model.BathymetryResult
import com.deeper.deepertask.feature.bathymetry.impl.domain.repository.BathymetryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
internal class BathymetryViewModel @Inject constructor(
    private val repository: BathymetryRepository,
    private val uiMapper: BathymetryUiMapper,
    @param:BathymetryDefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
) : ViewModel() {
    private val mutableUiState = MutableStateFlow<BathymetryUiState>(BathymetryUiState.Loading)
    val uiState: StateFlow<BathymetryUiState> = mutableUiState.asStateFlow()

    private val eventsChannel = Channel<BathymetryEvent>(capacity = Channel.BUFFERED)
    val events = eventsChannel.receiveAsFlow()

    private var currentScanId: Long? = null
    private var loadJob: Job? = null

    fun load(scanId: Long) {
        currentScanId = scanId
        startLoad(scanId)
    }

    fun retry() {
        currentScanId?.let(::startLoad)
    }

    private fun startLoad(scanId: Long) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            mutableUiState.value = BathymetryUiState.Loading
            when (val result = repository.getBathymetry(scanId)) {
                is BathymetryResult.Success -> {
                    val map = withContext(defaultDispatcher) {
                        uiMapper(result.data)
                    }
                    mutableUiState.value = BathymetryUiState.Content(map)
                }

                is BathymetryResult.Failure -> {
                    if (result.error == BathymetryError.AuthenticationRequired) {
                        mutableUiState.value = BathymetryUiState.AuthenticationRequired
                        eventsChannel.send(BathymetryEvent.NavigateToLogin)
                    } else {
                        mutableUiState.value = BathymetryUiState.Error(result.error)
                    }
                }
            }
        }
    }
}

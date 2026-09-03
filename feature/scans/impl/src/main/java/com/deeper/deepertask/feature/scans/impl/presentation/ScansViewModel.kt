package com.deeper.deepertask.feature.scans.impl.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deeper.deepertask.feature.scans.api.ScanSummary
import com.deeper.deepertask.feature.scans.impl.domain.repository.ScansRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException
import javax.inject.Inject

@HiltViewModel
internal class ScansViewModel @Inject constructor(
    private val repository: ScansRepository,
) : ViewModel() {
    private val mutableUiState = MutableStateFlow<ScansUiState>(ScansUiState.Loading)
    val uiState: StateFlow<ScansUiState> = mutableUiState.asStateFlow()

    private var currentScans: List<ScanSummary>? = null

    fun load(scans: List<ScanSummary>) {
        val snapshot = scans.toList()
        if (snapshot == currentScans) {
            return
        }
        currentScans = snapshot
        startLoad(snapshot)
    }

    fun retry() {
        currentScans?.let(::startLoad)
    }

    private fun startLoad(scans: List<ScanSummary>) {
        viewModelScope.launch {
            mutableUiState.value = ScansUiState.Loading
            try {
                repository.replaceScans(scans)
                repository.observeScans().collect { cachedScans ->
                    mutableUiState.value = cachedScans.toScansUiState()
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: Exception) {
                mutableUiState.value = ScansUiState.StorageError
            }
        }
    }
}

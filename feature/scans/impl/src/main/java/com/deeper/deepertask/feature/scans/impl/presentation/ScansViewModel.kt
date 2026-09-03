package com.deeper.deepertask.feature.scans.impl.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deeper.deepertask.feature.scans.api.ScanSummary
import com.deeper.deepertask.feature.scans.impl.domain.repository.ScansRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class ScansViewModel @Inject constructor(
    private val repository: ScansRepository,
) : ViewModel() {
    private var lastSubmittedScans: List<ScanSummary>? = null

    fun cache(scans: List<ScanSummary>) {
        val snapshot = scans.toList()
        if (snapshot == lastSubmittedScans) {
            return
        }
        lastSubmittedScans = snapshot

        viewModelScope.launch {
            try {
                repository.replaceScans(snapshot)
            } catch (_: Exception) {
                ensureActive()
            }
        }
    }
}

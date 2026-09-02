package com.deeper.deepertask.feature.login.impl.presentation

import com.deeper.deepertask.feature.scans.api.ScanSummary

internal sealed interface LoginEvent {
    data class NavigateToScans(
        val scans: List<ScanSummary>,
    ) : LoginEvent
}

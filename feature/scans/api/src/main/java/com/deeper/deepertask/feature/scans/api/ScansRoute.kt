package com.deeper.deepertask.feature.scans.api

import com.deeper.deepertask.core.navigation.AppNavKey
import kotlinx.serialization.Serializable

@Serializable
data class ScansRoute(
    val scans: List<ScanSummary>,
) : AppNavKey

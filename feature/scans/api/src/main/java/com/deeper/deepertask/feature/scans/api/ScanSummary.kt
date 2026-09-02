package com.deeper.deepertask.feature.scans.api

import kotlinx.serialization.Serializable

@Serializable
data class ScanSummary(
    val id: Long?,
    val name: String?,
    val date: String?,
)

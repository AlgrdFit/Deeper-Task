package com.deeper.deepertask.feature.bathymetry.api

import com.deeper.deepertask.core.navigation.AppNavKey
import kotlinx.serialization.Serializable

@Serializable
data class BathymetryRoute(
    val scanId: Long,
) : AppNavKey

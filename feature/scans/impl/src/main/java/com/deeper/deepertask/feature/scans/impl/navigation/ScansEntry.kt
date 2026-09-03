package com.deeper.deepertask.feature.scans.impl.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.deeper.deepertask.core.navigation.Navigator
import com.deeper.deepertask.feature.bathymetry.api.BathymetryRoute
import com.deeper.deepertask.feature.scans.api.ScansRoute
import com.deeper.deepertask.feature.scans.impl.presentation.ScansScreen

fun EntryProviderScope<NavKey>.scansEntry(
    navigator: Navigator,
) {
    entry<ScansRoute> { route ->
        ScansScreen(
            scans = route.scans,
            onScanSelected = { scanId -> navigator.navigate(BathymetryRoute(scanId)) },
        )
    }
}

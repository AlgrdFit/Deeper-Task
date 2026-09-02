package com.deeper.deepertask.feature.scans.impl.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.deeper.deepertask.feature.scans.api.ScansRoute
import com.deeper.deepertask.feature.scans.impl.presentation.ScansScreen

fun EntryProviderScope<NavKey>.scansEntry() {
    entry<ScansRoute> { route ->
        ScansScreen(
            scans = route.scans,
        )
    }
}

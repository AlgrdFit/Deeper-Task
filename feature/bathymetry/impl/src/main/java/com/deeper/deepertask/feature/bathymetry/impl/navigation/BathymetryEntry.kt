package com.deeper.deepertask.feature.bathymetry.impl.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.deeper.deepertask.core.navigation.Navigator
import com.deeper.deepertask.feature.bathymetry.api.BathymetryRoute
import com.deeper.deepertask.feature.bathymetry.impl.presentation.BathymetryScreen
import com.deeper.deepertask.feature.login.api.LoginRoute

fun EntryProviderScope<NavKey>.bathymetryEntry(
    navigator: Navigator,
) {
    entry<BathymetryRoute> { route ->
        BathymetryScreen(
            scanId = route.scanId,
            onBack = navigator::goBack,
            onAuthenticationRequired = { navigator.replaceAll(LoginRoute) },
        )
    }
}

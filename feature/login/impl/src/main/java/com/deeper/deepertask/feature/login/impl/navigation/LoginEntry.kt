package com.deeper.deepertask.feature.login.impl.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.deeper.deepertask.core.navigation.Navigator
import com.deeper.deepertask.feature.login.api.LoginRoute
import com.deeper.deepertask.feature.login.impl.presentation.LoginScreen
import com.deeper.deepertask.feature.scans.api.ScansRoute

fun EntryProviderScope<NavKey>.loginEntry(
    navigator: Navigator,
) {
    entry<LoginRoute> {
        LoginScreen(
            onLoginSuccess = { scans -> navigator.replaceAll(ScansRoute(scans)) },
        )
    }
}

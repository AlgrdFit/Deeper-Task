package com.deeper.deepertask

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.deeper.deepertask.feature.login.api.LoginRoute
import com.deeper.deepertask.feature.login.impl.navigation.loginEntry
import com.deeper.deepertask.feature.scans.impl.navigation.scansEntry
import com.deeper.deepertask.navigation.BackStackNavigator

@Composable
internal fun DeeperTaskApp() {
    val backStack = rememberNavBackStack(LoginRoute)
    val navigator = remember(backStack) {
        BackStackNavigator(backStack)
    }

    NavDisplay(
        backStack = backStack,
        modifier = Modifier.fillMaxSize(),
        onBack = navigator::goBack,
        entryProvider = entryProvider {
            loginEntry(navigator)
            scansEntry()
        },
    )
}

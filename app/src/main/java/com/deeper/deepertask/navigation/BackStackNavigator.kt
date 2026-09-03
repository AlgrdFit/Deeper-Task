package com.deeper.deepertask.navigation

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.deeper.deepertask.core.navigation.AppNavKey
import com.deeper.deepertask.core.navigation.Navigator

internal class BackStackNavigator(
    private val backStack: NavBackStack<NavKey>,
) : Navigator {
    override fun navigate(route: AppNavKey) {
        backStack.add(route)
    }

    override fun replaceAll(route: AppNavKey) {
        backStack.clear()
        backStack.add(route)
    }

    override fun goBack() {
        backStack.removeLastOrNull()
    }
}

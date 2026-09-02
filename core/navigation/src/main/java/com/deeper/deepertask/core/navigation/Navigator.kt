package com.deeper.deepertask.core.navigation

interface Navigator {
    fun navigate(route: AppNavKey)

    fun replaceAll(route: AppNavKey)

    fun goBack()
}

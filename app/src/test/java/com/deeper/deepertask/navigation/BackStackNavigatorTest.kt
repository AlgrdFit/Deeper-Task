package com.deeper.deepertask.navigation

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.deeper.deepertask.core.navigation.AppNavKey
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BackStackNavigatorTest {
    private val firstRoute = TestRoute("first")
    private val secondRoute = TestRoute("second")
    private val replacementRoute = TestRoute("replacement")

    @Test
    fun `navigate appends route to back stack`() {
        // Arrange
        val backStack = NavBackStack<NavKey>(firstRoute)
        val fixture = fixture(backStack)

        // Act
        fixture.navigate(secondRoute)

        // Assert
        assertEquals(listOf(firstRoute, secondRoute), backStack.toList())
    }

    @Test
    fun `replace all removes existing routes and adds replacement`() {
        // Arrange
        val backStack = NavBackStack<NavKey>(firstRoute, secondRoute)
        val fixture = fixture(backStack)

        // Act
        fixture.replaceAll(replacementRoute)

        // Assert
        assertEquals(listOf(replacementRoute), backStack.toList())
    }

    @Test
    fun `go back removes latest route`() {
        // Arrange
        val backStack = NavBackStack<NavKey>(firstRoute, secondRoute)
        val fixture = fixture(backStack)

        // Act
        fixture.goBack()

        // Assert
        assertEquals(listOf(firstRoute), backStack.toList())
    }

    @Test
    fun `go back does nothing when back stack is empty`() {
        // Arrange
        val backStack = NavBackStack<NavKey>()
        val fixture = fixture(backStack)

        // Act
        fixture.goBack()

        // Assert
        assertTrue(backStack.isEmpty())
    }

    private fun fixture(
        backStack: NavBackStack<NavKey>,
    ): BackStackNavigator = BackStackNavigator(backStack)

    private data class TestRoute(val value: String) : AppNavKey
}

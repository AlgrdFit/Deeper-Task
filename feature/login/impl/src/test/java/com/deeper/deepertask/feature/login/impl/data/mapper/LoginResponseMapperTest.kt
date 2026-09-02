package com.deeper.deepertask.feature.login.impl.data.mapper

import com.deeper.deepertask.feature.login.impl.data.remote.LoginDto
import com.deeper.deepertask.feature.login.impl.data.remote.LoginResponseDto
import com.deeper.deepertask.feature.login.impl.data.remote.ScanDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LoginResponseMapperTest {
    private val sessionToken = "session-token"

    @Test
    fun `maps validated nonblank token without managing expiry or scans`() {
        // Arrange
        val response = LoginResponseDto(
            login = LoginDto(
                token = "  $sessionToken  ",
                validated = true,
                validTill = null,
            ),
            scans = listOf(
                ScanDto(id = 42L, name = "Lake scan", date = "not-a-date"),
            ),
        )

        // Act
        val result = response.toTokenOrNull()

        // Assert
        assertEquals(sessionToken, result)
    }

    @Test
    fun `rejects unvalidated login response`() {
        // Arrange
        val response = LoginResponseDto(
            login = LoginDto(
                token = sessionToken,
                validated = false,
                validTill = "2030-04-26T11:07:22Z",
            ),
            scans = emptyList(),
        )

        // Act
        val result = response.toTokenOrNull()

        // Assert
        assertNull(result)
    }

    @Test
    fun `rejects missing or blank token`() {
        // Arrange
        val missingToken = LoginResponseDto(
            login = LoginDto(token = null, validated = true, validTill = null),
            scans = emptyList(),
        )
        val blankToken = LoginResponseDto(
            login = LoginDto(token = "  ", validated = true, validTill = null),
            scans = emptyList(),
        )

        // Act
        val missingResult = missingToken.toTokenOrNull()
        val blankResult = blankToken.toTokenOrNull()

        // Assert
        assertNull(missingResult)
        assertNull(blankResult)
    }
}

package com.deeper.deepertask.feature.login.impl.data.mapper

import com.deeper.deepertask.feature.login.impl.data.remote.LoginDto
import com.deeper.deepertask.feature.login.impl.data.remote.LoginResponseDto
import com.deeper.deepertask.feature.login.impl.data.remote.ScanDto
import com.deeper.deepertask.feature.login.impl.domain.model.LoginResult
import com.deeper.deepertask.feature.scans.api.ScanSummary
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LoginResponseMapperTest {
    private val token = "token"
    private val scanId = 42L
    private val scanName = "Lake scan"
    private val scanDate = "2030-04-26T11:07:22Z"

    @Test
    fun `maps validated login and neutral scans to successful result`() {
        // Arrange
        val response = LoginResponseDto(
            login = LoginDto(
                token = "  $token  ",
                validated = true,
                validTill = null,
            ),
            scans = listOf(
                ScanDto(id = scanId, name = scanName, date = scanDate),
                null,
            ),
        )

        // Act
        val result = response.toLoginSuccessOrNull()

        // Assert
        assertEquals(
            LoginResult.Success(
                token = token,
                scans = listOf(
                    ScanSummary(id = scanId, name = scanName, date = scanDate),
                ),
            ),
            result,
        )
    }

    @Test
    fun `maps missing scans collection to empty initial scans`() {
        // Arrange
        val response = LoginResponseDto(
            login = LoginDto(
                token = token,
                validated = true,
                validTill = null,
            ),
            scans = null,
        )

        // Act
        val result = response.toLoginSuccessOrNull()

        // Assert
        assertEquals(
            LoginResult.Success(token = token, scans = emptyList()),
            result,
        )
    }

    @Test
    fun `rejects unvalidated login response`() {
        // Arrange
        val response = LoginResponseDto(
            login = LoginDto(
                token = token,
                validated = false,
                validTill = "2030-04-26T11:07:22Z",
            ),
            scans = emptyList(),
        )

        // Act
        val result = response.toLoginSuccessOrNull()

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
        val missingResult = missingToken.toLoginSuccessOrNull()
        val blankResult = blankToken.toLoginSuccessOrNull()

        // Assert
        assertNull(missingResult)
        assertNull(blankResult)
    }
}

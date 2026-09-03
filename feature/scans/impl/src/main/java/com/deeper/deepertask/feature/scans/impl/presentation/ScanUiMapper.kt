package com.deeper.deepertask.feature.scans.impl.presentation

import com.deeper.deepertask.feature.scans.api.ScanSummary
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val scanDateTimeFormatter = DateTimeFormatter.ofPattern(
    "yyyy-MM-dd HH:mm",
    Locale.ROOT,
)

internal fun List<ScanSummary>.toScansUiState(
    zoneId: ZoneId = ZoneId.systemDefault(),
): ScansUiState {
    if (isEmpty()) {
        return ScansUiState.Empty
    }

    return ScansUiState.Content(
        items = map { scan -> scan.toUiModel(zoneId) },
    )
}

private fun ScanSummary.toUiModel(zoneId: ZoneId): ScanListItemUi = ScanListItemUi(
    id = id,
    name = name?.trim()?.takeIf(String::isNotEmpty),
    createdAt = date.toDisplayDateTime(zoneId),
)

private fun String?.toDisplayDateTime(zoneId: ZoneId): String? {
    val value = this?.trim()?.takeIf(String::isNotEmpty) ?: return null
    val instant = runCatching { OffsetDateTime.parse(value).toInstant() }.getOrNull() ?: return null
    return scanDateTimeFormatter.withZone(zoneId).format(instant)
}

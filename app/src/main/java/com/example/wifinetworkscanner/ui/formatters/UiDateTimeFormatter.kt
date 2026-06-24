package com.example.wifinetworkscanner.ui.formatters

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max

fun Long.toDisplayDateTime(
    locale: Locale = Locale.getDefault()
): String {
    val formatter = SimpleDateFormat(DISPLAY_DATE_TIME_PATTERN, locale)
    return formatter.format(Date(this))
}

fun Long.toDurationSecondsUntil(
    endEpochMillis: Long
): Long {
    val durationMillis = endEpochMillis - this
    return max(0L, durationMillis / MILLIS_IN_SECOND)
}

private const val DISPLAY_DATE_TIME_PATTERN = "dd/MM/yyyy HH:mm"
private const val MILLIS_IN_SECOND = 1_000L
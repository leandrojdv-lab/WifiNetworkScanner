package com.example.wifinetworkscanner.ui.formatters

import org.junit.Assert.assertEquals
import org.junit.Test

class UiDateTimeFormatterTest {

    @Test
    fun toDurationSecondsUntil_whenEndIsAfterStart_shouldReturnWholeSeconds() {
        val result = 1_000L.toDurationSecondsUntil(
            endEpochMillis = 3_500L
        )

        assertEquals(2L, result)
    }

    @Test
    fun toDurationSecondsUntil_whenEndIsBeforeStart_shouldReturnZero() {
        val result = 5_000L.toDurationSecondsUntil(
            endEpochMillis = 3_500L
        )

        assertEquals(0L, result)
    }
}
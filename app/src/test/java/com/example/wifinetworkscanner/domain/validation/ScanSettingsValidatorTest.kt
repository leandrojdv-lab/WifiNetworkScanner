package com.example.wifinetworkscanner.domain.validation

import com.example.wifinetworkscanner.domain.model.ScanSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ScanSettingsValidatorTest {

    @Test
    fun validate_whenValuesAreValid_shouldReturnSuccess() {
        val result = ScanSettingsValidator.validate(
            maxHostsText = ScanSettings.DEFAULT_MAX_HOSTS.toString(),
            timeoutMillisText = ScanSettings.DEFAULT_TIMEOUT_MILLIS.toString(),
            parallelismText = ScanSettings.DEFAULT_PARALLELISM.toString()
        )

        assertTrue(result is ScanSettingsValidationResult.Success)

        val success = result as ScanSettingsValidationResult.Success

        assertEquals(
            ScanSettings.DEFAULT_MAX_HOSTS,
            success.scanSettings.maxHosts
        )
        assertEquals(
            ScanSettings.DEFAULT_TIMEOUT_MILLIS,
            success.scanSettings.timeoutMillis
        )
        assertEquals(
            ScanSettings.DEFAULT_PARALLELISM,
            success.scanSettings.parallelism
        )
    }

    @Test
    fun validate_whenMaxHostsIsNotNumber_shouldReturnInvalidMaxHostsValue() {
        val result = ScanSettingsValidator.validate(
            maxHostsText = "",
            timeoutMillisText = ScanSettings.DEFAULT_TIMEOUT_MILLIS.toString(),
            parallelismText = ScanSettings.DEFAULT_PARALLELISM.toString()
        )

        assertValidationError(
            result = result,
            expectedError = ScanSettingsValidationError.InvalidMaxHostsValue
        )
    }

    @Test
    fun validate_whenTimeoutIsNotNumber_shouldReturnInvalidTimeoutMillisValue() {
        val result = ScanSettingsValidator.validate(
            maxHostsText = ScanSettings.DEFAULT_MAX_HOSTS.toString(),
            timeoutMillisText = "",
            parallelismText = ScanSettings.DEFAULT_PARALLELISM.toString()
        )

        assertValidationError(
            result = result,
            expectedError = ScanSettingsValidationError.InvalidTimeoutMillisValue
        )
    }

    @Test
    fun validate_whenParallelismIsNotNumber_shouldReturnInvalidParallelismValue() {
        val result = ScanSettingsValidator.validate(
            maxHostsText = ScanSettings.DEFAULT_MAX_HOSTS.toString(),
            timeoutMillisText = ScanSettings.DEFAULT_TIMEOUT_MILLIS.toString(),
            parallelismText = ""
        )

        assertValidationError(
            result = result,
            expectedError = ScanSettingsValidationError.InvalidParallelismValue
        )
    }

    @Test
    fun validate_whenMaxHostsIsOutOfRange_shouldReturnMaxHostsOutOfRange() {
        val result = ScanSettingsValidator.validate(
            maxHostsText = (ScanSettings.MIN_HOSTS - 1).toString(),
            timeoutMillisText = ScanSettings.DEFAULT_TIMEOUT_MILLIS.toString(),
            parallelismText = ScanSettings.DEFAULT_PARALLELISM.toString()
        )

        assertValidationError(
            result = result,
            expectedError = ScanSettingsValidationError.MaxHostsOutOfRange
        )
    }

    @Test
    fun validate_whenTimeoutIsOutOfRange_shouldReturnTimeoutMillisOutOfRange() {
        val result = ScanSettingsValidator.validate(
            maxHostsText = ScanSettings.DEFAULT_MAX_HOSTS.toString(),
            timeoutMillisText = (ScanSettings.MIN_TIMEOUT_MILLIS - 1).toString(),
            parallelismText = ScanSettings.DEFAULT_PARALLELISM.toString()
        )

        assertValidationError(
            result = result,
            expectedError = ScanSettingsValidationError.TimeoutMillisOutOfRange
        )
    }

    @Test
    fun validate_whenParallelismIsOutOfRange_shouldReturnParallelismOutOfRange() {
        val result = ScanSettingsValidator.validate(
            maxHostsText = ScanSettings.DEFAULT_MAX_HOSTS.toString(),
            timeoutMillisText = ScanSettings.DEFAULT_TIMEOUT_MILLIS.toString(),
            parallelismText = (ScanSettings.MIN_PARALLELISM - 1).toString()
        )

        assertValidationError(
            result = result,
            expectedError = ScanSettingsValidationError.ParallelismOutOfRange
        )
    }

    private fun assertValidationError(
        result: ScanSettingsValidationResult,
        expectedError: ScanSettingsValidationError
    ) {
        assertTrue(result is ScanSettingsValidationResult.Error)

        val error = result as ScanSettingsValidationResult.Error

        assertEquals(
            expectedError,
            error.error
        )
    }
}
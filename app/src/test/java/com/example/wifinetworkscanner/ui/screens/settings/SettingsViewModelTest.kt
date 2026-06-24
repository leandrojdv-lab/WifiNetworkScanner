package com.example.wifinetworkscanner.ui.screens.settings

import com.example.wifinetworkscanner.R
import com.example.wifinetworkscanner.domain.model.ScanSettings
import com.example.wifinetworkscanner.domain.repository.ScanSettingsRepository
import com.example.wifinetworkscanner.domain.usecase.ObserveScanSettingsUseCase
import com.example.wifinetworkscanner.domain.usecase.UpdateScanSettingsUseCase
import com.example.wifinetworkscanner.test.MainDispatcherRule
import com.example.wifinetworkscanner.ui.text.UiText
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun init_whenRepositoryEmitsSettings_shouldUpdateUiState() = runTest(mainDispatcherRule.testDispatcher) {
        val settings = ScanSettings(
            maxHosts = 100,
            timeoutMillis = 500,
            parallelism = 8
        )
        val viewModel = createViewModel(
            scanSettingsRepository = FakeScanSettingsRepository(
                initialSettings = settings
            )
        )

        advanceUntilIdle()

        assertEquals(settings, viewModel.uiState.value.scanSettings)
        assertFalse(viewModel.uiState.value.isSaving)
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun updateScanSettings_whenValidTextValues_shouldSaveSettings() = runTest(mainDispatcherRule.testDispatcher) {
        val repository = FakeScanSettingsRepository()
        val viewModel = createViewModel(scanSettingsRepository = repository)
        val updatedSettings = ScanSettings(
            maxHosts = 120,
            timeoutMillis = 800,
            parallelism = 12
        )

        advanceUntilIdle()

        viewModel.updateScanSettings(
            maxHostsText = "120",
            timeoutMillisText = "800",
            parallelismText = "12"
        )
        advanceUntilIdle()

        assertEquals(updatedSettings, repository.currentSettings.value)
        assertFalse(viewModel.uiState.value.isSaving)
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun updateScanSettings_whenMaxHostsTextIsInvalid_shouldExposeValidationMessage() = runTest(
        mainDispatcherRule.testDispatcher
    ) {
        val repository = FakeScanSettingsRepository()
        val viewModel = createViewModel(scanSettingsRepository = repository)

        advanceUntilIdle()

        viewModel.updateScanSettings(
            maxHostsText = "abc",
            timeoutMillisText = "800",
            parallelismText = "12"
        )
        advanceUntilIdle()

        assertEquals(ScanSettings.default(), repository.currentSettings.value)
        assertFalse(viewModel.uiState.value.isSaving)
        assertEquals(
            UiText.StringResource(resId = R.string.settings_validation_max_hosts_required),
            viewModel.uiState.value.errorMessage
        )
    }

    @Test
    fun updateScanSettings_whenTimeoutIsOutOfRange_shouldExposeValidationMessage() = runTest(
        mainDispatcherRule.testDispatcher
    ) {
        val repository = FakeScanSettingsRepository()
        val viewModel = createViewModel(scanSettingsRepository = repository)

        advanceUntilIdle()

        viewModel.updateScanSettings(
            maxHostsText = "120",
            timeoutMillisText = "1",
            parallelismText = "12"
        )
        advanceUntilIdle()

        assertEquals(ScanSettings.default(), repository.currentSettings.value)
        assertFalse(viewModel.uiState.value.isSaving)
        assertEquals(
            UiText.StringResource(
                resId = R.string.settings_validation_timeout_range,
                args = listOf(
                    ScanSettings.MIN_TIMEOUT_MILLIS,
                    ScanSettings.MAX_TIMEOUT_MILLIS
                )
            ),
            viewModel.uiState.value.errorMessage
        )
    }

    @Test
    fun clearErrorMessage_whenCalled_shouldRemoveCurrentErrorMessage() = runTest(
        mainDispatcherRule.testDispatcher
    ) {
        val viewModel = createViewModel()

        advanceUntilIdle()

        viewModel.updateScanSettings(
            maxHostsText = "abc",
            timeoutMillisText = "800",
            parallelismText = "12"
        )
        advanceUntilIdle()

        viewModel.clearErrorMessage()

        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun updateScanSettings_whenRepositoryFails_shouldExposeErrorMessage() = runTest(mainDispatcherRule.testDispatcher) {
        val repository = FakeScanSettingsRepository(
            shouldFailOnUpdate = true
        )
        val viewModel = createViewModel(scanSettingsRepository = repository)

        advanceUntilIdle()

        viewModel.updateScanSettings(
            maxHostsText = ScanSettings.default().maxHosts.toString(),
            timeoutMillisText = ScanSettings.default().timeoutMillis.toString(),
            parallelismText = ScanSettings.default().parallelism.toString()
        )
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isSaving)
        assertEquals(
            UiText.StringResource(resId = R.string.settings_operation_failed_message),
            viewModel.uiState.value.errorMessage
        )
    }

    private fun createViewModel(
        scanSettingsRepository: ScanSettingsRepository = FakeScanSettingsRepository()
    ): SettingsViewModel {
        return SettingsViewModel(
            observeScanSettingsUseCase = ObserveScanSettingsUseCase(
                scanSettingsRepository = scanSettingsRepository
            ),
            updateScanSettingsUseCase = UpdateScanSettingsUseCase(
                scanSettingsRepository = scanSettingsRepository
            )
        )
    }

    private class FakeScanSettingsRepository(
        initialSettings: ScanSettings = ScanSettings.default(),
        private val shouldFailOnUpdate: Boolean = false
    ) : ScanSettingsRepository {

        val currentSettings = MutableStateFlow(initialSettings)

        override fun observeScanSettings(): Flow<ScanSettings> {
            return currentSettings
        }

        override suspend fun updateScanSettings(scanSettings: ScanSettings) {
            if (shouldFailOnUpdate) {
                throw IllegalStateException("Falha simulada ao salvar configurações.")
            }

            currentSettings.value = scanSettings
        }
    }
}
package com.example.wifinetworkscanner.ui.screens.networkscans

import androidx.lifecycle.SavedStateHandle
import com.example.wifinetworkscanner.R
import com.example.wifinetworkscanner.domain.model.NetworkScanGroup
import com.example.wifinetworkscanner.domain.model.ScanHistory
import com.example.wifinetworkscanner.domain.model.ScanSettings
import com.example.wifinetworkscanner.domain.repository.ScanHistoryRepository
import com.example.wifinetworkscanner.domain.usecase.DeleteScanHistoryUseCase
import com.example.wifinetworkscanner.domain.usecase.ObserveScansByNetworkUseCase
import com.example.wifinetworkscanner.test.MainDispatcherRule
import com.example.wifinetworkscanner.ui.text.UiText
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NetworkScansViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun init_whenRepositoryEmitsScans_shouldUpdateUiState() = runTest {
        val scanHistoryList = listOf(createScanHistory())
        val viewModel = createViewModel(
            scanHistoryRepository = FakeScanHistoryRepository(
                scansByNetwork = scanHistoryList
            )
        )

        advanceUntilIdle()

        assertEquals(false, viewModel.uiState.value.isLoading)
        assertEquals(NETWORK_IDENTIFIER, viewModel.uiState.value.networkIdentifier)
        assertEquals("Minha Rede", viewModel.uiState.value.networkName)
        assertEquals(scanHistoryList, viewModel.uiState.value.scanHistoryList)
        assertEquals(null, viewModel.uiState.value.errorMessage)
    }

    @Test
    fun deleteScanHistory_whenCalled_shouldDeleteAndEmitMessage() = runTest {
        val repository = FakeScanHistoryRepository()
        val viewModel = createViewModel(scanHistoryRepository = repository)
        val effects = mutableListOf<NetworkScansUiEffect>()

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiEffect.toList(effects)
        }

        viewModel.deleteScanHistory(scanHistoryId = SCAN_HISTORY_ID)
        advanceUntilIdle()

        assertEquals(listOf(SCAN_HISTORY_ID), repository.deletedScanHistoryIds)
        assertEquals(
            UiText.StringResource(resId = R.string.network_scans_removed_message),
            (effects.last() as NetworkScansUiEffect.ShowMessage).message
        )
    }

    @Test
    fun init_whenNetworkIdentifierIsBlank_shouldShowInvalidNetworkError() = runTest {
        val viewModel = createViewModel(
            encodedNetworkIdentifier = ""
        )

        advanceUntilIdle()

        assertEquals(false, viewModel.uiState.value.isLoading)
        assertEquals(
            UiText.StringResource(resId = R.string.network_scans_invalid_network_message),
            viewModel.uiState.value.errorMessage
        )
    }

    private fun createViewModel(
        encodedNetworkIdentifier: String = ENCODED_NETWORK_IDENTIFIER,
        scanHistoryRepository: ScanHistoryRepository = FakeScanHistoryRepository()
    ): NetworkScansViewModel {
        return NetworkScansViewModel(
            savedStateHandle = SavedStateHandle(
                mapOf(
                    NETWORK_IDENTIFIER_ARGUMENT to encodedNetworkIdentifier
                )
            ),
            observeScansByNetworkUseCase = ObserveScansByNetworkUseCase(
                scanHistoryRepository = scanHistoryRepository
            ),
            deleteScanHistoryUseCase = DeleteScanHistoryUseCase(
                scanHistoryRepository = scanHistoryRepository
            )
        )
    }

    private fun createScanHistory(): ScanHistory {
        return ScanHistory(
            id = SCAN_HISTORY_ID,
            startedAtEpochMillis = 1_000L,
            completedAtEpochMillis = 2_000L,
            networkName = "Minha Rede",
            networkIdentifier = NETWORK_IDENTIFIER,
            localIpAddress = "192.168.1.10",
            gatewayIpAddress = "192.168.1.1",
            prefixLength = 24,
            totalHostCount = 254,
            scannedHostCount = 254,
            scanSettings = ScanSettings.default(),
            devices = emptyList()
        )
    }

    private class FakeScanHistoryRepository(
        private val scansByNetwork: List<ScanHistory> = emptyList()
    ) : ScanHistoryRepository {

        val deletedScanHistoryIds = mutableListOf<Long>()

        override fun observeNetworkScanGroups(): Flow<List<NetworkScanGroup>> {
            return flowOf(emptyList())
        }

        override fun observeAllScanHistory(): Flow<List<ScanHistory>> {
            return flowOf(emptyList())
        }

        override fun observeRecentScanHistory(limit: Int): Flow<List<ScanHistory>> {
            return flowOf(emptyList())
        }

        override fun observeScanHistoryById(scanHistoryId: Long): Flow<ScanHistory?> {
            return flowOf(null)
        }

        override fun observeScanHistoryByNetworkIdentifier(
            networkIdentifier: String
        ): Flow<List<ScanHistory>> {
            return flowOf(scansByNetwork)
        }

        override suspend fun saveScanHistory(scanHistory: ScanHistory): Long {
            return scanHistory.id
        }

        override suspend fun deleteScanHistory(scanHistoryId: Long) {
            deletedScanHistoryIds.add(scanHistoryId)
        }

        override suspend fun deleteScanHistoryByNetworkIdentifier(networkIdentifier: String) {
            return
        }

        override suspend fun deleteAllScanHistory() {
            return
        }
    }

    private companion object {
        const val NETWORK_IDENTIFIER_ARGUMENT = "networkIdentifier"
        const val NETWORK_IDENTIFIER = "ssid:minha rede"
        const val ENCODED_NETWORK_IDENTIFIER = "ssid%3Aminha+rede"
        const val SCAN_HISTORY_ID = 10L
    }
}
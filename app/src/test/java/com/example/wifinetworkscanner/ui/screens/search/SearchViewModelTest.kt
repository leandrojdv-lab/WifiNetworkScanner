package com.example.wifinetworkscanner.ui.screens.search

import com.example.wifinetworkscanner.domain.model.DeviceDetectionMethod
import com.example.wifinetworkscanner.domain.model.NetworkDevice
import com.example.wifinetworkscanner.domain.model.NetworkScanGroup
import com.example.wifinetworkscanner.domain.model.ScanHistory
import com.example.wifinetworkscanner.domain.model.ScanSettings
import com.example.wifinetworkscanner.domain.repository.ScanHistoryRepository
import com.example.wifinetworkscanner.domain.usecase.ObserveAllScanHistoryUseCase
import com.example.wifinetworkscanner.domain.usecase.SearchScanHistoryUseCase
import com.example.wifinetworkscanner.test.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun updateQuery_whenCalled_shouldUpdateFilterImmediately() = runTest {
        val viewModel = createViewModel(
            scanHistoryList = listOf(createScanHistory())
        )

        viewModel.updateQuery("Notebook")

        assertEquals(
            "Notebook",
            viewModel.uiState.value.searchFilter.query
        )
    }

    @Test
    fun updateOpenPortText_whenCalled_shouldKeepOnlyDigits() = runTest {
        val viewModel = createViewModel(
            scanHistoryList = listOf(createScanHistory())
        )

        viewModel.updateOpenPortText("80abc443")

        assertEquals(
            "80443",
            viewModel.uiState.value.searchFilter.openPortText
        )
    }

    @Test
    fun updateQuery_whenDebounceCompletes_shouldUpdateResults() = runTest {
        val scanHistory = createScanHistory()
        val viewModel = createViewModel(
            scanHistoryList = listOf(scanHistory)
        )

        viewModel.updateQuery("Notebook")
        advanceTimeBy(SEARCH_DEBOUNCE_MILLIS + 1L)
        advanceUntilIdle()

        assertEquals(
            listOf(scanHistory),
            viewModel.uiState.value.results
        )
    }

    @Test
    fun clearFilters_whenCalled_shouldRestoreEmptyFilter() = runTest {
        val viewModel = createViewModel(
            scanHistoryList = listOf(createScanHistory())
        )

        viewModel.updateQuery("Notebook")
        viewModel.updateOpenPortText("443")
        viewModel.clearFilters()

        assertEquals(
            "",
            viewModel.uiState.value.searchFilter.query
        )
        assertEquals(
            "",
            viewModel.uiState.value.searchFilter.openPortText
        )
    }

    private fun createViewModel(
        scanHistoryList: List<ScanHistory>
    ): SearchViewModel {
        val repository = FakeScanHistoryRepository(
            scanHistoryList = scanHistoryList
        )

        return SearchViewModel(
            observeAllScanHistoryUseCase = ObserveAllScanHistoryUseCase(
                scanHistoryRepository = repository
            ),
            searchScanHistoryUseCase = SearchScanHistoryUseCase()
        )
    }

    private fun createScanHistory(): ScanHistory {
        return ScanHistory(
            id = 1L,
            startedAtEpochMillis = 1_000L,
            completedAtEpochMillis = 2_000L,
            networkName = "Minha Rede",
            networkIdentifier = "ssid:minha rede",
            localIpAddress = "192.168.1.10",
            gatewayIpAddress = "192.168.1.1",
            prefixLength = 24,
            totalHostCount = 254,
            scannedHostCount = 254,
            scanSettings = ScanSettings.default(),
            devices = listOf(
                NetworkDevice(
                    ipAddress = "192.168.1.20",
                    label = "Notebook",
                    detectionMethod = DeviceDetectionMethod.REACHABLE_AND_TCP_PORT,
                    latencyMillis = 12L,
                    openPorts = listOf(80, 443),
                    scannedAtEpochMillis = 1_500L
                )
            )
        )
    }

    private class FakeScanHistoryRepository(
        private val scanHistoryList: List<ScanHistory>
    ) : ScanHistoryRepository {

        override fun observeNetworkScanGroups(): Flow<List<NetworkScanGroup>> {
            return flowOf(emptyList())
        }

        override fun observeAllScanHistory(): Flow<List<ScanHistory>> {
            return flowOf(scanHistoryList)
        }

        override fun observeRecentScanHistory(limit: Int): Flow<List<ScanHistory>> {
            return flowOf(scanHistoryList.take(limit))
        }

        override fun observeScanHistoryById(scanHistoryId: Long): Flow<ScanHistory?> {
            return flowOf(scanHistoryList.firstOrNull { scanHistory ->
                scanHistory.id == scanHistoryId
            })
        }

        override fun observeScanHistoryByNetworkIdentifier(
            networkIdentifier: String
        ): Flow<List<ScanHistory>> {
            return flowOf(
                scanHistoryList.filter { scanHistory ->
                    scanHistory.networkIdentifier == networkIdentifier
                }
            )
        }

        override suspend fun saveScanHistory(scanHistory: ScanHistory): Long {
            return scanHistory.id
        }

        override suspend fun deleteScanHistory(scanHistoryId: Long) {
            return
        }

        override suspend fun deleteScanHistoryByNetworkIdentifier(networkIdentifier: String) {
            return
        }

        override suspend fun deleteAllScanHistory() {
            return
        }
    }

    private companion object {
        const val SEARCH_DEBOUNCE_MILLIS = 300L
    }
}
package com.example.wifinetworkscanner.ui.screens.history

import com.example.wifinetworkscanner.R
import com.example.wifinetworkscanner.domain.model.NetworkScanGroup
import com.example.wifinetworkscanner.domain.model.ScanHistory
import com.example.wifinetworkscanner.domain.repository.ScanHistoryRepository
import com.example.wifinetworkscanner.domain.usecase.DeleteAllScanHistoryUseCase
import com.example.wifinetworkscanner.domain.usecase.DeleteScansByNetworkUseCase
import com.example.wifinetworkscanner.domain.usecase.ObserveNetworkScanGroupsUseCase
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
class HistoryViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun init_whenRepositoryEmitsNetworkGroups_shouldUpdateUiState() = runTest {
        val networkGroups = listOf(createNetworkScanGroup())
        val viewModel = createViewModel(
            scanHistoryRepository = FakeScanHistoryRepository(
                networkGroups = networkGroups
            )
        )

        advanceUntilIdle()

        assertEquals(false, viewModel.uiState.value.isLoading)
        assertEquals(networkGroups, viewModel.uiState.value.networkScanGroups)
        assertEquals(null, viewModel.uiState.value.errorMessage)
    }

    @Test
    fun deleteNetworkScanHistory_whenCalled_shouldDeleteNetworkAndEmitMessage() = runTest {
        val repository = FakeScanHistoryRepository()
        val viewModel = createViewModel(scanHistoryRepository = repository)
        val effects = mutableListOf<HistoryUiEffect>()

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiEffect.toList(effects)
        }

        viewModel.deleteNetworkScanHistory(networkIdentifier = NETWORK_IDENTIFIER)
        advanceUntilIdle()

        assertEquals(listOf(NETWORK_IDENTIFIER), repository.deletedNetworkIdentifiers)
        assertEquals(
            UiText.StringResource(resId = R.string.history_network_removed_message),
            (effects.last() as HistoryUiEffect.ShowMessage).message
        )
    }

    @Test
    fun deleteAllScanHistory_whenCalled_shouldDeleteAllAndEmitMessage() = runTest {
        val repository = FakeScanHistoryRepository()
        val viewModel = createViewModel(scanHistoryRepository = repository)
        val effects = mutableListOf<HistoryUiEffect>()

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiEffect.toList(effects)
        }

        viewModel.deleteAllScanHistory()
        advanceUntilIdle()

        assertEquals(1, repository.deleteAllCount)
        assertEquals(
            UiText.StringResource(resId = R.string.history_cleared_message),
            (effects.last() as HistoryUiEffect.ShowMessage).message
        )
    }

    private fun createViewModel(
        scanHistoryRepository: ScanHistoryRepository = FakeScanHistoryRepository()
    ): HistoryViewModel {
        return HistoryViewModel(
            observeNetworkScanGroupsUseCase = ObserveNetworkScanGroupsUseCase(
                scanHistoryRepository = scanHistoryRepository
            ),
            deleteScansByNetworkUseCase = DeleteScansByNetworkUseCase(
                scanHistoryRepository = scanHistoryRepository
            ),
            deleteAllScanHistoryUseCase = DeleteAllScanHistoryUseCase(
                scanHistoryRepository = scanHistoryRepository
            )
        )
    }

    private fun createNetworkScanGroup(): NetworkScanGroup {
        return NetworkScanGroup(
            networkIdentifier = NETWORK_IDENTIFIER,
            networkName = "Minha Rede",
            latestGatewayIpAddress = "192.168.1.1",
            scanCount = 2L,
            latestCompletedAtEpochMillis = 2_000L,
            totalFoundDeviceCount = 4L
        )
    }

    private class FakeScanHistoryRepository(
        private val networkGroups: List<NetworkScanGroup> = emptyList()
    ) : ScanHistoryRepository {

        val deletedNetworkIdentifiers = mutableListOf<String>()
        var deleteAllCount = 0
            private set

        override fun observeNetworkScanGroups(): Flow<List<NetworkScanGroup>> {
            return flowOf(networkGroups)
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
            return flowOf(emptyList())
        }

        override suspend fun saveScanHistory(scanHistory: ScanHistory): Long {
            return scanHistory.id
        }

        override suspend fun deleteScanHistory(scanHistoryId: Long) {
            return
        }

        override suspend fun deleteScanHistoryByNetworkIdentifier(networkIdentifier: String) {
            deletedNetworkIdentifiers.add(networkIdentifier)
        }

        override suspend fun deleteAllScanHistory() {
            deleteAllCount += 1
        }
    }

    private companion object {
        const val NETWORK_IDENTIFIER = "ssid:minha rede"
    }
}
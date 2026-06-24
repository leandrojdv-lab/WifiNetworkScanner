package com.example.wifinetworkscanner.ui.screens.networkscanner

import com.example.wifinetworkscanner.domain.model.DeviceDetectionMethod
import com.example.wifinetworkscanner.domain.model.NetworkDevice
import com.example.wifinetworkscanner.domain.model.NetworkScanEvent
import com.example.wifinetworkscanner.domain.model.NetworkScanGroup
import com.example.wifinetworkscanner.domain.model.ScanHistory
import com.example.wifinetworkscanner.domain.model.ScanSettings
import com.example.wifinetworkscanner.domain.model.WifiNetworkInfo
import com.example.wifinetworkscanner.domain.repository.NetworkScannerRepository
import com.example.wifinetworkscanner.domain.repository.ScanHistoryRepository
import com.example.wifinetworkscanner.domain.repository.ScanSettingsRepository
import com.example.wifinetworkscanner.domain.usecase.ObserveScanSettingsUseCase
import com.example.wifinetworkscanner.domain.usecase.SaveScanHistoryUseCase
import com.example.wifinetworkscanner.domain.usecase.ScanWifiDevicesUseCase
import com.example.wifinetworkscanner.domain.usecase.UpdateScanSettingsUseCase
import com.example.wifinetworkscanner.test.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NetworkScannerViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun startScan_whenDevicesAreFoundOutOfOrder_shouldPublishDevicesSortedByIp() = runTest {
        val repository = FakeNetworkScannerRepository(
            events = listOf(
                NetworkScanEvent.Started(networkInfo = createWifiNetworkInfo()),
                NetworkScanEvent.DeviceFound(device = createDevice(ipAddress = "192.168.1.20")),
                NetworkScanEvent.DeviceFound(device = createDevice(ipAddress = "192.168.1.2"))
            )
        )
        val viewModel = createViewModel(networkScannerRepository = repository)

        viewModel.startScan()
        advanceUntilIdle()

        assertEquals(
            listOf(
                "192.168.1.2",
                "192.168.1.20"
            ),
            viewModel.uiState.value.devices.map { device -> device.ipAddress }
        )
    }

    @Test
    fun startScan_whenSameIpIsFoundTwice_shouldReplaceExistingDevice() = runTest {
        val repository = FakeNetworkScannerRepository(
            events = listOf(
                NetworkScanEvent.Started(networkInfo = createWifiNetworkInfo()),
                NetworkScanEvent.DeviceFound(
                    device = createDevice(
                        ipAddress = "192.168.1.20",
                        label = "Antigo"
                    )
                ),
                NetworkScanEvent.DeviceFound(
                    device = createDevice(
                        ipAddress = "192.168.1.20",
                        label = "Atualizado"
                    )
                )
            )
        )
        val viewModel = createViewModel(networkScannerRepository = repository)

        viewModel.startScan()
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.devices.size)
        assertEquals("Atualizado", viewModel.uiState.value.devices.first().label)
    }

    @Test
    fun startScan_whenCompleted_shouldStopScanningAndSaveHistory() = runTest {
        val devices = listOf(
            createDevice(ipAddress = "192.168.1.20")
        )
        val networkScannerRepository = FakeNetworkScannerRepository(
            events = listOf(
                NetworkScanEvent.Started(networkInfo = createWifiNetworkInfo()),
                NetworkScanEvent.DeviceFound(device = devices.first()),
                NetworkScanEvent.Progress(
                    scannedHostCount = 2,
                    totalHostCount = 2
                ),
                NetworkScanEvent.Completed(devices = devices)
            )
        )
        val scanHistoryRepository = FakeScanHistoryRepository()
        val viewModel = createViewModel(
            networkScannerRepository = networkScannerRepository,
            scanHistoryRepository = scanHistoryRepository
        )

        viewModel.startScan()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isScanning)
        assertEquals(devices, viewModel.uiState.value.devices)
        assertEquals(1, scanHistoryRepository.savedHistoryList.size)
        assertEquals(devices, scanHistoryRepository.savedHistoryList.first().devices)
    }

    private fun createViewModel(
        networkScannerRepository: NetworkScannerRepository,
        scanSettingsRepository: ScanSettingsRepository = FakeScanSettingsRepository(),
        scanHistoryRepository: ScanHistoryRepository = FakeScanHistoryRepository()
    ): NetworkScannerViewModel {
        return NetworkScannerViewModel(
            scanWifiDevicesUseCase = ScanWifiDevicesUseCase(
                networkScannerRepository = networkScannerRepository
            ),
            observeScanSettingsUseCase = ObserveScanSettingsUseCase(
                scanSettingsRepository = scanSettingsRepository
            ),
            updateScanSettingsUseCase = UpdateScanSettingsUseCase(
                scanSettingsRepository = scanSettingsRepository
            ),
            saveScanHistoryUseCase = SaveScanHistoryUseCase(
                scanHistoryRepository = scanHistoryRepository
            )
        )
    }

    private fun createWifiNetworkInfo(): WifiNetworkInfo {
        return WifiNetworkInfo(
            localIpAddress = "192.168.1.10",
            prefixLength = 24,
            interfaceName = "wlan0",
            totalHostCount = 2,
            gatewayIpAddress = "192.168.1.1",
            networkName = "Minha Rede",
            networkIdentifier = "ssid:minha rede"
        )
    }

    private fun createDevice(
        ipAddress: String,
        label: String? = null
    ): NetworkDevice {
        return NetworkDevice(
            ipAddress = ipAddress,
            latencyMillis = 10L,
            scannedAtEpochMillis = 1_000L,
            detectionMethod = DeviceDetectionMethod.REACHABLE,
            openPorts = emptyList(),
            label = label
        )
    }

    private class FakeNetworkScannerRepository(
        private val events: List<NetworkScanEvent>
    ) : NetworkScannerRepository {

        override fun scanConnectedDevices(
            maxHosts: Int,
            timeoutMillis: Int,
            parallelism: Int
        ): Flow<NetworkScanEvent> {
            return flowOf(*events.toTypedArray())
        }
    }

    private class FakeScanSettingsRepository : ScanSettingsRepository {

        private var scanSettings = ScanSettings.default()

        override fun observeScanSettings(): Flow<ScanSettings> {
            return flowOf(scanSettings)
        }

        override suspend fun updateScanSettings(scanSettings: ScanSettings) {
            this.scanSettings = scanSettings
        }
    }

    private class FakeScanHistoryRepository : ScanHistoryRepository {

        val savedHistoryList = mutableListOf<ScanHistory>()

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
            return flowOf(emptyList())
        }

        override suspend fun saveScanHistory(scanHistory: ScanHistory): Long {
            savedHistoryList.add(scanHistory)
            return 1L
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
}
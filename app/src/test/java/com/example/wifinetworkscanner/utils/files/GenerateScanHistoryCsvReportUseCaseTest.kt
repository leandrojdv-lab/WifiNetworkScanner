package com.example.wifinetworkscanner.domain.usecase

import com.example.wifinetworkscanner.domain.model.DeviceDetectionMethod
import com.example.wifinetworkscanner.domain.model.NetworkDevice
import com.example.wifinetworkscanner.domain.model.ScanHistory
import com.example.wifinetworkscanner.domain.model.ScanSettings
import org.junit.Assert.assertTrue
import org.junit.Test

class GenerateScanHistoryCsvReportUseCaseTest {

    @Test
    fun invoke_whenScanHistoryHasDevice_shouldGenerateCsvWithDeviceData() {
        val useCase = GenerateScanHistoryCsvReportUseCase()
        val scanHistory = createScanHistory()

        val csv = useCase(scanHistory)

        assertTrue(csv.contains("section;field;value"))
        assertTrue(csv.contains("network;network_name;Minha Rede"))
        assertTrue(csv.contains("device_ip_address;device_label;detection_method;latency_millis;open_ports;scanned_at_epoch_millis"))
        assertTrue(csv.contains("192.168.1.20;Notebook;REACHABLE_AND_TCP_PORT;12;80|443;3000"))
    }

    @Test
    fun invoke_whenValueHasSeparator_shouldEscapeCsvCell() {
        val useCase = GenerateScanHistoryCsvReportUseCase()
        val scanHistory = createScanHistory(
            deviceLabel = "Notebook; Quarto"
        )

        val csv = useCase(scanHistory)

        assertTrue(csv.contains("192.168.1.20;\"Notebook; Quarto\";REACHABLE_AND_TCP_PORT"))
    }

    private fun createScanHistory(
        deviceLabel: String = "Notebook"
    ): ScanHistory {
        return ScanHistory(
            id = 1L,
            startedAtEpochMillis = 1_000L,
            completedAtEpochMillis = 4_500L,
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
                    label = deviceLabel,
                    detectionMethod = DeviceDetectionMethod.REACHABLE_AND_TCP_PORT,
                    latencyMillis = 12L,
                    openPorts = listOf(80, 443),
                    scannedAtEpochMillis = 3_000L
                )
            )
        )
    }
}
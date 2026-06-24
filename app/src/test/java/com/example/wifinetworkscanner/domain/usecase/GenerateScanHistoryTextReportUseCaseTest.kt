package com.example.wifinetworkscanner.domain.usecase

import com.example.wifinetworkscanner.domain.model.ScanHistory
import com.example.wifinetworkscanner.domain.model.ScanSettings
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GenerateScanHistoryTextReportUseCaseTest {

    @Test
    fun invoke_whenScanHistoryHasNoDevices_shouldGenerateReportWithEmptyDeviceMessage() {
        val useCase = GenerateScanHistoryTextReportUseCase()
        val scanHistory = createScanHistory()

        val report = useCase(scanHistory)

        assertTrue(report.contains("Relatório de varredura Wi-Fi"))
        assertTrue(report.contains("Rede: Minha Rede"))
        assertTrue(report.contains("IP local: 192.168.1.10/24"))
        assertTrue(report.contains("Gateway: 192.168.1.1"))
        assertTrue(report.contains("Nenhum dispositivo encontrado nesta varredura."))
    }

    @Test
    fun invoke_whenGatewayIsNull_shouldUseUnknownValue() {
        val useCase = GenerateScanHistoryTextReportUseCase()
        val scanHistory = createScanHistory(
            gatewayIpAddress = null
        )

        val report = useCase(scanHistory)

        assertTrue(report.contains("Gateway: não identificado"))
        assertFalse(report.contains("Gateway: null"))
    }

    @Test
    fun invoke_whenNetworkNameIsBlank_shouldUseUnknownValue() {
        val useCase = GenerateScanHistoryTextReportUseCase()
        val scanHistory = createScanHistory(
            networkName = ""
        )

        val report = useCase(scanHistory)

        assertTrue(report.contains("Rede: não identificado"))
    }

    private fun createScanHistory(
        networkName: String = "Minha Rede",
        gatewayIpAddress: String? = "192.168.1.1"
    ): ScanHistory {
        return ScanHistory(
            id = 1L,
            startedAtEpochMillis = 1_000L,
            completedAtEpochMillis = 4_500L,
            networkName = networkName,
            networkIdentifier = "ssid:minha rede",
            localIpAddress = "192.168.1.10",
            gatewayIpAddress = gatewayIpAddress,
            prefixLength = 24,
            totalHostCount = 254,
            scannedHostCount = 254,
            scanSettings = ScanSettings.default(),
            devices = emptyList()
        )
    }
}
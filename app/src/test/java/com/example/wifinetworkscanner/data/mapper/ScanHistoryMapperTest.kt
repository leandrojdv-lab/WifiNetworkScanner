package com.example.wifinetworkscanner.data.mapper

import com.example.wifinetworkscanner.data.local.entity.ScanDeviceEntity
import com.example.wifinetworkscanner.data.local.entity.ScanHistoryEntity
import com.example.wifinetworkscanner.data.local.relation.ScanHistoryWithDevices
import com.example.wifinetworkscanner.domain.model.DeviceDetectionMethod
import com.example.wifinetworkscanner.domain.model.NetworkDevice
import com.example.wifinetworkscanner.domain.model.ScanHistory
import com.example.wifinetworkscanner.domain.model.ScanSettings
import org.junit.Assert.assertEquals
import org.junit.Test

class ScanHistoryMapperTest {

    @Test
    fun toEntity_whenScanHistoryIsValid_shouldMapSummaryFields() {
        val scanHistory = ScanHistory(
            id = 10L,
            startedAtEpochMillis = 1_000L,
            completedAtEpochMillis = 2_000L,
            networkName = "Minha Rede",
            networkIdentifier = "ssid:minha rede",
            localIpAddress = "192.168.1.25",
            gatewayIpAddress = "192.168.1.1",
            prefixLength = 24,
            totalHostCount = 254,
            scannedHostCount = 254,
            scanSettings = ScanSettings(
                maxHosts = 254,
                timeoutMillis = 700,
                parallelism = 32
            ),
            devices = listOf(
                NetworkDevice(
                    ipAddress = "192.168.1.1",
                    latencyMillis = 15L,
                    scannedAtEpochMillis = 1_500L,
                    detectionMethod = DeviceDetectionMethod.REACHABLE_AND_TCP_PORT,
                    openPorts = listOf(80, 443),
                    label = "Gateway/Roteador provável"
                )
            )
        )

        val entity = scanHistory.toEntity()

        assertEquals(10L, entity.scanHistoryId)
        assertEquals("Minha Rede", entity.networkName)
        assertEquals("ssid:minha rede", entity.networkIdentifier)
        assertEquals("192.168.1.25", entity.localIpAddress)
        assertEquals("192.168.1.1", entity.gatewayIpAddress)
        assertEquals(24, entity.networkPrefixLength)
        assertEquals(254, entity.totalHostCount)
        assertEquals(1, entity.foundDeviceCount)
        assertEquals(700, entity.timeoutMillis)
    }

    @Test
    fun toEntity_whenNetworkDeviceHasPorts_shouldMapPortsAsCsv() {
        val networkDevice = NetworkDevice(
            ipAddress = "192.168.1.1",
            latencyMillis = 15L,
            scannedAtEpochMillis = 1_500L,
            detectionMethod = DeviceDetectionMethod.REACHABLE_AND_TCP_PORT,
            openPorts = listOf(80, 443),
            label = "Gateway/Roteador provável"
        )

        val entity = networkDevice.toEntity(scanHistoryOwnerId = 20L)

        assertEquals(20L, entity.scanHistoryOwnerId)
        assertEquals("192.168.1.1", entity.ipAddress)
        assertEquals("REACHABLE_AND_TCP_PORT", entity.detectionMethod)
        assertEquals("80,443", entity.openPortsCsv)
        assertEquals("Gateway/Roteador provável", entity.label)
    }

    @Test
    fun toDomain_whenDetectionMethodIsUnknown_shouldUseReachableFallback() {
        val scanHistoryWithDevices = ScanHistoryWithDevices(
            scanHistory = createScanHistoryEntity(),
            devices = listOf(
                ScanDeviceEntity(
                    scanDeviceId = 1L,
                    scanHistoryOwnerId = 10L,
                    ipAddress = "192.168.1.30",
                    latencyMillis = 18L,
                    scannedAtEpochMillis = 1_800L,
                    detectionMethod = "LEGACY_UNKNOWN_METHOD",
                    openPortsCsv = "80,443",
                    label = "Dispositivo antigo"
                )
            )
        )

        val domain = scanHistoryWithDevices.toDomain()

        assertEquals(DeviceDetectionMethod.REACHABLE, domain.devices.first().detectionMethod)
        assertEquals(listOf(80, 443), domain.devices.first().openPorts)
    }

    private fun createScanHistoryEntity(): ScanHistoryEntity {
        return ScanHistoryEntity(
            scanHistoryId = 10L,
            startedAtEpochMillis = 1_000L,
            completedAtEpochMillis = 2_000L,
            networkName = "Minha Rede",
            networkIdentifier = "ssid:minha rede",
            localIpAddress = "192.168.1.25",
            gatewayIpAddress = "192.168.1.1",
            networkPrefixLength = 24,
            totalHostCount = 254,
            scannedHostCount = 254,
            foundDeviceCount = 1,
            maxHosts = 254,
            timeoutMillis = 700,
            parallelism = 32
        )
    }
}
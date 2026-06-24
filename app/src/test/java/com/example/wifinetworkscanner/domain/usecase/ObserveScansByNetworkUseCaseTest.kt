package com.example.wifinetworkscanner.domain.usecase

import com.example.wifinetworkscanner.domain.model.NetworkScanGroup
import com.example.wifinetworkscanner.domain.model.ScanHistory
import com.example.wifinetworkscanner.domain.model.ScanSettings
import com.example.wifinetworkscanner.domain.repository.ScanHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ObserveScansByNetworkUseCaseTest {

    @Test
    fun invoke_whenNetworkIdentifierIsValid_shouldReturnRepositoryScans() = runTest {
        val expectedScans = listOf(
            ScanHistory(
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
                devices = emptyList()
            )
        )
        val repository = FakeScanHistoryRepository(scanHistoryList = expectedScans)
        val useCase = ObserveScansByNetworkUseCase(
            scanHistoryRepository = repository
        )

        val result = useCase(networkIdentifier = "ssid:minha rede").first()

        assertEquals(expectedScans, result)
    }

    @Test(expected = IllegalArgumentException::class)
    fun invoke_whenNetworkIdentifierIsBlank_shouldThrowException() {
        val repository = FakeScanHistoryRepository(scanHistoryList = emptyList())
        val useCase = ObserveScansByNetworkUseCase(
            scanHistoryRepository = repository
        )

        useCase(networkIdentifier = "")
    }

    private class FakeScanHistoryRepository(
        private val scanHistoryList: List<ScanHistory>
    ) : ScanHistoryRepository {

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
            return flowOf(scanHistoryList)
        }

        override suspend fun saveScanHistory(scanHistory: ScanHistory): Long {
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
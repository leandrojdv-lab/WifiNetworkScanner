package com.example.wifinetworkscanner.domain.usecase

import com.example.wifinetworkscanner.domain.model.NetworkScanGroup
import com.example.wifinetworkscanner.domain.model.ScanHistory
import com.example.wifinetworkscanner.domain.repository.ScanHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ObserveNetworkScanGroupsUseCaseTest {

    @Test
    fun invoke_whenCalled_shouldReturnRepositoryGroups() = runTest {
        val expectedGroups = listOf(
            NetworkScanGroup(
                networkIdentifier = "ssid:minha rede",
                networkName = "Minha Rede",
                latestGatewayIpAddress = "192.168.1.1",
                scanCount = 2L,
                latestCompletedAtEpochMillis = 2_000L,
                totalFoundDeviceCount = 8L
            )
        )
        val repository = FakeScanHistoryRepository(networkScanGroups = expectedGroups)
        val useCase = ObserveNetworkScanGroupsUseCase(
            scanHistoryRepository = repository
        )

        val result = useCase().first()

        assertEquals(expectedGroups, result)
    }

    private class FakeScanHistoryRepository(
        private val networkScanGroups: List<NetworkScanGroup>
    ) : ScanHistoryRepository {

        override fun observeNetworkScanGroups(): Flow<List<NetworkScanGroup>> {
            return flowOf(networkScanGroups)
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
package com.example.wifinetworkscanner.domain.usecase

import com.example.wifinetworkscanner.domain.model.NetworkScanGroup
import com.example.wifinetworkscanner.domain.model.ScanHistory
import com.example.wifinetworkscanner.domain.repository.ScanHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class DeleteScansByNetworkUseCaseTest {

    @Test
    fun invoke_whenNetworkIdentifierIsValid_shouldDelegateDeleteToRepository() = runTest {
        val repository = FakeScanHistoryRepository()
        val useCase = DeleteScansByNetworkUseCase(
            scanHistoryRepository = repository
        )

        useCase(networkIdentifier = "ssid:minha rede")

        assertEquals(
            "ssid:minha rede",
            repository.deletedNetworkIdentifier
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun invoke_whenNetworkIdentifierIsBlank_shouldThrowException() = runTest {
        val repository = FakeScanHistoryRepository()
        val useCase = DeleteScansByNetworkUseCase(
            scanHistoryRepository = repository
        )

        useCase(networkIdentifier = "")
    }

    private class FakeScanHistoryRepository : ScanHistoryRepository {

        var deletedNetworkIdentifier: String? = null

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
            return 1L
        }

        override suspend fun deleteScanHistory(scanHistoryId: Long) {
            return
        }

        override suspend fun deleteScanHistoryByNetworkIdentifier(networkIdentifier: String) {
            deletedNetworkIdentifier = networkIdentifier
        }

        override suspend fun deleteAllScanHistory() {
            return
        }
    }
}
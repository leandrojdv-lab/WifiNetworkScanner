package com.example.wifinetworkscanner.domain.usecase

import com.example.wifinetworkscanner.domain.model.NetworkScanGroup
import com.example.wifinetworkscanner.domain.model.ScanHistory
import com.example.wifinetworkscanner.domain.repository.ScanHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test

class DeleteAllScanHistoryUseCaseTest {

    @Test
    fun invoke_whenCalled_shouldDelegateDeleteAllToRepository() = runTest {
        val repository = FakeScanHistoryRepository()
        val useCase = DeleteAllScanHistoryUseCase(
            scanHistoryRepository = repository
        )

        useCase()

        assertTrue(repository.wasDeleteAllCalled)
    }

    private class FakeScanHistoryRepository : ScanHistoryRepository {

        var wasDeleteAllCalled: Boolean = false

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
            return
        }

        override suspend fun deleteAllScanHistory() {
            wasDeleteAllCalled = true
        }
    }
}
package com.example.wifinetworkscanner.domain.repository

import com.example.wifinetworkscanner.domain.model.NetworkScanGroup
import com.example.wifinetworkscanner.domain.model.ScanHistory
import kotlinx.coroutines.flow.Flow

interface ScanHistoryRepository {

    fun observeNetworkScanGroups(): Flow<List<NetworkScanGroup>>

    fun observeAllScanHistory(): Flow<List<ScanHistory>>

    fun observeRecentScanHistory(limit: Int = DEFAULT_HISTORY_LIMIT): Flow<List<ScanHistory>>

    fun observeScanHistoryById(scanHistoryId: Long): Flow<ScanHistory?>

    fun observeScanHistoryByNetworkIdentifier(networkIdentifier: String): Flow<List<ScanHistory>>

    suspend fun saveScanHistory(scanHistory: ScanHistory): Long

    suspend fun deleteScanHistory(scanHistoryId: Long)

    suspend fun deleteScanHistoryByNetworkIdentifier(networkIdentifier: String)

    suspend fun deleteAllScanHistory()

    companion object {
        const val DEFAULT_HISTORY_LIMIT = 20
    }
}